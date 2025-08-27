package net.tecfrac.restoapp.service;
import com.twilio.Twilio;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.rest.chat.v2.service.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.config.TwilioConfig;
import net.tecfrac.restoapp.dto.AuthResponseDto;
import net.tecfrac.restoapp.dto.LoginDto;
import net.tecfrac.restoapp.dto.RegisterDto;
import net.tecfrac.restoapp.entity.SessionTokenEntity;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.exception.ApiException;
import net.tecfrac.restoapp.repository.SessionTokenRepository;
import net.tecfrac.restoapp.repository.UserRepository;
import net.tecfrac.restoapp.security.JwtAuthenticationFilter;
import net.tecfrac.restoapp.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class AuthService    {
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    public SessionTokenRepository sessionTokenRepository;
    private JwtAuthenticationFilter  jwtAuthenticationFilter;
    private TwilioConfig twilioConfig;



    @Transactional
    public AuthResponseDto login(LoginDto loginDto){
        Optional<UserEntity> user = userRepository.findByUsername(loginDto.getUsername());
        if (user.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Invalid username or password");
        }
        if (!passwordEncoder.matches(loginDto.getPassword(), user.get().getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt =jwtTokenProvider.generateToken(auth);
        SessionTokenEntity sessionToken = new SessionTokenEntity();
        sessionToken.setToken(jwt);
        sessionToken.setUser(user.get());
        sessionToken.setIssuedAt(LocalDateTime.now());
        sessionToken.setExpiresAt(LocalDateTime.now().plusDays(7));


        sessionTokenRepository.deleteByUser(user.get());
        sessionTokenRepository.save(sessionToken);

        //twilio
        VoiceGrant voiceGrant = new VoiceGrant()
                .setOutgoingApplicationSid(twilioConfig.getTwimlAppSid())
                .setIncomingAllow(true);
        ChatGrant chatGrant = new ChatGrant().setServiceSid(twilioConfig.getServiceSid());


        AccessToken twilioToken = new AccessToken.Builder(
                twilioConfig.getAccountSid(),
                twilioConfig.getApiKeySid(),
                twilioConfig.getApiKeySecret()
        )
                .identity(user.get().getUsername())
                .ttl(twilioConfig.getTokenTtlSeconds())
                .grant(voiceGrant).grant(chatGrant)
                .build();

        String token = twilioToken.toJwt();

        AuthResponseDto responseDto = new AuthResponseDto();
        responseDto.setToken(jwt);
        responseDto.setTwilioToken(token);
        responseDto.setId(user.get().getId());
        responseDto.setUsername(user.get().getUsername());
        responseDto.setEmail(user.get().getEmail());
        responseDto.setProfileImageUrl(user.get().getProfileImageUrl());

        return responseDto;
    }
    public String register(RegisterDto registerDto) {
        if(userRepository.existsByUsername(registerDto.getUsername())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username already exists!");
        }
        if(userRepository.existsByEmail(registerDto.getEmail())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already exists!.");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(registerDto.getUsername());
        userEntity.setEmail(registerDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        userRepository.save(userEntity);

        try {

            Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
            User user = User.creator(twilioConfig.getChatServiceSid(), userEntity.getUsername())
                    .create();

            System.out.println(user.getIdentity());
        } catch (com.twilio.exception.ApiException e) {
            System.err.println("Failed to create Twilio user: " + e.getMessage());
        }

        return "User Registered Successfully, you can now login with username and password";
    }

    @Transactional
    public boolean logout(HttpServletRequest request) {
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            sessionTokenRepository.deleteByToken(token);
            SecurityContextHolder.clearContext();
            return true;
        }
        return false;
    }

    public List<Map<String, Boolean>> WhosOnline() {
        List<UserEntity> users = userRepository.findAll();
        List<Map<String, Boolean>> result = new ArrayList<>();
        for (UserEntity user : users) {
            boolean isOnline = sessionTokenRepository.existsByUserIdAndExpiresAtAfter(
                    user.getId(), LocalDateTime.now()
            );
            Map<String, Boolean> userStatus = new HashMap<>();
            userStatus.put(user.getUsername(), isOnline);
            result.add(userStatus);
        }
        return result;
    }

}

