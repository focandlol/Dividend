package focandlol.dividends.scraper;

import focandlol.dividends.exception.impl.ScrapingException;
import focandlol.dividends.model.Company;
import focandlol.dividends.model.Dividend;
import focandlol.dividends.model.ScrapedResult;
import focandlol.dividends.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    private static final String URL = "https://finance.yahoo.com/quote/%s/history/?period1=%d&period2=%d&interval=1mo&frequency=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {

        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;
            String url = String.format(URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url)
                    .timeout(15000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("class", "table yf-j5d1ld noDl");
            Element tableEle = parsingDivs.get(0);

            Element tbody;
            if (tableEle != null) {
                if (tableEle.children().size() >= 2) {
                    tbody = tableEle.children().get(1);
                } else {
                    throw new ScrapingException();
                }
            } else {
                throw new ScrapingException();
            }

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if(month < 0){
                    throw new ScrapingException();
                }

                dividends.add(Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend).build());
            }
            scrapResult.setDividend(dividends);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ScrapingException();
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Connection connection = Jsoup.connect(url)
                    .timeout(15000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

            Document document = connection.get();

            /**
             * 존재하지 않는 회사 ticker일 경우
             * 이 코드에서 IndexOutOfBoundsException이 뜬다. (yahoo finance 사이트에서 잘못된 ticker 입력시 나오는 페이지에서 2번째 h1은 없기 때문)
             * 따라서 IndexOutOfBounds catch 해서 처리 후 null 반환 -> CompanyService에서 적절한 예외 터뜨림
             */
            Element titleEle = document.getElementsByTag("h1").get(1);

            int lastIndex = titleEle.text().lastIndexOf(" ");
            String title = titleEle.text().substring(0, lastIndex);

            return Company.builder()
                    .name(title)
                    .ticker(ticker)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return null;
    }
}
