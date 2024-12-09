package focandlol.dividends.service.Impl;

import focandlol.dividends.exception.impl.AlreadyExistCompanyException;
import focandlol.dividends.exception.impl.NoCompanyException;
import focandlol.dividends.exception.impl.NoTickerException;
import focandlol.dividends.model.Company;
import focandlol.dividends.model.ScrapedResult;
import focandlol.dividends.persist.CompanyRepository;
import focandlol.dividends.persist.DividendRepository;
import focandlol.dividends.persist.entity.CompanyEntity;
import focandlol.dividends.persist.entity.DividendEntity;
import focandlol.dividends.scraper.Scraper;
import focandlol.dividends.service.CompanyService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;
    private final CompanyRepository companyRepository;

    @Override
    public Company save(String ticker){
        boolean exists = companyRepository.existsByTicker(ticker);
        if(exists){
            throw new AlreadyExistCompanyException();
        }
        ScrapedResult scrapedResult = storeCompanyAndDividend(ticker);
        saveScrapedResult(scrapedResult);

        return scrapedResult.getCompany();
    }

    private ScrapedResult storeCompanyAndDividend(String ticker){
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new NoTickerException();
        }

        return yahooFinanceScraper.scrap(company);
    }

    @Transactional
    public void saveScrapedResult(ScrapedResult scrapedResult){
        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(scrapedResult.getCompany()));
        List<DividendEntity> dividendEntities = scrapedResult.getDividend().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        dividendRepository.saveAll(dividendEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return companyRepository.findAll(pageable);
    }


    /**
     * 자동 완성 db 사용 방법
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getCompanyNamesByKeyword(String keyword){
        PageRequest limit = PageRequest.of(0, 10, Sort.by("name").descending());
        return companyRepository.findByNameStartingWithIgnoreCase(keyword,limit).stream()
                .map(a -> a.getName()).collect(Collectors.toList());
    }


    /**
     * 자동 완성 trie 사용 방법
     */
    @Override
    public List<String> autocomplete(String keyword){
        return (List<String>) trie.prefixMap(keyword).keySet().stream().limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 자동 완성 trie 사용 방법
     */
    @Override
    public void addAutocompleteKeyword(String keyword){
        trie.put(keyword,null);
    }

    private void deleteAutocompleteKeyword(String keyword){
        trie.remove(keyword);
    }

    @Override
    @Transactional
    public String deleteCompany(String ticker){
        CompanyEntity company = companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        dividendRepository.deleteAllByCompanyId(company.getId());
        companyRepository.delete(company);

        deleteAutocompleteKeyword(company.getName());
        return company.getName();
    }
}
