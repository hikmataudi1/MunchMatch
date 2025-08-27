package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.PollEntity;
import net.tecfrac.restoapp.entity.PollOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOptionEntity,Long> {
    List<PollOptionEntity> findAllByPollEntityId(Long pollId);


}
