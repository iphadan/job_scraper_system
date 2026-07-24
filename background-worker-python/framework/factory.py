from typing import Type, Dict
from framework.base_scraper import BaseScraper
from scrapers.remoteok import RemoteOkScraper
from scrapers.wwr import WwrScraper
from scrapers.djini import DjiniScraper
from scrapers.kifiya_scraper import KifiyaScraper
from scrapers.eaglelion_scraper import EagleLionScraper
from scrapers.safaricom_et_scraper import SafaricomEtScraper
class ScraperFactory:
    _registry: Dict[str, Type[BaseScraper]] = {
        "REMOTEOK": RemoteOkScraper,
        "WWR": WwrScraper,
         "DJINI": DjiniScraper,
         "KIFIYA": KifiyaScraper,
         "EAGLELION": EagleLionScraper,
         "SAFARICOM_ET" : SafaricomEtScraper
    }

    @classmethod
    def get_scraper(cls, site_code: str, target_url: str) -> BaseScraper:
        scraper_class = cls._registry.get(site_code.upper())
        if not scraper_class:
            raise ValueError(f"No registered Python scraping class found for engine: {site_code}")
        return scraper_class(target_url)