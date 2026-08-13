package com.groupeat;

import com.groupeat.domain.auth.config.AuthCookieProperties;
import com.groupeat.domain.auth.config.AuthTokenProperties;
import com.groupeat.domain.business.config.BusinessValidationProperties;
import com.groupeat.domain.business.config.BusinessValidationTokenProperties;
import com.groupeat.domain.auth.config.OAuth2RedirectProperties;
import com.groupeat.domain.business.config.NtsApiProperties;
import com.groupeat.domain.notification.config.FirebaseProperties;
import com.groupeat.domain.notification.config.NotificationSchedulerProperties;
import com.groupeat.domain.orders.config.OrderProperties;
import com.groupeat.domain.orders.config.OrderSchedulerProperties;
import com.groupeat.domain.payment.config.TossPaymentProperties;
import com.groupeat.domain.settlement.config.SettlementProperties;
import com.groupeat.global.config.AppTimeZoneProperties;
import com.groupeat.global.config.CorsProperties;
import com.groupeat.global.upload.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({
		OAuth2RedirectProperties.class,
		AuthCookieProperties.class,
		AuthTokenProperties.class,
		BusinessValidationProperties.class,
		BusinessValidationTokenProperties.class,
		AppTimeZoneProperties.class,
		CorsProperties.class,
		TossPaymentProperties.class,
		SettlementProperties.class,
		NtsApiProperties.class,
		S3Properties.class,
		FirebaseProperties.class,
		NotificationSchedulerProperties.class,
		OrderProperties.class,
		OrderSchedulerProperties.class
})
public class GroupeatBackendApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(GroupeatBackendApplication.class);
		application.addInitializers(context -> {
			String timeZone = context.getEnvironment().getProperty("app.time-zone", "Asia/Seoul");
			TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		});
		application.run(args);
	}

}
