package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.dto.LoginRequest;
import es.paloma.contacto.backend.dto.RegistroRequest;
import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String password = request.getPassword();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AccesoNoAutorizadoException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new AccesoNoAutorizadoException("Email o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", String.valueOf(usuario.getId()),
                "rol", usuario.getRol(),
                "email", usuario.getEmail()
        ));
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody RegistroRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ConflictoException("El email ya está registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.getNombre());
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setRol(determinarRol(request.getRol()));

        usuarioRepository.save(nuevo);
        log.info("Nuevo usuario registrado: {}", email);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario creado con éxito"));
    }

    private String determinarRol(String rolSolicitado) {
        if (rolSolicitado == null) return "MAYOR";
        String rol = rolSolicitado.toUpperCase();
        if ("ADMIN".equals(rol)) return "MAYOR";
        if ("VOLUNTARIO".equals(rol)) return "VOLUNTARIO";
        return "MAYOR";
    }

    @PostMapping("/intereses")
    public ResponseEntity<?> guardarIntereses(@RequestBody Map<String, Object> payload) {
        String email = payload.get("email") != null ? ((String) payload.get("email")).toLowerCase().trim() : "";
        if (email.isEmpty()) {
            throw new PeticionIncorrectaException("Email obligatorio");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return ResponseEntity.ok().build();
    }
}