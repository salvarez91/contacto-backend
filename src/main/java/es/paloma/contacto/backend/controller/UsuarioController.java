package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario actualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(actualizado.getNombre());
            usuario.setEmail(actualizado.getEmail());

            // Solo ciframos y actualizamos la contraseña si nos envían una nueva
            if (actualizado.getPassword() != null && !actualizado.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(actualizado.getPassword()));
            }

            usuario.setRol(actualizado.getRol());
            usuario.setFechaNacimiento(actualizado.getFechaNacimiento());
            usuario.setIntereses(actualizado.getIntereses()); // ¡Esta es la clave para el matching!

            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}/sugerencias")
    public ResponseEntity<List<Usuario>> getSugerencias(@PathVariable Long id) {
        return ResponseEntity.ok(matchingService.sugerirVoluntarios(id));
    }
}