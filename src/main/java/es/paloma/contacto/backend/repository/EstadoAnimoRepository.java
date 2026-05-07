package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.EstadoAnimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadoAnimoRepository extends JpaRepository<EstadoAnimo, Long> {
    List<EstadoAnimo> findByUsuarioId(Long usuarioId);
}