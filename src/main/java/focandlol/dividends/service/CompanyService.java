package focandlol.dividends.service;

import focandlol.dividends.model.Company;
import focandlol.dividends.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanyService {

    Company save(String ticker);
    Page<CompanyEntity> getAllCompany(Pageable pageable);
    List<String> getCompanyNamesByKeyword(String keyword);
    List<String> autocomplete(String keyword);
    void addAutocompleteKeyword(String keyword);
    String deleteCompany(String ticker);
}
