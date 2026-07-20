import os
import json
import time
import sys
import stomp
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, Index
from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy.sql import func
from framework.factory import ScraperFactory

# 1. Database connection config
DB_HOST = os.getenv("DB_HOST", "postgres-db")
DB_NAME = os.getenv("DB_NAME", "postgres")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "root")

DATABASE_URL = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}/{DB_NAME}"
engine = create_engine(DATABASE_URL, pool_size=10, max_overflow=20)
SessionLocal = sessionmaker(bind=engine)
Base = declarative_base()

# 2. Map the SQLAlchemy Model to the Spring-generated table
class ScrapedJob(Base):
    __tablename__ = 'scraped_jobs'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    request_id = Column(Integer, nullable=False)
    title = Column(String(255), nullable=False)
    company = Column(String(150), nullable=False)
    url = Column(Text, nullable=False, unique=True)
    description = Column(Text)
    scraped_at = Column(DateTime, default=func.now())

# 3. ActiveMQ Connection credentials
ACTIVEMQ_HOST = os.getenv("ACTIVEMQ_HOST", "activemq-broker")
ACTIVEMQ_PORT = int(os.getenv("ACTIVEMQ_PORT", 61613))
ACTIVEMQ_USER = os.getenv("ACTIVEMQ_USER", "admin")
ACTIVEMQ_PASSWORD = os.getenv("ACTIVEMQ_PASSWORD", "adminPassword")
QUEUE_NAME = "job.scrape.queue"  # 👈 Keep this clean

class JobQueueListener(stomp.ConnectionListener):
    def __init__(self, conn=None):
        self.conn = conn

    def on_message(self, frame):
        db_session = SessionLocal()
        try:
            print(f"📥 Raw Message Received from Queue: {repr(frame.body)}")          
            payload = json.loads(frame.body)
            request_id = payload.get("requestId")
            site_code = payload.get("siteCode")
            target_url = payload.get("targetUrl")
            keywords = payload.get("keywords", [])

            print(f"\n⚡ [Queue Event] Request #{request_id} | Engine: {site_code} | Keywords: {keywords}")

            # Instantiate dynamic scraper from factory
            scraper = ScraperFactory.get_scraper(site_code, target_url)
            scraped_jobs = scraper.scrape(keywords)

            print(f"🎉 Extracted {len(scraped_jobs)} matched listings. Saving to database...")

            saved_count = 0
            for job in scraped_jobs:
                # Deduplication logic: Check if job link already exists
                exists = db_session.query(ScrapedJob).filter(ScrapedJob.url == job['url']).first()
                if not exists:
                    db_job = ScrapedJob(
                        request_id=request_id,
                        title=job['title'],
                        company=job['company'],
                        url=job['url'],
                        description=job['description']
                    )
                    db_session.add(db_job)
                    saved_count += 1
            
            db_session.commit()
            print(f"✅ Successfully inserted {saved_count} new jobs into DB.")

        except Exception as e:
            db_session.rollback()
            print(f"❌ Worker Process Error: {e}", file=sys.stderr)
        finally:
            db_session.close()

def start_worker():
    print("🤖 Starting Modular Python Scraper Daemon...")
    conn = stomp.Connection([(ACTIVEMQ_HOST, ACTIVEMQ_PORT)])
    conn.set_listener('ScraperListener', JobQueueListener(conn))
    
    while True:
        try:
            conn.connect(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD, wait=True)
            
            # ✅ Only ONE clean subscription call with a string ID and the ANYCAST header
            conn.subscribe(
                destination=QUEUE_NAME, 
                id="sub-0", 
                ack='auto',
                headers={'subscription-type': 'ANYCAST'}
            )
            
            print(f"🔌 Connected to ActiveMQ Broker! Listening on: {QUEUE_NAME}")
            break
        except Exception as e:
            print(f"🔌 Broker connection failed ({e}). Retrying in 5 seconds...")
            time.sleep(5)

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Halting Python Daemon...")
        conn.disconnect()

if __name__ == "__main__":
    start_worker()