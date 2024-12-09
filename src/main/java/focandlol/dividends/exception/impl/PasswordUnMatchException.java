package focandlol.dividends.exception.impl;

import focandlol.dividends.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class PasswordUnMatchException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "패스워드가 옳지 않습니다";
    }
}
