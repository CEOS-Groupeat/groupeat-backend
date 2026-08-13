package com.groupeat.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupeat.global.apiPayload.ApiResponse;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import com.groupeat.global.apiPayload.code.status.GlobalErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ErrorReasonDTO errorReason = GlobalErrorStatus._UNAUTHORIZED.getReasonHttpStatus();

        response.setStatus(errorReason.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.onFailure(errorReason.getCode(), errorReason.getMessage(), null)
        ));
    }
}
