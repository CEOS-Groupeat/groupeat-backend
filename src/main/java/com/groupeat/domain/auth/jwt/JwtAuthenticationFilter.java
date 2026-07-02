package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenProvider authTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = extractAccessToken(request);

        if (accessToken != null && shouldAuthenticateWithAccessToken()) {
            authenticate(accessToken);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String accessToken) {
        try {
            AuthenticatedMember member = authTokenProvider.parseAccessToken(accessToken);
            memberRepository.findById(member.memberId())
                    .filter(foundMember -> foundMember.getMemberStatus() == MemberStatus.ACTIVE)
                    .filter(foundMember -> foundMember.getMemberType() == member.memberType())
                    .orElseThrow(() -> new IllegalStateException("활성 회원이 아닙니다."));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            member,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + member.memberType().name()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
        }
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = extractBearerToken(request);
        if (bearerToken != null) {
            return bearerToken;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private boolean shouldAuthenticateWithAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedMember);
    }
}
