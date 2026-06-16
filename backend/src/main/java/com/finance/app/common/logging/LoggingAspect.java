package com.finance.app.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.finance.app..*Service.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        if (log.isDebugEnabled()) {
            log.debug("→ {}.{}({})", className, methodName,
                    Arrays.toString(joinPoint.getArgs()));
        } else {
            log.info("→ {}.{}", className, methodName);
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (log.isDebugEnabled()) {
                log.debug("← {}.{} → {} ({}ms)", className, methodName, result, elapsed);
            } else {
                log.info("← {}.{} ({}ms)", className, methodName, elapsed);
            }
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("✕ {}.{} failed after {}ms", className, methodName, elapsed, e);
            throw e;
        }
    }

    @Around("execution(* com.finance.app..*Controller.*(..))")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.info("→ {}.{}", className, methodName);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("← {}.{} ({}) ({}ms)", className, methodName,
                    result != null ? result.getClass().getSimpleName() : "void", elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("✕ {}.{} failed after {}ms", className, methodName, elapsed, e);
            throw e;
        }
    }
}
