package focandlol.dividends.scraper;

import focandlol.dividends.model.Company;
import focandlol.dividends.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
