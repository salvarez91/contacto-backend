package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Alerta;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByReferidoId(Long referidoId);

    List<Alerta> findByVistaFalse();

    @Transactional
    void deleteByReferidoId(Long referidoId);
}