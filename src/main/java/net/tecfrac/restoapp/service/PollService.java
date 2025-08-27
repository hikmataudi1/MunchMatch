package net.tecfrac.restoapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.*;
import net.tecfrac.restoapp.entity.*;
import net.tecfrac.restoapp.exception.PollAccessDeniedException;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.*;
//import net.tecfrac.restoapp.utils.ReadyNotifications;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final ModelMapper modelMapper;
    PollOptionRepository pollOptionRepository;
    VoteRepository voteRepository;
    NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    public PollResponseDto createPoll(PollRequestDto pollDto, String username) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));


            PollEntity pollEntity = new PollEntity();
            pollEntity.setTitle(pollDto.getTitle());


            Date originalDate = pollDto.getEndDate();
            Instant instant = originalDate.toInstant();
            Date newDate = Date.from(instant.minus(3, ChronoUnit.HOURS));
            pollEntity.setEndDate(newDate);

            pollEntity.setCreatedAt(new Date());
            pollEntity.setActive(true);
            pollEntity.setAllowMultipleVotes(pollDto.isAllowMultipleVotes());
            pollEntity.setCreatedBy(userEntity);

        if (pollDto.getVisibleUserIds() != null && !pollDto.getVisibleUserIds().isEmpty()) {
            List<UserEntity> visibleUsers = userRepository.findAllById(pollDto.getVisibleUserIds());
            pollEntity.setVisibleToUsers(visibleUsers);
        }

        PollEntity savedPollEntity = pollRepository.save(pollEntity);

        if (savedPollEntity.getVisibleToUsers() != null) {
            for (UserEntity user : savedPollEntity.getVisibleToUsers()) {
                notificationService.notifyUser(
                        user.getId(),
                        "\uD83D\uDDF3️ New Poll Available!",
                        "A new poll titled \"" + savedPollEntity.getTitle() + "\" has just been created. Cast your vote now!"
                );
            }
        }


            List<OptionEntity> options = optionRepository.findAllByIdIn(pollDto.getOptionIds());
            List<PollOptionEntity> pollOptions = new ArrayList<>();

            for (OptionEntity option : options) {
                PollOptionEntity pollOption = new PollOptionEntity();
                pollOption.setPollEntity(savedPollEntity);
                pollOption.setOption(option);
                pollOptions.add(pollOption);
            }

            pollOptionRepository.saveAll(pollOptions);
            savedPollEntity.setPollOptions(pollOptions);


            List<PollOptionDto> optionDtos = pollOptions.stream().map(po -> {
                OptionEntity o = po.getOption();
                PollOptionDto optionDto = new PollOptionDto();
                optionDto.setId(po.getId());
                optionDto.setTitle(o.getTitle());
                optionDto.setDescription(o.getDescription());
                optionDto.setVoters(new ArrayList<>());

                return optionDto;
            }).toList();

            PollResponseDto response = new PollResponseDto();
            response.setId(savedPollEntity.getId());
            response.setTitle(savedPollEntity.getTitle());
            response.setCreatedById(userEntity.getId());
            response.setCreatedByUsername(userEntity.getUsername());
            response.setCreatedAt(savedPollEntity.getCreatedAt());
            response.setEndDate(savedPollEntity.getEndDate());
            response.setActive(savedPollEntity.isActive());
            response.setAllowMultipleVotes(savedPollEntity.isAllowMultipleVotes());
            response.setOptions(optionDtos);

        if (savedPollEntity.getVisibleToUsers() != null) {
            System.out.println("visible to: "+savedPollEntity.getVisibleToUsers());
            messagingTemplate.convertAndSendToUser(pollEntity.getCreatedBy().getUsername(),
                    "/queue/poll",
                    objectMapper.writeValueAsString(response)
            );
            for (UserEntity user : savedPollEntity.getVisibleToUsers()) {
                try{
                    messagingTemplate.convertAndSendToUser(user.getUsername(),
                            "/queue/poll",
                            objectMapper.writeValueAsString(response)
                    );
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }

            }
            System.out.println("end");
        }
        System.out.println("done");

        return response;
    }
    public PollResponseDto deactivatePoll(Long id,String username){
        PollEntity pollEntity = pollRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (!pollEntity.getCreatedBy().getUsername().equals(username)) {
            throw new PollAccessDeniedException("You are not authorized to deactivate this poll");
        }
        pollEntity.setActive(false);
        pollRepository.save(pollEntity);
        return modelMapper.map(pollEntity, PollResponseDto.class);
    }
    public Page<PollResponseDto> getPaginatedPollsWithDetails(Pageable pageable, Principal principal, boolean activeOnly) {
        UserEntity user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<PollEntity> pollPage;
        if (activeOnly) {
            pollPage = pollRepository.findAllByActiveTrue(pageable);
        } else {
            pollPage = pollRepository.findAll(pageable);
        }

        List<PollResponseDto> pollDtos = pollPage.stream()
                .filter(poll -> poll.getVisibleToUsers().isEmpty()
                        || poll.getVisibleToUsers().contains(user)
                        || poll.getCreatedBy().equals(user))
                .map(poll -> {
                    PollResponseDto dto = new PollResponseDto();
                    dto.setId(poll.getId());
                    dto.setTitle(poll.getTitle());
                    dto.setCreatedAt(poll.getCreatedAt());
                    dto.setEndDate(poll.getEndDate());
                    dto.setActive(poll.isActive());
                    dto.setAllowMultipleVotes(poll.isAllowMultipleVotes());
                    dto.setCreatedByUsername(poll.getCreatedBy().getUsername());
                    dto.setCreatedById(poll.getCreatedBy().getId());

                    List<PollOptionEntity> pollOptionEntities = pollOptionRepository.findAllByPollEntityId(poll.getId());

                    List<PollOptionDto> optionDtos = pollOptionEntities.stream().map(po -> {
                        OptionEntity o = po.getOption();
                        List<UserDto> voters = voteRepository.findAllByPollOptionId(po.getId()).stream()
                                .map(vote -> {
                                    UserEntity voter = vote.getUserEntity();
                                    return new UserDto(voter.getId(), voter.getUsername(), voter.getEmail(), voter.getProfileImageUrl());
                                }).collect(Collectors.toList());

                        PollOptionDto optionDto = new PollOptionDto();
                        optionDto.setId(po.getId());
                        optionDto.setTitle(o.getTitle());
                        optionDto.setDescription(o.getDescription());
                        optionDto.setImageUrls(o.getImageUrls());
                        optionDto.setTags(o.getTags());
                        optionDto.setVoters(voters);
                        return optionDto;
                    }).toList();

                    dto.setOptions(optionDtos);
                    return dto;
                }).toList();

        return new PageImpl<>(pollDtos, pageable, pollPage.getTotalElements());
    }

    public PollResponseDto getPollById(Long id) {
        PollEntity pollEntity = pollRepository.findById(id).orElseThrow(() -> new RuntimeException("Poll not found"));
        return modelMapper.map(pollEntity, PollResponseDto.class);
    }

    public List<PollResponseDto> getPollsByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<PollEntity> polls = pollRepository.findByCreatedBy(userEntity);
        return polls.stream()
                .map(poll -> modelMapper.map(poll, PollResponseDto.class))
                .toList();
    }
    @Transactional
    @Scheduled(fixedRate = 3000000)
    public void checkPollTimeout() {
        Date now = new Date();
        List<PollEntity> ToCheckPolls = pollRepository.findByActive(true);

        for (PollEntity poll : ToCheckPolls) {
            if (poll.getEndDate().before(now)) {
                poll.setActive(false);
                List<PollOptionEntity> pollOptions = pollOptionRepository.findAllByPollEntityId(poll.getId());
                List<Long> pollOptionsIds = pollOptions.stream().map(PollOptionEntity::getId).collect(Collectors.toList());
                List<VoteEntity> votes = voteRepository.findAllByPollOptionIdIn(pollOptionsIds);
                for (VoteEntity vote : votes) {
                    notificationService.notifyUser(
                            vote.getUserEntity().getId(),
                            "Poll Ended",
                            "Poll "+poll.getId()+" - "+poll.getTitle()+" ended now! Check the results!"
                    );
                }
            }
        }
        if (!ToCheckPolls.isEmpty()) {
            pollRepository.saveAll(ToCheckPolls);
        }
    }
}

