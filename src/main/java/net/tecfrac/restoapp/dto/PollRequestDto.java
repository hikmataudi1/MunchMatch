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
public class PollRequestDto {
    private String title;
    private Date endDate;
    private List<Long> optionIds;
    private List<Long> visibleUserIds;
    private boolean allowMultipleVotes;
}
