package net.tecfrac.restoapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
//import net.tecfrac.restoapp.config.NotificationHandler;
//import net.tecfrac.restoapp.config.VoteHandler;
import net.tecfrac.restoapp.dto.NotificationDto;
import net.tecfrac.restoapp.entity.NotificationEntity;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.NotificationRepository;
import net.tecfrac.restoapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
//    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationDto notifyUser(Long id, String title, String content) {
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User not found");
        }

        UserEntity recipient = userRepository.findById(id).get();
        NotificationEntity notification = new NotificationEntity();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRecipient(recipient);
        notification.setCreatedAt(new Date());

        notificationRepository.save(notification);

        NotificationDto dto = new NotificationDto();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setCreatedAt(notification.getCreatedAt());
        try {
            System.out.println("before sending notification");
            messagingTemplate.convertAndSendToUser(recipient.getUsername(),"/queue/notification",objectMapper.writeValueAsString(dto));
            System.out.println("after sending notification");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not send real-time notification to user " + recipient.getId());
        }
        return dto;
    }

    public List<NotificationDto> getNotifications(Principal principal) {
        UserEntity user=userRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        List<NotificationEntity> notificationEntities = notificationRepository.findByRecipientId(user.getId());
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for (NotificationEntity notificationEntity : notificationEntities) {
            notificationDtos.add(modelMapper.map(notificationEntity, NotificationDto.class));
        }
        return notificationDtos;
    }
    public void checkNotificationsForUser(Principal  principal) {
        UserEntity user=userRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        List<NotificationEntity> notificationEntities = notificationRepository.findByRecipientId(user.getId());
        for (NotificationEntity notificationEntity : notificationEntities) {
            notificationEntity.setChecked(true);
            notificationRepository.save(notificationEntity);
        }
    }

}
