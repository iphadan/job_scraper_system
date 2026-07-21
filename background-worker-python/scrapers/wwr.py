from typing import Any, Dict, List
from bs4 import BeautifulSoup
import requests
import urllib3

from framework.base_scraper import BaseScraper


class WwrScraper(BaseScraper):

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(f"📡 Fetching We Work Remotely: {self.target_url}")

    # Suppress local self-signed cert SSL warnings from corporate proxies
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            " (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
    }

    try:
      # 🌟 Fixed: Use self.target_url instead of undefined 'url'
      response = requests.get(
          self.target_url, headers=headers, verify=False, timeout=15
      )

      if response.status_code != 200:
        print(f"❌ WWR returned HTTP status: {response.status_code}")
        return []

    except requests.exceptions.RequestException as e:
      print(f"❌ Network error while fetching WWR: {e}")
      return []

    soup = BeautifulSoup(response.text, "html.parser")
    job_sections = soup.find_all("section", class_="jobs")

    matched_jobs = []
    for section in job_sections:
      listings = section.find_all("li")
      for listing in listings:
        anchor = listing.find("a", href=True)
        if not anchor or "/jobs/" not in anchor["href"]:
          continue

        title_elem = listing.find("span", class_="title")
        company_elem = listing.find("span", class_="company")

        title = title_elem.text.strip() if title_elem else ""
        company = (
            company_elem.text.strip() if company_elem else "Unknown Company"
        )

        # 🌟 Renamed variable to avoid shadowing
        job_url = (
            "https://weworkremotely.com" + anchor["href"]
            if anchor["href"].startswith("/")
            else anchor["href"]
        )

        # Case-insensitive keyword matching on job title
        if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
          matched_jobs.append({
              "title": title,
              "company": company,
              "url": job_url,
              "description": (
                  "Click the link to view full job description on We Work"
                  " Remotely."
              ),
          })

    return matched_jobs