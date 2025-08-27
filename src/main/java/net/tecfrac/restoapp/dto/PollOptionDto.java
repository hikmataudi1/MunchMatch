package net.tecfrac.restoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PollOptionDto {
    private Long id;
    private String title;
    private String description;
    private List<UserDto> voters;
    private List<String> imageUrls;
    private List<String> tags;
}
