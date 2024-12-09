package focandlol.dividends.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {

    private Company company;

    private List<Dividend> dividend;

    public ScrapedResult(){
        this.dividend = new ArrayList<Dividend>();
    }
}
