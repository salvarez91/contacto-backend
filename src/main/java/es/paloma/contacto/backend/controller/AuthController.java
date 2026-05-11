package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.InteresRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> datos) {
        String email = datos.get("email") != null ? datos.get("email").toLowerCase().trim() : "";
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El email ya está registrado"));
        }
        Usuario nuevo = new Usuario();
        nuevo.setNombre(datos.get("nombre"));
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(datos.get("password")));
        nuevo.setRol(datos.get("rol") != null ? datos.get("rol").toUpperCase() : "MAYOR");
        usuarioRepository.save(nuevo);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario creado con éxito"));
    }

    @PostMapping("/intereses")
    public ResponseEntity<?> guardarIntereses(@RequestBody Map<String, Object> payload) {
        String email = payload.get("email") != null ? ((String) payload.get("email")).toLowerCase().trim() : "";
        @SuppressWarnings("unchecked")
        java.util.List<String> nombresIntereses = (java.util.List<String>) payload.get("intereses");
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Usuario usuario = userOpt.get();
        java.util.Set<es.paloma.contacto.backend.model.Interes> intereses = new java.util.HashSet<>();
        for (String nombre : nombresIntereses) {
            es.paloma.contacto.backend.model.Interes interes = interesRepository.findByNombre(nombre)
                    .orElseGet(() -> {
                        es.paloma.contacto.backend.model.Interes n = new es.paloma.contacto.backend.model.Interes();
                        n.setNombre(nombre);
                        return interesRepository.save(n);
                    });
            intereses.add(interes);
        }
        usuario.setIntereses(intereses);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Intereses guardados"));
    }
}