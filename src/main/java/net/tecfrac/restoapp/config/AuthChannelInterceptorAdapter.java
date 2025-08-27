package net.tecfrac.restoapp.config;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// # ignore # //
@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    private final Map<String, Principal> authenticatedSessions = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String sessionId = accessor.getSessionId();
        Principal stored = authenticatedSessions.get(sessionId);
        if (stored != null) {
            accessor.setUser(stored);
        }
        return message;
    }


    public void authenticateSession(String sessionId, Principal principal) {
        authenticatedSessions.put(sessionId, principal);
    }

    public void removeSession(String sessionId) {
        authenticatedSessions.remove(sessionId);
    }

    public boolean isAuthenticated(String sessionId) {
        return authenticatedSessions.containsKey(sessionId);
    }
}
