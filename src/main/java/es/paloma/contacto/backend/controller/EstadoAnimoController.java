package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.EstadoAnimoRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/estados-animo")
public class EstadoAnimoController {

    @Autowired
    private EstadoAnimoRepository estadoAnimoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<EstadoAnimo> registrar(@RequestBody EstadoAnimo estado, @RequestHeader("Authorization") String authHeader) {
        if (estado.getUsuario() == null || estado.getUsuario().getId() == null) {
            throw new PeticionIncorrectaException("Falta el usuario");
        }

        String emailToken = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario u = usuarioRepository.findById(estado.getUsuario().getId())
                .orElseThrow(() -> new PeticionIncorrectaException("Usuario inválido"));

        if (!u.getEmail().equals(emailToken)) {
            throw new AccesoNoAutorizadoException("No puedes registrar estados de ánimo para otro usuario");
        }

        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        if (estadoAnimoRepository.existsByUsuarioIdAndFecha(estado.getUsuario().getId(), hoy)) {
            throw new ConflictoException("Ya registraste tu estado hoy");
        }

        estado.setFecha(hoy);
        EstadoAnimo nuevo = estadoAnimoRepository.save(estado);

        if (nuevo.getNivelEmocional() == 1) {
            List<EstadoAnimo> ultimos = estadoAnimoRepository.findByUsuarioIdOrderByFechaDesc(nuevo.getUsuario().getId());
            if (ultimos.size() >= 3) {
                boolean todosTristes = ultimos.stream().limit(3).allMatch(e -> e.getNivelEmocional() == 1);
                if (todosTristes) {
                    Alerta alerta = new Alerta();
                    alerta.setDescripcion("El usuario " + nuevo.getUsuario().getNombre() + " ha registrado 3 estados tristes consecutivos.");
                    alerta.setReferido(nuevo.getUsuario());
                    alerta.setFechaCreacion(LocalDateTime.now(ZoneOffset.UTC));
                    alerta.setVista(false);
                    alertaRepository.save(alerta);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping("/hoy/{usuarioId}")
    public ResponseEntity<Boolean> haRegistradoHoy(@PathVariable Long usuarioId, @RequestHeader("Authorization") String authHeader) {
        String emailToken = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new PeticionIncorrectaException("Usuario inválido"));

        if (!u.getEmail().equals(emailToken)) {
            throw new AccesoNoAutorizadoException("Acceso denegado");
        }

        boolean exists = estadoAnimoRepository.existsByUsuarioIdAndFecha(usuarioId, LocalDate.now(ZoneOffset.UTC));
        return ResponseEntity.ok(exists);
    }
}
