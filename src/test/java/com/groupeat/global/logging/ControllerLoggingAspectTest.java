package com.groupeat.global.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.global.apiPayload.code.status.GlobalErrorStatus;
import com.groupeat.global.exception.GeneralException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerLoggingAspectTest {

    private final ControllerLoggingAspect aspect = new ControllerLoggingAspect();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void logControllerRequest_proceedsAndCleansMdc() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("getOrders");
        when(joinPoint.proceed()).thenReturn("ok");
        setRequest("GET", "/api/orders");
        setAuthenticatedMember(1L);

        Object result = aspect.logControllerRequest(joinPoint);

        assertThat(result).isEqualTo("ok");
        assertThat(MDC.get("httpMethod")).isNull();
        assertThat(MDC.get("memberId")).isNull();
    }

    @Test
    void logControllerRequest_usesUriPatternAndRestoresPreviousMdc() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("getOrder");
        when(joinPoint.proceed()).thenReturn("ok");
        setRequest("GET", "/api/orders/1", "/api/orders/{orderId}");
        setAuthenticatedMember(1L);
        MDC.put("traceId", "trace-1");
        MDC.put("requestUri", "previous-uri");
        ListAppender<ILoggingEvent> appender = attachListAppender();

        try {
            aspect.logControllerRequest(joinPoint);

            ILoggingEvent event = appender.list.get(0);
            assertThat(event.getMDCPropertyMap())
                    .containsEntry("requestUri", "/api/orders/1")
                    .containsEntry("uriPattern", "/api/orders/{orderId}");
            assertThat(MDC.get("traceId")).isEqualTo("trace-1");
            assertThat(MDC.get("requestUri")).isEqualTo("previous-uri");
        } finally {
            detachListAppender(appender);
        }
    }

    @Test
    void logControllerRequest_rethrowsExceptionAndCleansMdc() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("getOrder");
        when(joinPoint.proceed()).thenThrow(new GeneralException(GlobalErrorStatus._BAD_REQUEST));
        setRequest("GET", "/api/orders/1");
        setAuthenticatedMember(1L);

        assertThatThrownBy(() -> aspect.logControllerRequest(joinPoint))
                .isInstanceOf(GeneralException.class);
        assertThat(MDC.get("success")).isNull();
        assertThat(MDC.get("exceptionType")).isNull();
    }

    private ProceedingJoinPoint joinPoint(String methodName) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getDeclaringType()).thenReturn(TestController.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        return joinPoint;
    }

    private void setRequest(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setRequest(String method, String uri, String uriPattern) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, uriPattern);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setAuthenticatedMember(Long memberId) {
        AuthenticatedMember member = new AuthenticatedMember(
                memberId,
                MemberType.CUSTOMER,
                MemberStatus.ACTIVE,
                false
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(member, null, List.of())
        );
    }

    private static class TestController {
    }

    private ListAppender<ILoggingEvent> attachListAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ControllerLoggingAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachListAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(ControllerLoggingAspect.class);
        logger.detachAppender(appender);
    }
}
