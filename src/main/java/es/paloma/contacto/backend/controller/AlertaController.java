package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/usuario/{usuarioId}")
    public List<Alerta> getAlertasByUsuarioId(@PathVariable Long usuarioId, @RequestHeader("Authorization") String authHeader) {
        String emailToken = jwtUtil.extractEmail(authHeader.substring(7));
        String rolToken = jwtUtil.extractRol(authHeader.substring(7));

        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!"ADMIN".equals(rolToken) && !u.getEmail().equals(emailToken)) {
            throw new AccesoNoAutorizadoException("No tienes permiso para ver las alertas de este usuario");
        }

        return alertaRepository.findByReferidoId(usuarioId);
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Alerta> getAlertasPendientes() {
        return alertaRepository.findByVistaFalse();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Alerta> createAlerta(@RequestBody Alerta alerta) {
        Alerta nuevaAlerta = alertaRepository.save(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAlerta);
    }

    @PutMapping("/{id}/vista")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Alerta> marcarAlertaComoVista(@PathVariable Long id) {
        return alertaRepository.findById(id).map(alerta -> {
            alerta.setVista(true);
            Alerta actualizada = alertaRepository.save(alerta);
            return ResponseEntity.ok(actualizada);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}