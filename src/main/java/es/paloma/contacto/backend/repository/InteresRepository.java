package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InteresRepository extends JpaRepository<Interes, Long> {

    Optional<Interes> findByNombre(String nombre);
}