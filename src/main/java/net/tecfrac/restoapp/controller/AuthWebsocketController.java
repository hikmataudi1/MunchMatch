//package net.tecfrac.restoapp.controller;
//
//import lombok.RequiredArgsConstructor;
//import net.tecfrac.restoapp.config.AuthChannelInterceptorAdapter;
//import net.tecfrac.restoapp.config.CustomHandshakeHandler;
//import net.tecfrac.restoapp.security.JwtTokenProvider;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class AuthWebsocketController {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final AuthChannelInterceptorAdapter authChannelInterceptor;
//    private final SimpMessageSendingOperations simpAccessor;
//
//    @MessageMapping("/auth")
//    public void authenticate(@Header("Authorization") String token,
//                             SimpMessageHeaderAccessor simpAccessor) {
//            StompHeaderAccessor accessor = StompHeaderAccessor.wrap((Message<?>) simpAccessor.getMessageHeaders());
//
//        if (token != null && jwtTokenProvider.validateToken(token.substring(7))) {
//            String username = jwtTokenProvider.getUsername(token.substring(7));
//            Principal authenticatedPrincipal = new CustomHandshakeHandler.UsernamePrincipal(username);
//
//            authChannelInterceptor.authenticateSession(accessor.getSessionId(), authenticatedPrincipal);
//
//            System.out.println("✅ Authenticated via message: " + username);
//        } else {
//            System.out.println("❌ Invalid token in /auth");
//        }
//    }
//
//}
