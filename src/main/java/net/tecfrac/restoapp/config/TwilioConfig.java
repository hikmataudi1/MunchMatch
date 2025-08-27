// src/main/java/com/example/twilio/VoiceConfig.java
package net.tecfrac.restoapp.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Getter
@Setter
public class TwilioConfig {
    private String accountSid;
    private String apiKeySid;
    private String apiKeySecret;
    private String twimlAppSid;
    private String serviceSid;
    private String authToken;
    private String chatServiceSid;
    private int tokenTtlSeconds = 86400;
}
