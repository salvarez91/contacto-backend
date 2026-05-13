package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.dto.MatchAdminDTO;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.MatchRepository;
import es.paloma.contacto.backend.repository.MensajeRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PutMapping("/usuarios/{id}/toggle")
    @Transactional
    public ResponseEntity<Usuario> toggleActivo(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(!usuario.isActivo());
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @DeleteMapping("/usuarios/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MatchAdminDTO>> listarMatches() {
        List<Match> matches = matchRepository.findAllWithUsuarios();
        List<MatchAdminDTO> dtos = matches.stream().map(match -> new MatchAdminDTO(
                match.getId(),
                match.getMayor().getNombre(),
                match.getVoluntario().getNombre(),
                match.getCreatedAt().toString(),
                match.isActive()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/matches/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarMatch(@PathVariable Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Match no encontrado"));
        mensajeRepository.borrarConversacion(match.getMayor().getId(), match.getVoluntario().getId());
        matchRepository.delete(match);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<Alerta>> listarAlertas() {
        return ResponseEntity.ok(alertaRepository.findAll());
    }

    @PutMapping("/alertas/{id}/atender")
    @Transactional
    public ResponseEntity<Alerta> atenderAlerta(@PathVariable Long id) {
        return alertaRepository.findById(id).map(alerta -> {
            alerta.setVista(true);
            return ResponseEntity.ok(alertaRepository.save(alerta));
        }).orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada"));
    }
}
