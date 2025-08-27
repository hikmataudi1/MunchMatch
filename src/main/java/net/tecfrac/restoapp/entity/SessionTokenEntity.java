package net.tecfrac.restoapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "session_token")
public class SessionTokenEntity {
    @Id
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")

    private UserEntity user;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private boolean active = true;
}
