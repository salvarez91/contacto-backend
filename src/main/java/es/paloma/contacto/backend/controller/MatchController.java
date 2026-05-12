package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.MatchRepository;
import es.paloma.contacto.backend.repository.MensajeRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import es.paloma.contacto.backend.service.MatchingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    @Transactional
    public ResponseEntity<Match> createMatch(@RequestBody Map<String, Long> payload,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        Usuario mayorAutenticado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Long voluntarioId = payload.get("voluntarioId");

        if (voluntarioId == null) throw new PeticionIncorrectaException("ID de voluntario no proporcionado");

        boolean yaExiste = matchRepository.findByMayorId(mayorAutenticado.getId()).stream()
                .anyMatch(m -> m.getVoluntario().getId().equals(voluntarioId));

        if (yaExiste) {
            throw new ConflictoException("Ya tienes una conexión activa con este voluntario");
        }

        Usuario voluntario = usuarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Voluntario no encontrado"));

        Match match = new Match();
        match.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        match.setActive(true);
        match.setMayor(mayorAutenticado);
        match.setVoluntario(voluntario);

        Match nuevoMatch = matchRepository.save(match);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMatch);
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<Usuario>> sugerirVoluntarios(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String interes) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!"MAYOR".equalsIgnoreCase(usuario.getRol())) {
            return ResponseEntity.ok(List.of());
        }

        List<Usuario> sugerencias = matchingService.sugerirVoluntarios(usuario.getId(), interes);
        return ResponseEntity.ok(sugerencias);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarMatch(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Match no encontrado"));

        if (!"ADMIN".equals(usuario.getRol()) &&
                !match.getMayor().getId().equals(usuario.getId()) &&
                !match.getVoluntario().getId().equals(usuario.getId())) {
            throw new AccesoNoAutorizadoException("No tienes permiso para eliminar esta conexión");
        }

        mensajeRepository.borrarConversacion(match.getMayor().getId(), match.getVoluntario().getId());

        matchRepository.delete(match);
        return ResponseEntity.noContent().build();
    }
}