package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // Usamos trim() para evitar el error del tabulador \t detectado en los logs
        String email = credentials.get("email") != null ? credentials.get("email").trim() : "";
        String password = credentials.get("password") != null ? credentials.get("password").trim() : "";

        System.out.println("Intentando login para: " + email);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado en BD. Comparando claves...");

            if (passwordEncoder.matches(password, usuario.getPassword())) {
                String token = jwtUtil.generateToken(email);

                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("id", String.valueOf(usuario.getId()));
                response.put("rol", usuario.getRol() != null ? usuario.getRol() : "MAYOR");
                response.put("email", usuario.getEmail());

                return ResponseEntity.ok(response);
            } else {
                System.out.println("¡Error! La contraseña no coincide.");
            }
        } else {
            System.out.println("¡Error! No existe ningún usuario con email: " + email);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
    }

    // Cambiado de /registrar a /registro para coincidir con el ApiService de Android
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> datos) {
        String email = datos.get("email") != null ? datos.get("email").trim() : "";

        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(datos.get("nombre"));
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(datos.get("password")));
        nuevo.setRol(datos.get("rol") != null ? datos.get("rol").toUpperCase() : "MAYOR");

        usuarioRepository.save(nuevo);

        // Devolvemos un mapa vacío en lugar de .build() para que Retrofit no de error al parsear
        return ResponseEntity.ok(Map.of("mensaje", "Usuario creado con éxito"));
    }
}