package net.tecfrac.restoapp.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.*;
import net.tecfrac.restoapp.entity.*;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.*;
import net.tecfrac.restoapp.security.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class VoteService {
    SimpMessagingTemplate  simpMessagingTemplate;
    VoteRepository voteRepository;
    UserRepository userRepository;
    PollRepository pollRepository;
    PollOptionRepository pollOptionRepository;
    OptionRepository optionRepository;
    ModelMapper modelMapper;
    JwtTokenProvider  jwtTokenProvider;
    NotificationService notificationService;

    public PollOptionDto addVote(HttpServletRequest request,Long pollOptionId) {

        String username=jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PollOptionEntity pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found"));

        if (voteRepository.existsByUserEntityAndPollOption(user, pollOption)) {
            throw new IllegalArgumentException("User has already voted for this option.");
        }

        VoteEntity vote = new VoteEntity();
        vote.setUserEntity(user);
        vote.setPollOption(pollOption);
        voteRepository.save(vote);

        List<VoteEntity> votes = voteRepository.findAllByPollOptionId(pollOptionId);
        List<UserDto> voterDtos = votes.stream()
                .map(v -> modelMapper.map(v.getUserEntity(), UserDto.class))
                .collect(Collectors.toList());

        OptionEntity option = pollOption.getOption();

        PollOptionDto pollOptionDto = new PollOptionDto();
        pollOptionDto.setId(pollOption.getId());
        pollOptionDto.setTitle(option.getTitle());
        pollOptionDto.setDescription(option.getDescription());
        pollOptionDto.setVoters(voterDtos);

        return pollOptionDto;

    }
    public void deleteVote(HttpServletRequest request, Long voteId) {

        String username=jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VoteEntity vote = voteRepository.findById(voteId).orElseThrow(
                () -> new ResourceNotFoundException("Vote not found"));
        voteRepository.delete(vote);
    }
    public VoteDto updateVotePollOption(Long voteId, Long newPollOptionId) {
        if (voteId == null) {
            throw new IllegalArgumentException("Vote ID must not be null");
        }
        if (newPollOptionId == null) {
            throw new IllegalArgumentException("New PollOption ID must not be null");
        }

        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with ID " + voteId));

        PollOptionEntity newPollOption = pollOptionRepository.findById(newPollOptionId)
                .orElseThrow(() -> new ResourceNotFoundException("PollOption not found with ID " + newPollOptionId));

        if (!(vote.getPollOption().getPollEntity().getId() == newPollOption.getPollEntity().getId())) {
            throw new IllegalArgumentException("New PollOption does not belong to the same Poll");
        }

        vote.setPollOption(newPollOption);

        voteRepository.save(vote);
        return  modelMapper.map(vote, VoteDto.class);
    }

    @Transactional
    public PollResponseDto addVoteWs(Long pollOptionId, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PollOptionEntity pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found"));

        if (voteRepository.existsByUserEntityAndPollOption(user, pollOption)) {
            throw new IllegalArgumentException("User has already voted for this option.");
        }




        VoteEntity updatedVote = new VoteEntity();
        updatedVote.setUserEntity(user);
        updatedVote.setPollOption(pollOption);
        voteRepository.save(updatedVote);

        PollEntity poll = pollRepository.findById(pollOption.getPollEntity().getId()).get();
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
//            optionDto.setImageUrls(o.getImageUrls());
//            optionDto.setTags(o.getTags());
            optionDto.setVoters(voters);
            return optionDto;
        }).toList();

        dto.setOptions(optionDtos);
        if(userId!=poll.getCreatedBy().getId()){
            notificationService.notifyUser(
                    poll.getCreatedBy().getId(),
                    "Someone voted on your poll",
                    updatedVote.getUserEntity().getUsername()+" voted on your poll "+poll.getTitle()
            );
        }

        return dto;
    }
    @Transactional
    public PollResponseDto removeVoteWs(Long pollOptionId, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PollOptionEntity pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found"));

        VoteEntity updatedVote = voteRepository.findByUserEntityAndPollOption(user, pollOption);

        if(voteRepository.existsByUserEntityAndPollOption(user, pollOption)) {
            voteRepository.delete(updatedVote);

        }else {
            throw new ResourceNotFoundException("Vote not found with ID "+pollOptionId);
        }

        PollEntity poll = pollRepository.findById(pollOption.getPollEntity().getId()).get();
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
//            optionDto.setImageUrls(o.getImageUrls());
//            optionDto.setTags(o.getTags());
            optionDto.setVoters(voters);
            return optionDto;
        }).toList();

        dto.setOptions(optionDtos);

        return dto;

    }

}
