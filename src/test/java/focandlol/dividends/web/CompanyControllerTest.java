package focandlol.dividends.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.dividends.model.Company;
import focandlol.dividends.persist.entity.CompanyEntity;
import focandlol.dividends.service.Impl.CompanyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyServiceImpl companyServiceImpl;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void autoCompleteTest() throws Exception {
        given(companyServiceImpl.autocomplete(anyString()))
                .willReturn(Arrays.asList("a Company"));

        mockMvc.perform(get("/company/autocomplete")
                        .param("keyword", "a"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"READ"})
    void searchCompany() throws Exception {
        CompanyEntity company1 = new CompanyEntity(1L, "A", "company A");
        CompanyEntity company2 = new CompanyEntity(2L, "B", "company B");

        Page<CompanyEntity> mockPage = new PageImpl<>(
                List.of(company1, company2),
                PageRequest.of(0, 10),
                2
        );

        given(companyServiceImpl.getAllCompany(any()))
                .willReturn(mockPage);

        mockMvc.perform(get("/company?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("company A"))
                .andExpect(jsonPath("$.content[1].name").value("company B"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"WRITE"})
    void addCompany() throws Exception {

        Company company = new Company("MMM", "MMM company");

        given(companyServiceImpl.save(any()))
                .willReturn(company);

        mockMvc.perform(post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                company
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("MMM"))
                .andExpect(jsonPath("$.name").value("MMM company"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"WRITE"})
    void deleteCompany() throws Exception {

        given(companyServiceImpl.deleteCompany(anyString()))
                .willReturn("mmm company");

        mockMvc.perform(delete("/company/MMM"))
                .andExpect(status().isOk())
                .andExpect(content().string("mmm company"));

    }


}