package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.SessionTokenEntity;
import net.tecfrac.restoapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionTokenRepository extends JpaRepository<SessionTokenEntity, String> {

    void deleteByUser(UserEntity userEntity);

    String deleteByToken(String token);
    SessionTokenEntity findByToken(String token);

    boolean existsByUserIdAndExpiresAtAfter(Long id, LocalDateTime now);
}
