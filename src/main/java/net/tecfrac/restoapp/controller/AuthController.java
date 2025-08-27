package net.tecfrac.restoapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.LoginDto;
import net.tecfrac.restoapp.dto.AuthResponseDto;
import net.tecfrac.restoapp.dto.NotificationDto;
import net.tecfrac.restoapp.dto.RegisterDto;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.repository.UserRepository;
import net.tecfrac.restoapp.service.AuthService;
import net.tecfrac.restoapp.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;
NotificationService  notificationService;
UserRepository userRepository;
SimpMessagingTemplate simpMessagingTemplate;
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) throws AuthenticationException, InterruptedException {
        AuthResponseDto user=authService.login(loginDto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto){
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        boolean success = authService. logout(request);
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return success?
                ResponseEntity.ok("Logout successful"):
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or not logged in");
    }
    @GetMapping("/areonline")
    public ResponseEntity<List<Map<String,Boolean>>> whosOnline(){
        List<Map<String, Boolean>> res= authService.WhosOnline();
        return  ResponseEntity.ok(res);
    }
}
