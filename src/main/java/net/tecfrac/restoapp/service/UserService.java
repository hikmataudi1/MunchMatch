package net.tecfrac.restoapp.service;

import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.UserDto;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.PollRepository;
import net.tecfrac.restoapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    private final WebClient supabaseWebClient;
    private final String supabaseUrl;
    private final String bucket;

    public UserService(UserRepository userRepository,
                       PollRepository pollRepository,
                       ModelMapper modelMapper,
                       @Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.apiKey}") String supabaseApiKey,
                       @Value("${supabase.bucket}") String bucket) {

        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.supabaseWebClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseApiKey)
                .build();
    }

    public List<UserDto> getAllUsers() {
        List<UserDto> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> users.add(modelMapper.map(user, UserDto.class)));
        return users;
    }

    public UserDto getUserById(long id) {
        UserEntity userEntity = userRepository.getReferenceById(id);
        return modelMapper.map(userEntity, UserDto.class);
    }

    public Long getIdByUsername(String username) {
        return userRepository.findByUsername(username).get().getId();
    }

    public List<UserDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findAllByIdIn(ids).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    public UserDto updateUser(String base64Image, Principal principal){
        UserEntity userEntity = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        System.out.println(base64Image);
        if (base64Image != null && !base64Image.isBlank()) {
            try {
                String cleanBase64 = base64Image
                        .replaceAll("^data:image/[^;]+;base64,", "")
                        .replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                String mimeType = base64Image.startsWith("data:image/jpeg") ? "image/jpeg" : "image/png";
                String extension = mimeType.equals("image/jpeg") ? ".jpg" : ".png";
                String filename = bucket + "/profile-images/" + userEntity.getId() + "_" + UUID.randomUUID() + extension;
                supabaseWebClient.post()
                        .uri("/storage/v1/object/" + filename)
                        .header("Content-Type", mimeType)
                        .bodyValue(imageBytes)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                String imageUrl = supabaseUrl + "/storage/v1/object/public/" + filename;
                userEntity.setProfileImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Invalid Base64 image provided", e);
            }
        }
        UserEntity updatedUser = userRepository.save(userEntity);
        return modelMapper.map(updatedUser, UserDto.class);
    }
}
