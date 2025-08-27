package net.tecfrac.restoapp.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VoteMessageDto {
    private Long userId;
    private String voteAction;   // "add", "remove", "update"
    private Long pollOptionId;
    private Long previousOptionId;
}
