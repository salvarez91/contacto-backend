package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.MatchRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import es.paloma.contacto.backend.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Map<String, Long> payload,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        Usuario mayorAutenticado = usuarioRepository.findByEmail(email).orElse(null);
        Long voluntarioId = payload.get("voluntarioId");

        if (mayorAutenticado == null || voluntarioId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Usuario voluntario = usuarioRepository.findById(voluntarioId).orElse(null);
        if (voluntario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Match match = new Match();
        match.setCreatedAt(LocalDateTime.now());
        match.setActive(true);
        match.setMayor(mayorAutenticado);
        match.setVoluntario(voluntario);

        Match nuevoMatch = matchRepository.save(match);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMatch);
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<Usuario>> sugerirVoluntarios(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            Usuario usuarioAutenticado = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<Usuario> sugerencias = matchingService.sugerirVoluntarios(usuarioAutenticado.getId());
            return ResponseEntity.ok(sugerencias);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}