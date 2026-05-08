package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteresRepository extends JpaRepository<Interes, Long> {
    Optional<Interes> findByNombre(String nombre);
}