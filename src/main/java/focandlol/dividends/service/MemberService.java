package focandlol.dividends.service;

import focandlol.dividends.model.Auth;
import focandlol.dividends.persist.entity.MemberEntity;

public interface MemberService {
    MemberEntity register(Auth.SignUp member);
    MemberEntity authenticate(Auth.SignIn member);
}
