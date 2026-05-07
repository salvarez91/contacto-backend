package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByMatchIdOrderByTimestampAsc(Long matchId);
}