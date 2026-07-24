# # framework/scrapers/kifiya_scraper.py
# from typing import Any, Dict, List
# from bs4 import BeautifulSoup
# import requests
# import urllib3
# from framework.base_scraper import BaseScraper


# class KifiyaScraper(BaseScraper):

#   def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
#     print(f"📡 Fetching Kifiya Jobs: {self.target_url}")
#     urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

#     headers = {
#         "User-Agent": (
#             "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
#         )
#     }

#     try:
#       response = requests.get(
#           self.target_url, headers=headers, verify=False, timeout=15
#       )
#       if response.status_code != 200:
#         print(f"❌ Kifiya returned HTTP status: {response.status_code}")
#         return []
#     except Exception as e:
#       print(f"❌ Kifiya Network Error: {e}")
#       return []

#     soup = BeautifulSoup(response.text, "html.parser")
#     matched_jobs = []

#     # Parse job cards/articles on the work-with-us page
#     job_blocks = soup.find_all(
#         ["div", "article"], class_=lambda c: c and "job" in c.lower()
#     ) or soup.find_all("h3")

#     for block in soup.find_all(["h2", "h3"]):
#       title = block.text.strip()
#       if not title:
#         continue

#       parent = block.find_parent()
#       link = parent.find("a", href=True) if parent else None
#       job_url = link["href"] if link else self.target_url

#       # Match against keywords
#       if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
#         matched_jobs.append({
#             "title": title,
#             "company": "Kifiya Financial Technology",
#             "url": job_url,
#             "description": f"Kifiya Job Opening: {title}",
#         })

#     return matched_jobs

from typing import Any, Dict, List
from bs4 import BeautifulSoup
import requests
import urllib3
from framework.base_scraper import BaseScraper


class KifiyaScraper(BaseScraper):

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(f"📡 Fetching Kifiya.com Careers: {self.target_url}")
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
        print(f"❌ Kifiya returned status: {response.status_code}")
        return []
    except Exception as e:
      print(f"❌ Kifiya Network Error: {e}")
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
          "https://www.kifiya.com" + link["href"]
          if link and link["href"].startswith("/")
          else (link["href"] if link else self.target_url)
      )

      if any(kw.lower() in title.lower() for kw in keywords if kw.strip()):
        matched_jobs.append({
            "title": title,
            "company": "Kifiya Financial Technology",
            "url": job_url,
            "description": f"Vacancy at Kifiya: {title}",
        })

    return matched_jobs