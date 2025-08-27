package net.tecfrac.restoapp.controller;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.*;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.repository.UserRepository;
import net.tecfrac.restoapp.service.NotificationService;
import net.tecfrac.restoapp.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
public class VoteController {
    VoteService voteService;

    @MessageMapping("/vote")
    @SendTo("/topic/vote")
    public PollResponseDto addVote(VoteMessageDto voteMessage) throws Exception {
        System.out.println("Vote received: " + voteMessage.getVoteAction());
        switch (voteMessage.getVoteAction()) {
            case "add" -> {
                return voteService.addVoteWs(voteMessage.getPollOptionId(), voteMessage.getUserId());
            }
            case "remove" -> {
                return voteService.removeVoteWs(voteMessage.getPollOptionId(), voteMessage.getUserId());
            }
            case "update" -> {
                voteService.removeVoteWs(voteMessage.getPreviousOptionId(), voteMessage.getUserId());
                return voteService.addVoteWs(voteMessage.getPollOptionId(), voteMessage.getUserId());
            }
            default -> System.out.println("Invalid action " + voteMessage.getVoteAction());
        }
        return  null;
    }
}
