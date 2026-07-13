package com.groupeat;

import com.groupeat.domain.auth.config.AuthCookieProperties;
import com.groupeat.domain.auth.config.OAuth2RedirectProperties;
import com.groupeat.domain.business.config.NtsApiProperties;
import com.groupeat.domain.notification.config.FirebaseProperties;
import com.groupeat.domain.payment.config.TossPaymentProperties;
import com.groupeat.domain.settlement.config.SettlementProperties;
import com.groupeat.global.config.CorsProperties;
import com.groupeat.global.upload.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({
		OAuth2RedirectProperties.class,
		AuthCookieProperties.class,
		CorsProperties.class,
		TossPaymentProperties.class,
		SettlementProperties.class,
        NtsApiProperties.class,
		S3Properties.class,
		FirebaseProperties.class
})
public class GroupeatBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupeatBackendApplication.class, args);
	}

}
