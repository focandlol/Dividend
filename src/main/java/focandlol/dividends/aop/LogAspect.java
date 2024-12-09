package focandlol.dividends.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogAspect {
    @Pointcut("execution(* focandlol.dividends..*(..)) && !execution(* focandlol.dividends.security..*(..))")
    private void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info(joinPoint.getTarget() + "-> started to " + joinPoint.getSignature().getName());

        Object proceed = joinPoint.proceed();

        log.info(joinPoint.getTarget() + "-> end to " + joinPoint.getSignature().getName());

        return proceed;
    }
}
