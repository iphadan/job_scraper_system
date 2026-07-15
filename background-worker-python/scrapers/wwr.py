import requests
from bs4 import BeautifulSoup
from typing import List, Dict, Any
from framework.base_scraper import BaseScraper

class WwrScraper(BaseScraper):
    def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
        print(f"📡 Fetching We Work Remotely: {self.target_url}")
        
        response = requests.get(self.target_url, headers=self.headers, timeout=15)
        if response.status_code != 200:
            print(f"❌ WWR returned HTTP error: {response.status_code}")
            return []

        soup = BeautifulSoup(response.text, 'html.parser')
        job_sections = soup.find_all('section', class_='jobs')
        
        matched_jobs = []
        for section in job_sections:
            listings = section.find_all('li')
            for listing in listings:
                anchor = listing.find('a', href=True)
                if not anchor or 'jobs/' not in anchor['href']:
                    continue
                    
                title_elem = listing.find('span', class_='title')
                company_elem = listing.find('span', class_='company')
                
                title = title_elem.text.strip() if title_elem else ""
                company = company_elem.text.strip() if company_elem else "Unknown"
                url = "https://weworkremotely.com" + anchor['href']
                
                if any(kw.lower() in title.lower() for kw in keywords):
                    matched_jobs.append({
                        "title": title,
                        "company": company,
                        "url": url,
                        "description": "Click link to view job description on WWR."
                    })
        return matched_jobs