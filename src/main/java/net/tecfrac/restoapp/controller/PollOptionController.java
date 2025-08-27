package net.tecfrac.restoapp.controller;

import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.PollOptionDto;
import net.tecfrac.restoapp.service.PollOptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/polloption")
public class PollOptionController {
    PollOptionService pollOptionService;
    public ResponseEntity<PollOptionDto> creatPollOption(PollOptionDto pollOptionDto,Long optionId,Long pollId) {
        if (pollOptionDto==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PollOptionDto newPollOptionDto = pollOptionService.createPollOption(pollId,optionId);
        return new ResponseEntity<>(newPollOptionDto, HttpStatus.CREATED);
    }
}
