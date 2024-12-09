package focandlol.dividends.service;

import focandlol.dividends.model.ScrapedResult;

public interface FinanceService {
    ScrapedResult getDividendByCompanyName(String companyName);
}
