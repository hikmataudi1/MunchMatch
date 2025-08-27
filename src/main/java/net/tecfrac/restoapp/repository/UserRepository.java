package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {

    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findAllByIdIn(List<Long> ids);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

}
