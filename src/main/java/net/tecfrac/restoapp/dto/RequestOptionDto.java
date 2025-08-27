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
public class RequestOptionDto {
    private String title;
    private String description;
    private List<String> images;
    private List<String> tags;
}