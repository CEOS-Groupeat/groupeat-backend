package com.groupeat.global.logging;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
}
