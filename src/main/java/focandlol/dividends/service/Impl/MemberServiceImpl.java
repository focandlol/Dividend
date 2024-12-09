package focandlol.dividends.service.Impl;

import focandlol.dividends.exception.impl.AlreadyExistUserException;
import focandlol.dividends.exception.impl.NoUserException;
import focandlol.dividends.exception.impl.PasswordUnMatchException;
import focandlol.dividends.model.Auth;
import focandlol.dividends.persist.entity.MemberEntity;
import focandlol.dividends.persist.MemberRepository;
import focandlol.dividends.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements UserDetailsService, MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    @Override
    @Transactional
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = memberRepository.existsByUsername(member.getUsername());
        if(exists){
            throw new AlreadyExistUserException();
        }

        member.setPassword(passwordEncoder.encode(member.getPassword()));
        MemberEntity result = memberRepository.save(member.toEntity());

        return result;
    }

    @Override
    public MemberEntity authenticate(Auth.SignIn member){
        MemberEntity user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoUserException());

        if(!passwordEncoder.matches(member.getPassword(),user.getPassword())){
            throw new PasswordUnMatchException();
        }

        return user;
    }
}
