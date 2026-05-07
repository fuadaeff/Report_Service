package org.example.report_service.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* org.example.report_service.service.*.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String correlationId = MDC.get("correlationId");
        String method = joinPoint.getSignature().toShortString();

        log.info("[{}] >>> {}", correlationId, method);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("[{}] <<< {} | time: {}ms", correlationId, method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[{}] !!! {} | error: {}", correlationId, method, e.getMessage());
            throw e;
        }
    }
}