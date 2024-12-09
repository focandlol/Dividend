package focandlol.dividends.exception.impl;

import focandlol.dividends.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ScrapingException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Override
    public String getMessage() {
        return "스크래핑이 실패하였습니다.";
    }
}
