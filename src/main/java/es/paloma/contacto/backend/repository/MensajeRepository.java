package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Mensaje;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE (m.emisorId = :u1 AND m.receptorId = :u2) OR (m.emisorId = :u2 AND m.receptorId = :u1) ORDER BY m.id ASC")
    List<Mensaje> findConversacion(@Param("u1") Long u1, @Param("u2") Long u2);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mensaje m WHERE (m.emisorId = :u1 AND m.receptorId = :u2) OR (m.emisorId = :u2 AND m.receptorId = :u1)")
    void borrarConversacion(@Param("u1") Long u1, @Param("u2") Long u2);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mensaje m WHERE m.emisorId = :usuarioId OR m.receptorId = :usuarioId")
    void borrarTodosLosMensajesDeUsuario(@Param("usuarioId") Long usuarioId);
}