package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.InteresRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private InteresRepository interesRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email") != null ? credentials.get("email").toLowerCase().trim() : "";
        String password = credentials.get("password") != null ? credentials.get("password").trim() : "";

        if (email.isEmpty()) {
            throw new es.paloma.contacto.backend.exception.PeticionIncorrectaException("El email es obligatorio");
        }
        if (password.isEmpty()) {
            throw new es.paloma.contacto.backend.exception.PeticionIncorrectaException("La contraseña es obligatoria");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                String token = jwtUtil.generateToken(email, usuario.getRol());
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("id", String.valueOf(usuario.getId()));
                response.put("rol", usuario.getRol() != null ? usuario.getRol() : "MAYOR");
                response.put("email", usuario.getEmail());
                return ResponseEntity.ok(response);
            }
        }
        throw new es.paloma.contacto.backend.exception.AccesoNoAutorizadoException("Email o contraseña incorrectos");
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario nuevo) {
        String email = nuevo.getEmail().toLowerCase().trim();
        
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new es.paloma.contacto.backend.exception.ConflictoException("El email ya está registrado");
        }

        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(nuevo.getPassword()));
        
        if (nuevo.getRol() == null || nuevo.getRol().equalsIgnoreCase("ADMIN")) {
            nuevo.setRol("MAYOR");
        } else {
            nuevo.setRol(nuevo.getRol().toUpperCase());
        }

        usuarioRepository.save(nuevo);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario creado con éxito"));
    }

    @PostMapping("/intereses")
    public ResponseEntity<?> guardarIntereses(@RequestBody Map<String, Object> payload) {
        String email = payload.get("email") != null ? ((String) payload.get("email")).toLowerCase().trim() : "";
        @SuppressWarnings("unchecked")
        java.util.List<String> nombresIntereses = (java.util.List<String>) payload.get("intereses");

        if (email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email obligatorio"));
        }

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = userOpt.get();
        java.util.Set<es.paloma.contacto.backend.model.Interes> intereses = new java.util.HashSet<>();

        if (nombresIntereses != null) {
            for (String nombre : nombresIntereses) {
                Optional<es.paloma.contacto.backend.model.Interes> interesOpt = interesRepository.findByNombre(nombre);
                interesOpt.ifPresent(intereses::add);
            }
        }

        usuario.setIntereses(intereses);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Intereses guardados"));
    }
}