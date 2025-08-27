package net.tecfrac.restoapp.dto;

import lombok.*;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String token;
    private String twilioToken;
    private String type="Bearer";
}

