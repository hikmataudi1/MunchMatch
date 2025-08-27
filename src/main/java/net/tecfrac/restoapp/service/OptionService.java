package net.tecfrac.restoapp.service;

import net.tecfrac.restoapp.dto.RequestOptionDto;
import net.tecfrac.restoapp.dto.ResponseOptionDto;
import net.tecfrac.restoapp.entity.OptionEntity;
import net.tecfrac.restoapp.exception.ResourceNotFoundException;
import net.tecfrac.restoapp.repository.OptionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final ModelMapper modelMapper;
    private final WebClient supabaseWebClient;
    private final String supabaseUrl;
    private final String bucket;

    public OptionService(OptionRepository optionRepository,
                         ModelMapper modelMapper,
                         @Value("${supabase.url}") String supabaseUrl,
                         @Value("${supabase.apiKey}") String supabaseApiKey,
                         @Value("${supabase.bucket}") String bucket) {
        this.optionRepository = optionRepository;
        this.modelMapper = modelMapper;
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;

        this.supabaseWebClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseApiKey)
                .build();
    }

    public ResponseOptionDto createOption(RequestOptionDto dto) {
        OptionEntity entity = modelMapper.map(dto, OptionEntity.class);

        if (entity.getTitle() == null) {
            throw new InputMismatchException("Title cannot be null");
        }

        List<String> uploadedUrls = new ArrayList<>();

        if (dto.getImages() != null) {
            for (String base64Image : dto.getImages()) {
                try {
                    String base64DataTrimmed = base64Image
                            .replaceAll("^data:image/[^;]+;base64,", "")
                            .replaceAll("\\s+", "");

                    byte[] imageBytes = Base64.getDecoder().decode(base64DataTrimmed);
                    String url = uploadBytesToSupabase(base64Image, imageBytes);
                    uploadedUrls.add(url);

                } catch (Exception e) {
                    throw new RuntimeException("Failed to process image", e);
                }
            }
        }

        entity.setImageUrls(uploadedUrls);

        OptionEntity saved = optionRepository.save(entity);

        return modelMapper.map(saved, ResponseOptionDto.class);
    }

    public List<ResponseOptionDto> getAllOptions() {
        return optionRepository.findAll().stream()
                .map(option -> modelMapper.map(option, ResponseOptionDto.class))
                .collect(Collectors.toList());
    }

    public ResponseOptionDto getOptionById(Long id) {
        OptionEntity entity = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        return modelMapper.map(entity, ResponseOptionDto.class);
    }

    public ResponseOptionDto updateOption(Long id, RequestOptionDto dto) {
        OptionEntity existing = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        modelMapper.map(dto, existing);
        OptionEntity updated = optionRepository.save(existing);
        return modelMapper.map(updated, ResponseOptionDto.class);
    }

    public void deleteOption(Long id) {
        OptionEntity entity = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        if (entity.getImageUrls() != null) {
            entity.getImageUrls().forEach(this::deleteImageFromSupabase);
        }
        optionRepository.delete(entity);
    }

    private String uploadBytesToSupabase(String base64Image, byte[] fileBytes) {
        String mimeType = "image/png"; // default
        if (base64Image.startsWith("data:image/jpeg")) {
            mimeType = "image/jpeg";
        }

        String extension = mimeType.equals("image/jpeg") ? ".jpg" : ".png";
        String filename = UUID.randomUUID() + extension;
        String path = bucket + "/" + filename;

        supabaseWebClient.post()
                .uri("/storage/v1/object/" + path)
                .header("Content-Type", mimeType)
                .bodyValue(fileBytes)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return supabaseUrl + "/storage/v1/object/public/" + path;
    }
    private void deleteImageFromSupabase(String imageUrl) {
        String path = imageUrl.substring(imageUrl.indexOf(bucket + "/"));
        supabaseWebClient.delete()
                .uri("/storage/v1/object/" + path)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

}
