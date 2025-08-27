
package net.tecfrac.restoapp.config;

import net.tecfrac.restoapp.repository.UserRepository;
import net.tecfrac.restoapp.security.JwtTokenProvider;
import net.tecfrac.restoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
public class    CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];
            if (token.contains("&")) {
                token = token.split("&")[0];
            }
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsername(token);
                return new UsernamePrincipal(username);
            }
        }
        return null;
    }

    public static class UsernamePrincipal implements Principal {
        private final String name;

        public UsernamePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

}