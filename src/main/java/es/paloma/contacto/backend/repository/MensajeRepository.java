package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE (m.emisorId = ?1 AND m.receptorId = ?2) " +
            "OR (m.emisorId = ?2 AND m.receptorId = ?1) ORDER BY m.id ASC")
    List<Mensaje> findConversacion(Long u1, Long u2);
}