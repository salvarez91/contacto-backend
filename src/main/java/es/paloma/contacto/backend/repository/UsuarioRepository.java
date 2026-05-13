package es.paloma.contacto.backend.repository;

import es.paloma.contacto.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByEmailNot(String email);

    Page<Usuario> findByEmailNot(String email, Pageable pageable);

    @Query("""
                SELECT DISTINCT u
                FROM Usuario u
                JOIN u.intereses i
                WHERE i.id IN :interesesIds
            """)
    List<Usuario> findVoluntariosSugeridos(@Param("interesesIds") Set<Long> interesesIds);
}