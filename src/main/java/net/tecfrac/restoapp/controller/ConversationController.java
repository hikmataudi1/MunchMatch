//package net.tecfrac.restoapp.controller;
//
//import com.twilio.Twilio;
//import com.twilio.exception.ApiException;
//import com.twilio.rest.conversations.v1.Conversation;
//import com.twilio.rest.conversations.v1.conversation.Participant;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.Setter;
//import net.tecfrac.restoapp.config.TwilioConfig;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.StreamSupport;
//
//
//@CrossOrigin("*")
//@RestController
//@Getter
//@Setter
//@AllArgsConstructor
//@RequestMapping("/api/conversation")
//public class ConversationController {
//    private final TwilioConfig twilioConfig;
//    private void initTwilio() {
//        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createConversation(@RequestBody ConversationRequestDto request, Principal principal) {
//    try {
//        initTwilio();
//        String uniqueName = "conv-" + UUID.randomUUID();
//
//        Conversation conversation = Conversation.creator()
//                .setUniqueName(uniqueName)
//                .setFriendlyName(
//                        request.getName() != null ? request.getName() : uniqueName
//                )
//                .create();
//
//        Participant.creator(conversation.getSid())
//                .setIdentity(principal.getName())
//                .create();
//
//        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
//            for (String user : request.getParticipants()) {
//                boolean alreadyIn = StreamSupport.stream(
//                                Participant.reader(conversation.getSid()).read().spliterator(), false
//                        )
//                        .anyMatch(p -> user.equals(p.getIdentity()));
//                if (!alreadyIn) {
//                    Participant.creator(conversation.getSid())
//                            .setIdentity(user)
//                            .create();
//                }
//            }
//        }
//        return ResponseEntity.ok(Map.of(
//                "conversationSid", conversation.getSid(),
//                "uniqueName", uniqueName,
//                "friendlyName", conversation.getFriendlyName(),
//                "participantsAdded", request.getParticipants() != null ? request.getParticipants() : List.of()
//        ));
//    } catch (ApiException e) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(Map.of("error", e.getMessage()));
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", e.getMessage()));
//    }
//}
//    @PostMapping("/add_participant")
//    public ResponseEntity<?> addParticipant(@RequestBody ParticipantRequestDto request) {
//        try {
//            Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
//
//            boolean alreadyIn = StreamSupport.stream(
//                            Participant.reader(request.getConversationSid()).read().spliterator(), false
//                    )
//                    .anyMatch(p -> request.getIdentity().equals(p.getIdentity()));
//
//            if (!alreadyIn) {
//                Participant.creator(request.getConversationSid())
//                        .setIdentity(request.getIdentity())
//                        .create();
//                return ResponseEntity.ok(Map.of(
//                        "conversationSid", request.getConversationSid(),
//                        "identityAdded", request.getIdentity()
//                ));
//            } else {
//                return ResponseEntity.ok(Map.of(
//                        "conversationSid", request.getConversationSid(),
//                        "message", "Participant already exists"
//                ));
//            }
//        } catch (ApiException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//    @PostMapping("/remove_participant")
//    public ResponseEntity<?> removeParticipant(@RequestBody ParticipantRequestDto request) {
//        try {
//            initTwilio();
//
//            Participant participant = StreamSupport.stream(
//                            Participant.reader(request.getConversationSid()).read().spliterator(), false
//                    )
//                    .filter(p -> request.getIdentity().equals(p.getIdentity()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (participant != null) {
//                Participant.deleter(request.getConversationSid(), participant.getSid()).delete();
//                return ResponseEntity.ok(Map.of(
//                        "conversationSid", request.getConversationSid(),
//                        "identityRemoved", request.getIdentity()
//                ));
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of(
//                                "conversationSid", request.getConversationSid(),
//                                "message", "Participant not found"
//                        ));
//            }
//        } catch (ApiException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//    @GetMapping("/list_participants")
//    public ResponseEntity<?> listParticipants(@RequestParam String conversationSid)  {
//        try {
//            initTwilio();
//            var participants = StreamSupport.stream(
//                            Participant.reader(conversationSid).read().spliterator(), false
//                    )
//                    .map(p -> Map.of(
//                            "identity", p.getIdentity(),
//                            "sid", p.getSid()
//                    ))
//                    .toList();
//
//            return ResponseEntity.ok(Map.of(
//                    "conversationSid", conversationSid,
//                    "participants", participants
//            ));
//        } catch (ApiException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//    @GetMapping("/my_conversations")
//    public ResponseEntity<?> getMyConversations(Principal principal) {
//        long startTotal = System.currentTimeMillis();
//        try {
//            long startInit = System.currentTimeMillis();
//            initTwilio();
//            long endInit = System.currentTimeMillis();
//            System.out.println("Twilio init time: " + (endInit - startInit) + " ms");
//
//            String identity = principal.getName();
//
//            long startLogic = System.currentTimeMillis();
//            var conversations = StreamSupport.stream(
//                            Conversation.reader().read().spliterator(), false
//                    )
//                    .filter(conv -> StreamSupport.stream(
//                            Participant.reader(conv.getSid()).read().spliterator(), false
//                    ).anyMatch(p -> identity.equals(p.getIdentity())))
//                    .map(conv -> Map.of(
//                            "conversationSid", conv.getSid(),
//                            "name", conv.getFriendlyName() != null ? conv.getFriendlyName() : conv.getUniqueName()
//                    ))
//                    .toList();
//            long endLogic = System.currentTimeMillis();
//            System.out.println("Conversations processing time: " + (endLogic - startLogic) + " ms");
//
//            long endTotal = System.currentTimeMillis();
//            System.out.println("Total getMyConversations time: " + (endTotal - startTotal) + " ms");
//
//            return ResponseEntity.ok(conversations);
//        } catch (Exception e) {
//            long endTotal = System.currentTimeMillis();
//            System.out.println("Total getMyConversations time (exception): " + (endTotal - startTotal) + " ms");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//}
