package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteresRepository extends JpaRepository<Interes, Long> {
}