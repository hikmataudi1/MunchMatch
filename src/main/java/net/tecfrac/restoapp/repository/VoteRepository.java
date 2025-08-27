package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.PollOptionEntity;
import net.tecfrac.restoapp.entity.UserEntity;
import net.tecfrac.restoapp.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<VoteEntity,Long> {
    boolean existsByUserEntityAndPollOption(UserEntity user, PollOptionEntity pollOption);

    List<VoteEntity> findAllByPollOptionId(Long pollOptionId);

    VoteEntity findByUserEntityAndPollOption(UserEntity user, PollOptionEntity pollOption);
    boolean existsById(Long id);
    void deleteById(Long id);
    List<VoteEntity> findAllByPollOptionIdIn(List<Long> optionIds);

}
