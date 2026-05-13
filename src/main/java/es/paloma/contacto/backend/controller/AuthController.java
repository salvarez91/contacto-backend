package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.dto.*;
import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.InteresRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InteresRepository interesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AccesoNoAutorizadoException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new AccesoNoAutorizadoException("Email o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(new LoginResponse(
                token,
                String.valueOf(usuario.getId()),
                usuario.getRol(),
                usuario.getEmail()
        ));
    }

    @Transactional
    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ConflictoException("El email ya está registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.getNombre());
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setRol(determinarRol(request.getRol()));

        usuarioRepository.save(nuevo);
        log.info("Usuario registrado: {}", email);
        return ResponseEntity.ok(new RegistroResponse("Usuario creado con éxito"));
    }

    @Transactional
    @PostMapping("/intereses")
    public ResponseEntity<RegistroResponse> guardarIntereses(@Valid @RequestBody InteresesRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Set<Interes> intereses = new HashSet<>();
        if (request.getIntereses() != null) {
            for (String nombre : request.getIntereses()) {
                Optional<Interes> interes = interesRepository.findByNombre(nombre);
                interes.ifPresent(intereses::add);
            }
        }
        usuario.setIntereses(intereses);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new RegistroResponse("Intereses guardados con éxito"));
    }

    private String determinarRol(String rol) {
        if (rol == null || "ADMIN".equalsIgnoreCase(rol)) return "MAYOR";
        return rol.toUpperCase();
    }
}