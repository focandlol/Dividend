package focandlol.dividends.web;


import focandlol.dividends.model.Company;
import focandlol.dividends.model.Dividend;
import focandlol.dividends.model.ScrapedResult;
import focandlol.dividends.service.Impl.FinanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinanceServiceImpl financeServiceImpl; // Service 계층을 Mocking

    @Test
    void testSearchFinance() throws Exception {

        ScrapedResult mockResult = new ScrapedResult();
        mockResult.setCompany(new Company("mmm","mmm company"));
        mockResult.setDividend(List.of(
                new Dividend(LocalDateTime.now(), "1.23"),
                new Dividend(LocalDateTime.now(), "1.11")
        ));

        // Mock 동작 정의
        given(financeServiceImpl.getDividendByCompanyName(anyString()))
                .willReturn(mockResult);

        // MockMvc를 사용해 요청 및 응답 검증
        mockMvc.perform(get("/finance/dividend/mmm company"))
                .andExpect(status().isOk()) // 상태 코드 200 검증
                .andExpect(jsonPath("$.company.ticker").value("mmm"))
                .andExpect(jsonPath("$.company.name").value("mmm company"))
                .andExpect(jsonPath("$.dividend[0].dividend").value(1.23)) // 첫 번째 dividend 금액 확인
                .andExpect(jsonPath("$.dividend[1].dividend").value(1.11)); // 두 번째 dividend 금액 확인
    }
}