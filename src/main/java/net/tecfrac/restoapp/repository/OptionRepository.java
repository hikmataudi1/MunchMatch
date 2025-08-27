package net.tecfrac.restoapp.repository;

import net.tecfrac.restoapp.entity.OptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<OptionEntity, Integer> {
    Optional<OptionEntity> findById(Long id);
    List<OptionEntity> findAllByIdIn(List<Long> ids);
}
