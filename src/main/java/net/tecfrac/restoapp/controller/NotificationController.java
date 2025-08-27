package net.tecfrac.restoapp.controller;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.tecfrac.restoapp.dto.NotificationDto;
import net.tecfrac.restoapp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications(Principal principal) {
            List<NotificationDto> notificationDtos =notificationService.getNotifications(principal);
            return ResponseEntity.ok(notificationDtos);
    }
    @PatchMapping
    public ResponseEntity<String> CheckAllNotifications(Principal principal) {
        notificationService.checkNotificationsForUser(principal);
        return ResponseEntity.ok().body("Notification checked for User");
    }

}
