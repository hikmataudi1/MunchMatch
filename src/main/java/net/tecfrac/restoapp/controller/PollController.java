package net.tecfrac.restoapp.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.PollRequestDto;
import net.tecfrac.restoapp.dto.PollResponseDto;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.UserRepository;
import net.tecfrac.restoapp.service.PollService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/poll")
public class PollController {
    PollService pollService;

    @PostMapping
    public ResponseEntity<PollResponseDto> createPoll(@RequestBody PollRequestDto dto, Principal principal) throws JsonProcessingException {
        PollResponseDto created = pollService.createPoll(dto, principal.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PollResponseDto>> getPolls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "false") boolean active,
            Principal principal) {
        System.out.println("is active?"+active);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PollResponseDto> polls = pollService.getPaginatedPollsWithDetails(pageable, principal, active);
        return ResponseEntity.ok(polls);
    }

    @PatchMapping("/{pollId}/deactivate")
    public ResponseEntity<PollResponseDto> deactivatePoll(@PathVariable Long pollId,Principal principal) throws AccessDeniedException {
        System.out.println("start");
        PollResponseDto updatedPoll = pollService.deactivatePoll(pollId, principal.getName());
        return ResponseEntity.ok(updatedPoll);
    }

    @GetMapping("{id}")
    public ResponseEntity<PollResponseDto> getPollById(@PathVariable Long id) {
        PollResponseDto responseDto= pollService.getPollById(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/mypolls")
    public ResponseEntity<List<PollResponseDto>> getMyPolls(Principal principal) {
        String username = principal.getName();
        List<PollResponseDto> myPolls = pollService.getPollsByUsername(username);
        return ResponseEntity.ok(myPolls);
    }

}
