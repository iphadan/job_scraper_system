from html import unescape
import re
import uuid
from typing import Any, Dict, List
from urllib.parse import urlparse
import requests
import urllib3
from framework.base_scraper import BaseScraper


class SafaricomEtScraper(BaseScraper):

  def clean_html(self, raw_html: str) -> str:
    """Removes HTML tags and unescapes text entities."""
    if not raw_html:
      return ""
    clean_text = re.sub(r"<[^>]+>", " ", raw_html)
    return " ".join(unescape(clean_text).split())

  def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
    print(
        f"📡 Dynamically scraping Safaricom ET via base URL: {self.target_url}"
    )
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    # 1. Extract base host (https://egjd.fa.us6.oraclecloud.com)
    parsed_url = urlparse(self.target_url)
    base_domain = f"{parsed_url.scheme}://{parsed_url.netloc}"

    # 2. Endpoint with mandatory expand=requisitionList parameter
    endpoint = f"{base_domain}/hcmRestApi/resources/latest/recruitingCEJobRequisitions"
    raw_query = "onlyData=true&expand=requisitionList&finder=findReqs;siteNumber=STEP,limit=100,sortBy=POSTING_DATES_DESC"
    full_url = f"{endpoint}?{raw_query}"

    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        ),
        "Accept": "application/json",
        "ora-irc-cx-userid": str(uuid.uuid4()),
    }

    try:
      response = requests.get(
          full_url, headers=headers, verify=False, timeout=15
      )

      if response.status_code != 200:
        print(
            f"❌ Safaricom ET API returned status code: {response.status_code}"
        )
        return []

      data = response.json()
      items = data.get("items", [])

      # Extract requisitionList array inside items[0]
      job_requisitions = []
      if items and isinstance(items, list):
        for item in items:
          if "requisitionList" in item:
            job_requisitions.extend(item["requisitionList"])
          elif "Title" in item or "RequisitionTitle" in item:
            job_requisitions.append(item)

      print(
          f"📊 Safaricom API returned {len(job_requisitions)} raw job"
          " requisition(s) from Oracle Cloud."
      )

    except Exception as e:
      print(f"❌ Safaricom ET API Network Error: {e}")
      return []

    matched_jobs = []
    # Clean keywords list
    valid_keywords = [kw.strip().lower() for kw in keywords if kw.strip()]

    for req in job_requisitions:
      title = (req.get("Title") or req.get("RequisitionTitle") or "").strip()
      req_id = (
          req.get("Id") or req.get("RequisitionId") or req.get("JobRequisitionId")
      )

      if not title or not req_id:
        continue

      # Get raw description and strip HTML
      raw_desc = (
          req.get("ShortDescriptionStr")
          or req.get("ExternalDescriptionStr")
          or req.get("BriefDescriptionWithHTML")
          or ""
      )
      clean_desc = (
          self.clean_html(raw_desc) or f"Vacancy at Safaricom Ethiopia: {title}"
      )

      job_url = f"{base_domain}/hcmUI/CandidateExperience/en/sites/STEP/job/{req_id}"

      # Combine title and cleaned description into a searchable text block
      searchable_text = f"{title} {clean_desc}".lower()

      # Substring match against any keywords provided
      matched_kw = [kw for kw in valid_keywords if kw in searchable_text]

      if not valid_keywords or len(matched_kw) > 0:
        print(
            f"✅ MATCH FOUND: '{title}' (Matched on keywords:"
            f" {matched_kw})"
        )
        matched_jobs.append({
            "title": title,
            "company": "Safaricom Telecommunications Ethiopia PLC",
            "url": job_url,
            "description": clean_desc[:500],
        })
      else:
        print(
            f"⏭️ SKIPPED: '{title}' - No keywords from {valid_keywords}"
            " matched."
        )

    print(f"🎯 Matched {len(matched_jobs)} job(s) total.")
    return matched_jobs