import requests
from typing import List, Dict, Any
from framework.base_scraper import BaseScraper

class RemoteOkScraper(BaseScraper):
    def scrape(self, keywords: List[str]) -> List[Dict[str, Any]]:
        print(f"📡 Querying RemoteOK JSON endpoint: {self.target_url}")
        
        response = requests.get(self.target_url, headers=self.headers, timeout=15)
        if response.status_code != 200:
            print(f"❌ RemoteOK returned HTTP error: {response.status_code}")
            return []

        raw_jobs = response.json()
        if not isinstance(raw_jobs, list) or len(raw_jobs) <= 1:
            return []

        matched_jobs = []
        for raw_job in raw_jobs[1:]:
            title = raw_job.get("position", "").lower()
            description = raw_job.get("description", "").lower()
            tags = [tag.lower() for tag in raw_job.get("tags", [])]

            # Filter locally using requested keywords
            if any(kw.lower() in title or kw.lower() in description or kw.lower() in tags for kw in keywords):
                matched_jobs.append({
                    "title": raw_job.get("position"),
                    "company": raw_job.get("company"),
                    "url": f"https://remoteok.com/l/{raw_job.get('id')}",
                    "description": raw_job.get("description", "")[:500] + "..." # Truncate description
                })
        return matched_jobs