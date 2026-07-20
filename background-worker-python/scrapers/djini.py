import requests
from bs4 import BeautifulSoup

class DjiniScraper:
    def __init__(self, target_url):
        self.target_url = target_url

    def scrape(self, keywords):
        print(f"📡 Scraping Djini: {self.target_url} for keywords: {keywords}")
        headers = {'User-Agent': 'Mozilla/5.0'}
        matched_jobs = []

        try:
            # Basic HTML Fetching (Adjust selectors based on Djini HTML structure)
            response = requests.get("https://djinni.co/jobs/", headers=headers, timeout=10)
            if response.status_code != 200:
                return matched_jobs

            soup = BeautifulSoup(response.text, 'html.parser')
            job_cards = soup.find_all('li', class_='list-jobs__item')

            for card in job_cards:
                title_elem = card.find('a', class_='job-list-item__link')
                if not title_elem:
                    continue

                title = title_elem.text.strip()
                link = "https://djinni.co" + title_elem['href']
                description = card.text.strip()

                # Filter by user keywords
                if any(kw.lower() in (title + description).lower() for kw in keywords):
                    matched_jobs.append({
                        'title': title,
                        'company': 'Djini Client',
                        'url': link,
                        'description': description[:300] + "..."
                    })

        except Exception as e:
            print(f"❌ Djini Scrape Error: {e}")

        return matched_jobs