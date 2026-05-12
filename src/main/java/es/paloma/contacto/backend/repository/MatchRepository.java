package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Match;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByMayorId(Long mayorId);

    List<Match> findByVoluntarioId(Long voluntarioId);

    @Transactional
    void deleteByMayorIdOrVoluntarioId(Long mayorId, Long voluntarioId);
}