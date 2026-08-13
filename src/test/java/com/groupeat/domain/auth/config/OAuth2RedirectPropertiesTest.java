package com.groupeat.domain.auth.config;

import com.groupeat.domain.member.enums.MemberType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2RedirectPropertiesTest {

    private final OAuth2RedirectProperties properties = new OAuth2RedirectProperties(
            "http://localhost:3000",
            "/customer/home",
            "/owner/home",
            "/signup",
            "/signup/customer",
            "/signup/business"
    );

    @Test
    void signupInProgressUrl_returnsBusinessSignupPathForBusinessMember() {
        assertThat(properties.signupInProgressUrl(MemberType.BUSINESS))
                .isEqualTo("http://localhost:3000/signup/business");
    }

    @Test
    void signupInProgressUrl_returnsCustomerSignupPathForCustomerMember() {
        assertThat(properties.signupInProgressUrl(MemberType.CUSTOMER))
                .isEqualTo("http://localhost:3000/signup/customer");
    }
}
