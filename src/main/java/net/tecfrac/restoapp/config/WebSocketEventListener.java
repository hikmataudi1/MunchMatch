package net.tecfrac.restoapp.config;

import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.security.JwtTokenProvider;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
@AllArgsConstructor
@Component
public class WebSocketEventListener {
    AuthChannelInterceptorAdapter authChannelInterceptor;
    JwtTokenProvider jwtTokenProvider;
    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String token = accessor.getFirstNativeHeader("Authorization");
        System.out.println("🔌 Client connected: sessionId = " + sessionId);
        if (token != null) {
            System.out.println("👤 token: " + token);
            Principal principal = determineUser(token);
            accessor.setUser(principal);
            authChannelInterceptor.authenticateSession(sessionId, principal);
            System.out.println();
        }else{
            System.out.println("token is null");
        }
    }
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        System.out.println("❌ Client disconnected: sessionId = " + sessionId);

        authChannelInterceptor.removeSession(sessionId);

    }

    protected Principal determineUser(String token) {
            System.out.println("token: " + token);

            if (jwtTokenProvider.validateToken(token.substring(7))) {
                System.out.println("token  valid");
                System.out.println();
                String username = jwtTokenProvider.getUsername(token.substring(7));
                System.out.println("username: " + username);

                return new CustomHandshakeHandler.UsernamePrincipal(username);
            }
        return null;
    }

}
