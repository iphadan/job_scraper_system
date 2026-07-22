# framework/scrapers/eaglelion_scraper.py
from typing import Any, Dict, List
from bs4 import BeautifulSoup
import requests
import urllib3
from framework.base_scraper import BaseScraper


class EagleLionScraper(BaseScraper):

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(f"📡 Fetching EagleLion Systems: {self.target_url}")
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        )
    }

    try:
      response = requests.get(
          self.target_url, headers=headers, verify=False, timeout=15
      )
      if response.status_code != 200:
        print(f"❌ EagleLion returned HTTP status: {response.status_code}")
        return []
    except Exception as e:
      print(f"❌ EagleLion Network Error: {e}")
      return []

    soup = BeautifulSoup(response.text, "html.parser")
    matched_jobs = []

    # EagleLion job listing elements
    for container in soup.find_all(["div", "section"]):
      heading = container.find(["h3", "h4", "a"])
      if not heading:
        continue

      title = heading.text.strip()
      link = container.find("a", href=True)

      if link:
        job_url = (
            "https://www.eaglelionsystems.com" + link["href"]
            if link["href"].startswith("/")
            else link["href"]
        )
      else:
        job_url = self.target_url

      if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
        matched_jobs.append({
            "title": title,
            "company": "EagleLion System Technology",
            "url": job_url,
            "description": f"Role at EagleLion: {title}",
        })

    return matched_jobs