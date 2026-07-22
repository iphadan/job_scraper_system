# framework/scrapers/safaricom_et_scraper.py
from typing import Any, Dict, List
from bs4 import BeautifulSoup
import requests
import urllib3
from framework.base_scraper import BaseScraper


class SafaricomEtScraper(BaseScraper):

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(f"📡 Fetching Safaricom Ethiopia Careers: {self.target_url}")
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
        print(f"❌ Safaricom ET returned status: {response.status_code}")
        return []
    except Exception as e:
      print(f"❌ Safaricom ET Network Error: {e}")
      return []

    soup = BeautifulSoup(response.text, "html.parser")
    matched_jobs = []

    for item in soup.find_all(["tr", "div", "li"]):
      link = item.find("a", href=True)
      title_elem = item.find(["h3", "h4", "strong", "a"])

      if not title_elem:
        continue

      title = title_elem.text.strip()
      if len(title) < 3 or "vacancy" in title.lower():
        continue

      job_url = (
          "https://www.safaricom.et" + link["href"]
          if link and link["href"].startswith("/")
          else (link["href"] if link else self.target_url)
      )

      if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
        matched_jobs.append({
            "title": title,
            "company": "Safaricom Telecommunications Ethiopia PLC",
            "url": job_url,
            "description": f"Vacancy at Safaricom Ethiopia: {title}",
        })

    return matched_jobs