package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByMayorId(Long mayorId);

    List<Match> findByVoluntarioId(Long voluntarioId);

    @Query("SELECT m FROM Match m JOIN FETCH m.mayor JOIN FETCH m.voluntario")
    List<Match> findAllWithUsuarios();

    @Modifying
    @Transactional
    @Query("DELETE FROM Match m WHERE m.mayor.id = :mayorId OR m.voluntario.id = :voluntarioId")
    void deleteByMayorIdOrVoluntarioId(@Param("mayorId") Long mayorId, @Param("voluntarioId") Long voluntarioId);
}
