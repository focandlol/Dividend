package focandlol.dividends.service.Impl;

import focandlol.dividends.exception.impl.AlreadyExistCompanyException;
import focandlol.dividends.exception.impl.NoCompanyException;
import focandlol.dividends.exception.impl.NoTickerException;
import focandlol.dividends.model.Company;
import focandlol.dividends.model.Dividend;
import focandlol.dividends.model.ScrapedResult;
import focandlol.dividends.persist.CompanyRepository;
import focandlol.dividends.persist.DividendRepository;
import focandlol.dividends.persist.entity.CompanyEntity;
import focandlol.dividends.persist.entity.DividendEntity;
import focandlol.dividends.scraper.YahooFinanceScraper;
import focandlol.dividends.service.CompanyService;
import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private YahooFinanceScraper yahooFinanceScraper;

    @Mock
    private Trie trie;

    @InjectMocks
    @Spy
    private CompanyServiceImpl companyServiceImpl;


    @Test
    void saveSuccess(){
        ScrapedResult mockResult = new ScrapedResult();
        mockResult.setCompany(new Company("mmm","mmm company"));
        mockResult.setDividend(List.of(
                new Dividend(LocalDateTime.now(), "1.23"),
                new Dividend(LocalDateTime.now(), "1.11")
        ));

        given(companyRepository.existsByTicker(anyString()))
                .willReturn(false);

        given(yahooFinanceScraper.scrapCompanyByTicker(anyString()))
                .willReturn(new Company("mmm","mmm company"));

        given(yahooFinanceScraper.scrap(any()))
                .willReturn(mockResult);

        given(companyRepository.save(any()))
        .willReturn(new CompanyEntity(1L,"mmm","mmm company"));

        ArgumentCaptor<CompanyEntity> captor = ArgumentCaptor.forClass(CompanyEntity.class);
        ArgumentCaptor<List<DividendEntity>> captorDividend = ArgumentCaptor.forClass(List.class);

        Company save = companyServiceImpl.save("mmm");


        verify(companyRepository,times(1)).save(captor.capture());
        verify(dividendRepository,times(1)).saveAll(captorDividend.capture());

        assertEquals("mmm",captor.getValue().getTicker());
        assertEquals("mmm company",captor.getValue().getName());
        assertEquals("mmm",save.getTicker());
        assertEquals("mmm company",save.getName());
        assertEquals(2,captorDividend.getValue().size());
        assertEquals("1.23",captorDividend.getValue().get(0).getDividend());
        assertEquals("1.11",captorDividend.getValue().get(1).getDividend());
    }

    @Test
    void saveFailAlreadyExistCompanyException(){

        given(companyRepository.existsByTicker(anyString()))
                .willReturn(true);

        AlreadyExistCompanyException ex = assertThrows(AlreadyExistCompanyException.class, () -> companyServiceImpl.save("mmm"));
        assertEquals(HttpStatus.BAD_REQUEST.value(),ex.getStatusCode());
        assertEquals("이미 보유하고 있는 회사 정보 입니다",ex.getMessage());
    }

    @Test
    void saveFailNoTickerException(){

        given(companyRepository.existsByTicker(anyString()))
                .willReturn(false);

        given(yahooFinanceScraper.scrapCompanyByTicker(anyString()))
                .willReturn(null);

        NoTickerException ex = assertThrows(NoTickerException.class, () -> companyServiceImpl.save("mmm"));
        assertEquals(HttpStatus.BAD_REQUEST.value(),ex.getStatusCode());
        assertEquals("존재하지 않는 ticker 입니다.",ex.getMessage());
    }

    @Test
    void getAllCompany(){
        CompanyEntity company1 = new CompanyEntity(1L, "A", "company A");
        CompanyEntity company2 = new CompanyEntity(2L, "B", "company B");

        Page<CompanyEntity> mockPage = new PageImpl<>(
                List.of(company1, company2),
                PageRequest.of(0, 10),
                2
        );
        given(companyRepository.findAll(PageRequest.of(0, 2)))
                .willReturn(mockPage);

        Page<CompanyEntity> companies = companyServiceImpl.getAllCompany(PageRequest.of(0, 2));

        assertEquals(2, companies.getTotalElements());
        assertEquals(1L, companies.getContent().get(0).getId());
        assertEquals(2L, companies.getContent().get(1).getId());
        assertEquals("company A", companies.getContent().get(0).getName());
        assertEquals("company B", companies.getContent().get(1).getName());
        assertEquals("A", companies.getContent().get(0).getTicker());
        assertEquals("B", companies.getContent().get(1).getTicker());
    }

    @Test
    void deleteSuccess(){
        given(companyRepository.findByTicker(anyString()))
                .willReturn(Optional.of(new CompanyEntity(1L,"mmm","mmm company")));

        String name = companyServiceImpl.deleteCompany("mmm");
        verify(companyRepository,times(1)).delete(any());
        verify(dividendRepository,times(1)).deleteAllByCompanyId(any());
        verify(companyRepository,times(1)).findByTicker(anyString());
        assertEquals("mmm company",name);
    }

    @Test
    void deleteFailed(){
        given(companyRepository.findByTicker(anyString()))
                .willReturn(Optional.empty());

        NoCompanyException ex = assertThrows(NoCompanyException.class, () -> companyServiceImpl.deleteCompany(anyString()));
        assertEquals(HttpStatus.BAD_REQUEST.value(),ex.getStatusCode());
        assertEquals("존재하지 않는 회사명 입니다.",ex.getMessage());
    }

    @Test
    void autoComplete(){
        SortedMap<String, Object> mockPrefixMap = new TreeMap<>();
        mockPrefixMap.put("a company", null);
        mockPrefixMap.put("ABC Mart", null);

        // Mock 동작 정의
        given(trie.prefixMap("a")).willReturn(mockPrefixMap);

        List<String> list = companyServiceImpl.autocomplete("a");

        assertEquals("ABC Mart",list.get(0));
        assertEquals("a company",list.get(1));
    }

}