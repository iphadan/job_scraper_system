from typing import Any, Dict, List
from bs4 import BeautifulSoup
import requests
import urllib3
import cloudscraper

from framework.base_scraper import BaseScraper


class WwrScraper(BaseScraper):

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(f"📡 Fetching We Work Remotely: {self.target_url}")

    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    # 🌟 Full realistic browser headers to prevent Cloudflare/Nginx HTTP 499 drops
    headers = {
        'User-Agent': (
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            ' (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'
        ),
        'Accept': (
            'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8'
        ),
        'Accept-Language': 'en-US,en;q=0.9',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'none',
        'Sec-Fetch-User': '?1',
    }

    try:
      # Use a Session object to maintain persistent TCP connection settings
    #   session = requests.Session()
    #   response = session.get(
    #       self.target_url, headers=headers, verify=False, timeout=20
    #   )

      scraper = cloudscraper.create_scraper()
      response = scraper.get(self.target_url, verify=False, timeout=20)

      if response.status_code != 200:
        print(f'❌ WWR returned HTTP status: {response.status_code}')
        return []

    except requests.exceptions.RequestException as e:
      print(f'❌ Network error while fetching WWR: {e}')
      return []

    soup = BeautifulSoup(response.text, 'html.parser')
    job_sections = soup.find_all('section', class_='jobs')

    matched_jobs = []
    for section in job_sections:
      listings = section.find_all('li')
      for listing in listings:
        anchor = listing.find('a', href=True)
        if not anchor or '/jobs/' not in anchor['href']:
          continue

        title_elem = listing.find('span', class_='title')
        company_elem = listing.find('span', class_='company')

        title = title_elem.text.strip() if title_elem else ''
        company = (
            company_elem.text.strip() if company_elem else 'Unknown Company'
        )

        job_url = (
            'https://weworkremotely.com' + anchor['href']
            if anchor['href'].startswith('/')
            else anchor['href']
        )

        if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
          matched_jobs.append({
              'title': title,
              'company': company,
              'url': job_url,
              'description': (
                  'Click link to view job description on We Work Remotely.'
              ),
          })

    return matched_jobs