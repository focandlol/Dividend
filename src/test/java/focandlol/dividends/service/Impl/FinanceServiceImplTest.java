package focandlol.dividends.service.Impl;

import focandlol.dividends.exception.impl.NoCompanyException;
import focandlol.dividends.model.Company;
import focandlol.dividends.model.ScrapedResult;
import focandlol.dividends.persist.CompanyRepository;
import focandlol.dividends.persist.DividendRepository;
import focandlol.dividends.persist.entity.CompanyEntity;
import focandlol.dividends.persist.entity.DividendEntity;
import focandlol.dividends.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FinanceServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DividendRepository dividendRepository;

    @InjectMocks
    private FinanceServiceImpl financeService;

    @Test
    void getDividendByCompanyNameSuccess(){
        DividendEntity dividendEntity = new DividendEntity(1L, 1L, LocalDateTime.now(), "1.4");
        DividendEntity dividendEntity2 = new DividendEntity(2L, 1L, LocalDateTime.now(), "2.4");

        given(companyRepository.findByName(anyString()))
                .willReturn(Optional.of(new CompanyEntity(1L, "mmm", "mmm company")));

        given(dividendRepository.findAllByCompanyId(anyLong()))
                .willReturn(Arrays.asList(dividendEntity, dividendEntity2));

        ScrapedResult result = financeService.getDividendByCompanyName("mmm company");

        assertEquals("mmm company",result.getCompany().getName());
        assertEquals("mmm",result.getCompany().getTicker());
        assertEquals("1.4",result.getDividend().get(0).getDividend());
        assertEquals("2.4",result.getDividend().get(1).getDividend());
    }

    @Test
    void getDividendByCompanyNameFailed(){
        given(companyRepository.findByName(anyString()))
                .willReturn(Optional.empty());

        NoCompanyException ex = assertThrows(NoCompanyException.class, () -> financeService.getDividendByCompanyName("mmm company"));
        assertEquals("존재하지 않는 회사명 입니다.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(),ex.getStatusCode());

    }

}