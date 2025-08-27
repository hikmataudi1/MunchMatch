package net.tecfrac.restoapp.controller;

import com.twilio.security.RequestValidator;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Say;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.config.TwilioConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/voice")
public class CallController {
    private TwilioConfig twilioConfig;
    private static final Logger log = LoggerFactory.getLogger(CallController.class);

    @PostMapping(produces = "application/xml")
    public ResponseEntity<String> handleCall(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature,
            @RequestParam Map<String, String> params
    ) {
        String to = params.get("to");
        String from = params.get("From");

        log.info("Incoming call webhook:");
        log.info("From: {}", from);
        log.info("To: {}", to);
        log.info("All params: {}", params);

        try {
            boolean isValid = true;

            if (twilioSignature != null) {
                String url = "https://munchmatch.up.railway.app/api/voice";

                RequestValidator validator = new RequestValidator(twilioConfig.getAuthToken());
                isValid = validator.validate(url, params, twilioSignature);
            } else {
                log.warn("⚠️ No Twilio signature provided → skipping validation (dev/test mode)");
            }

            if (!isValid) {
                log.error("❌ Invalid Twilio signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("<Response><Say>Unauthorized request</Say></Response>");
            }

            VoiceResponse.Builder responseBuilder = new VoiceResponse.Builder();

            if (to == null || to.isEmpty()) {
                responseBuilder.say(new Say.Builder("No user specified to call. Goodbye!").build());
            } else {
                Dial dial = new Dial.Builder()
                        .client(new Client.Builder(to).build())
                        .build();
                responseBuilder.dial(dial);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(responseBuilder.build().toXml());

        } catch (Exception e) {
            log.error("Error building TwiML", e);
            return ResponseEntity.ok("<Response></Response>");
        }
    }

}