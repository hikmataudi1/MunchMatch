package net.tecfrac.restoapp.controller;

import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.RequestOptionDto;
import net.tecfrac.restoapp.dto.ResponseOptionDto;
import net.tecfrac.restoapp.service.OptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/option")
public class OptionController {

    private final OptionService optionService;


    @PostMapping
    public ResponseEntity<ResponseOptionDto> createOption(@RequestBody RequestOptionDto dto) {
        ResponseOptionDto created = optionService.createOption(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<ResponseOptionDto>> getAllOptions() {
        return ResponseEntity.ok(optionService.getAllOptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseOptionDto> getOptionById(@PathVariable Long id) {
        return ResponseEntity.ok(optionService.getOptionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseOptionDto> updateOption(@PathVariable Long id, @RequestBody RequestOptionDto dto) {
        return ResponseEntity.ok(optionService.updateOption(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOption(@PathVariable Long id) {
        optionService.deleteOption(id);
        return ResponseEntity.ok("Option deleted successfully");
    }
}
