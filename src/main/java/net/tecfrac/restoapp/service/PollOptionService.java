package net.tecfrac.restoapp.service;


import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.PollOptionDto;
import net.tecfrac.restoapp.dto.PollResponseDto;
import net.tecfrac.restoapp.dto.UserDto;
import net.tecfrac.restoapp.entity.*;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PollOptionService {
    OptionRepository optionRepository;
    PollRepository pollRepository;
    PollOptionRepository pollOptionRepository;




    public PollOptionDto createPollOption(Long pollId, Long optionId)  {
        OptionEntity optionEntity = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));

        PollEntity pollEntity = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));
        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setPollEntity(pollEntity);
        pollOptionEntity.setOption(optionEntity);

        PollOptionEntity savedEntity = pollOptionRepository.save(pollOptionEntity);

        PollOptionDto dto = new PollOptionDto();
        dto.setId(savedEntity.getId());
        dto.setTitle(optionEntity.getTitle());
        dto.setDescription(optionEntity.getDescription());
        dto.setVoters(new ArrayList<>());
        return dto;
    }

//    List<PollOptionDto> getPollOptionsByPollId(Long pollId) {
//        List<PollOptionEntity> pollOptions = pollOptionRepository.findAllByPollEntityId(pollId);
//        List<PollOptionDto> pollOptionDtos = new ArrayList<>();
//        for (PollOptionEntity pollOptionEntity : pollOptions) {
//
//            List<VoteEntity> votes = voteRepository.findAllByPollOptionId(pollOptionEntity.getId());
//
//            List<UserEntity> users = votes.stream()
//                    .map(VoteEntity::getUserEntity)
//                    .toList();
//            PollOptionDto dto = new PollOptionDto();
//            dto.setId(pollOptionEntity.getId());
//
//            if (pollOptionEntity.getOption() != null) {
//                dto.setTitle(pollOptionEntity.getOption().getTitle());
//                dto.setDescription(pollOptionEntity.getOption().getDescription());
//            }
//
//            List<UserDto> userDtos = users.stream()
//                    .map(user -> modelMapper.map(user, UserDto.class))
//                    .collect(Collectors.toList());
//
//            dto.setVoters(userDtos);
//            pollOptionDtos.add(dto);
//
//        }
//        return pollOptionDtos;
//    }


}
