package focandlol.dividends.exception.impl;

import focandlol.dividends.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistCompanyException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 보유하고 있는 회사 정보 입니다";
    }
}
