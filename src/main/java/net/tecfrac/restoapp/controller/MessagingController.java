package net.tecfrac.restoapp.controller;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.exception.ApiException;
import com.twilio.rest.conversations.v1.service.Conversation;
import com.twilio.rest.conversations.v1.service.conversation.Participant;
import com.twilio.security.RequestValidator;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.config.TwilioConfig;
import net.tecfrac.restoapp.service.MessagingService;
import net.tecfrac.restoapp.service.NotificationService;
import net.tecfrac.restoapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api/conversation")
public class MessagingController {

    private final TwilioConfig twilioConfig;
    private UserService userService;
    private NotificationService notificationService;
    @PostConstruct
    public void init() {
        MessagingService.loadBadWords();
    }

    @PostMapping("/pre-webhook")
    public ResponseEntity<Map<String, Object>> handlePreWebhook(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature,
            @RequestParam Map<String, String> params
    ) {
        System.out.println("Pre-Webhook Params: " + params);
        System.out.println("Twilio Signature: " + twilioSignature);

        try {
            boolean isValid = true;

            if (twilioSignature != null) {
                String url = "https://4a270059e278.ngrok-free.app/api/conversation/pre-webhook";
                RequestValidator validator = new RequestValidator(twilioConfig.getAuthToken());
                isValid = validator.validate(url, params, twilioSignature);
            } else {
                System.out.println("No Twilio signature provided, skipping validation (dev/test mode)");
            }

            if (!isValid) {
                System.out.println("Invalid Twilio signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("reason", "Invalid Twilio Signature"));
            }

            String eventType = params.get("EventType");

            switch (eventType) {
                case "onMessageAdd":
                    String body = params.get("Body");
                    String author = params.get("Author");

                    System.out.println("Incoming message from " + author + ": " + body);

                    if (body != null && !body.isBlank()) {
                        String censored = MessagingService.censorMessage(body);
                        if (!censored.equals(body)) {
                            System.out.println("Badword detected → modifying body: " + censored);
                            return ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(Map.of("body", censored));
                        }
                    }

                    break;

                case "onConversationAdd":
                    System.out.println("New conversation added: " + params.get("FriendlyName"));
                    break;

                default:
                    System.out.println("Unhandled event type: " + eventType);
            }

            return ResponseEntity.ok(Map.of());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of());
        }
    }


    @PostMapping(value = "/post-webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handlePostWebhook(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature,
            @RequestParam Map<String, String> params
    ) {
        try {
            System.out.println("Post-Webhook Params: " + params);
            System.out.println("Twilio Signature: " + twilioSignature);

            // Twilio signature validation
            boolean isValid = true;
            if (twilioSignature != null) {
                String url = "https://4a270059e278.ngrok-free.app/api/conversation/post-webhook";
                RequestValidator validator = new RequestValidator(twilioConfig.getAuthToken());
                isValid = validator.validate(url, params, twilioSignature);
            }

            if (!isValid) {
                System.out.println("Invalid Twilio Signature on post-webhook!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Event processing
            String eventType = params.get("EventType");
            switch (eventType) {
                case "onMessageAdded" -> {
                    String body = params.get("Body");
                    String convoSid = params.get("ConversationSid");
                    System.out.println("convo sid:"+convoSid);
                    String serviceSid = params.get("ChatServiceSid");
                    String author = params.get("Author");

                    System.out.println("Message sent by " + author + ": " + body);

                    Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());

                    String convoName;
                    try {
                        Conversation convo = Conversation.fetcher(serviceSid,convoSid).fetch();
                        System.out.println(convo);
                        convoName = convo.getFriendlyName();
                        System.out.println("Conversation name: " + convoName);

                    }
                     catch (ApiException e) {
                        System.out.println("Failed to fetch conversation: " + e.getMessage());
                        convoName = "a conversation";
                    }

                    ResourceSet<Participant> participants = Participant.reader(serviceSid,convoSid).read();
                    for (Participant participant : participants) {
                        String identity = participant.getIdentity();
                        if (identity != null && !identity.equals(author)) {
                            Long userId = userService.getIdByUsername(identity);
                            if (userId != null) {
                                notificationService.notifyUser(
                                        userId,
                                        "New message in " + convoName,
                                        author + " sent: \"" + body + "\" in " + convoName
                                );
                            }
                        }
                    }
                }
                case "onConversationAdded" -> {
                    String convoSid = params.get("ConversationSid");
                    System.out.println("New conversation created: " + convoSid);
                }
                case "onParticipantAdded" -> {
                    String participant = params.get("Identity");
                    System.out.println("New participant joined: " + participant);
                }
                default -> System.out.println("Unhandled event: " + eventType);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().build();
        }
    }

}
