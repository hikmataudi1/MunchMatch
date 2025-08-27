package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.PollEntity;
import net.tecfrac.restoapp.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface PollRepository extends JpaRepository<PollEntity,Long> {

    List<PollEntity> findByCreatedBy(UserEntity createdBy);

    List<PollEntity> findByActive(boolean active);

    Page<PollEntity> findAllByActiveTrue(Pageable pageable);
}
