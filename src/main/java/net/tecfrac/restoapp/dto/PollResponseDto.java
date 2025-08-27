package net.tecfrac.restoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PollResponseDto {
    private Long id;
    private String title;
    private Long createdById;
    private String createdByUsername;
    private Date createdAt;
    private Date endDate;
    private boolean active;
    private boolean allowMultipleVotes;
    private List<PollOptionDto> options;
}