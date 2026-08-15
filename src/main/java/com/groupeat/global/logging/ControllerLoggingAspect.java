package com.groupeat.global.logging;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final String UNKNOWN_USER = "anonymous";
    private static final String NO_EXCEPTION = "none";
    private static final String UNKNOWN_REQUEST_VALUE = "UNKNOWN";

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object logControllerRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        Map<String, String> previousMdc = MDC.getCopyOfContextMap();

        try {
            Object result = joinPoint.proceed();
            putMdc(joinPoint, elapsedMs(startTime), true, NO_EXCEPTION);
            log.info("Controller request completed");
            return result;
        } catch (Throwable throwable) {
            putMdc(joinPoint, elapsedMs(startTime), false, throwable.getClass().getSimpleName());
            logFailure(throwable);
            throw throwable;
        } finally {
            restoreMdc(previousMdc);
        }
    }

    private void putMdc(
            ProceedingJoinPoint joinPoint,
            long elapsedMs,
            boolean success,
            String exceptionType
    ) {
        HttpServletRequest request = currentRequest();

        MDC.put("httpMethod", request == null ? UNKNOWN_REQUEST_VALUE : request.getMethod());
        MDC.put("requestUri", request == null ? UNKNOWN_REQUEST_VALUE : request.getRequestURI());
        MDC.put("uriPattern", uriPattern(request));
        MDC.put("memberId", currentMemberId());
        MDC.put("controller", joinPoint.getSignature().getDeclaringType().getSimpleName());
        MDC.put("controllerMethod", joinPoint.getSignature().getName());
        MDC.put("elapsedMs", String.valueOf(elapsedMs));
        MDC.put("success", String.valueOf(success));
        MDC.put("exceptionType", exceptionType);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String uriPattern(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_REQUEST_VALUE;
        }

        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null) {
            return pattern.toString();
        }

        return request.getRequestURI();
    }

    private String currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return UNKNOWN_USER;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedMember member) {
            return String.valueOf(member.memberId());
        }
        if (principal instanceof Long memberId) {
            return String.valueOf(memberId);
        }

        return UNKNOWN_USER;
    }

    private long elapsedMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private void logFailure(Throwable throwable) {
        if (isExpectedException(throwable)) {
            log.warn("Controller request failed");
            return;
        }

        log.error("Controller request failed");
    }

    private boolean isExpectedException(Throwable throwable) {
        if (throwable instanceof GeneralException exception) {
            return exception.getErrorReasonHttpStatus().getHttpStatus().is4xxClientError();
        }

        if (throwable instanceof MethodArgumentNotValidException
                || throwable instanceof ConstraintViolationException
                || throwable instanceof BindException
                || throwable instanceof MethodArgumentTypeMismatchException
                || throwable instanceof AccessDeniedException) {
            return true;
        }

        if (throwable instanceof ErrorResponseException exception) {
            return exception.getStatusCode().is4xxClientError();
        }

        if (throwable instanceof ResponseStatusException exception) {
            return exception.getStatusCode().is4xxClientError();
        }

        return false;
    }

    private void restoreMdc(Map<String, String> previousMdc) {
        if (previousMdc == null) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(previousMdc);
    }
}
