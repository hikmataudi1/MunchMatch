package net.tecfrac.restoapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@EnableScheduling
@SpringBootApplication
public class RestoappApplication {

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

	public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.load();
//        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
//        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//        System.setProperty("spring.datasource.url", dotenv.get("DB_URL"));
//        System.setProperty("spring.datasource.username", dotenv.get("DB_USERNAME"));
//        System.setProperty("spring.datasource.password", dotenv.get("DB_PASSWORD"));
//
//        System.setProperty("app.jwt-secret", dotenv.get("JWT_SECRET"));
//        System.setProperty("app.jwt-expiration-milliseconds", dotenv.get("JWT_EXPIRATION"));
//
//        System.setProperty("twilio.accountSid", dotenv.get("TWILIO_ACCOUNT_SID"));
//        System.setProperty("twilio.apiKeySid", dotenv.get("TWILIO_API_KEY"));
//        System.setProperty("twilio.apiKeySecret", dotenv.get("TWILIO_API_SECRET"));


        SpringApplication.run(RestoappApplication.class, args);
		System.out.println("Server Started");
	}

}
