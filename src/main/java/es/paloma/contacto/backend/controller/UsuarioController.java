package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import es.paloma.contacto.backend.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Usuario> obtenerTodos(@RequestParam(required = false) String excluir) {
        if (excluir != null && !excluir.isBlank()) {
            return usuarioRepository.findByEmailNot(excluir.trim());
        }
        return usuarioRepository.findAll();
    }

    @GetMapping("/mis-contactos")
    public ResponseEntity<List<Usuario>> getMisContactos(@RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return ResponseEntity.ok(matchingService.obtenerMisContactos(usuario.getId()));
    }

    @GetMapping("/mi-perfil")
    public ResponseEntity<Usuario> getMiPerfil(@RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/perfil")
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody Map<String, Object> datos,
                                                    @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (datos.containsKey("nombre")) {
            usuario.setNombre((String) datos.get("nombre"));
        }
        if (datos.containsKey("descripcion")) {
            usuario.setDescripcion((String) datos.get("descripcion"));
        }
        if (datos.containsKey("puebloCiudad")) {
            usuario.setPuebloCiudad((String) datos.get("puebloCiudad"));
        }
        if (datos.containsKey("fechaNacimiento")) {
            usuario.setFechaNacimiento(LocalDate.parse((String) datos.get("fechaNacimiento")));
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/upload-url/{nombreArchivo}")
    public ResponseEntity<Map<String, String>> obtenerUrlSubidaS3(@PathVariable String nombreArchivo) {
        String url = "https://ffe-contacto-repositorio.s3.us-east-1.amazonaws.com/perfiles/" + nombreArchivo;
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/read-url/{nombreArchivo}")
    public ResponseEntity<Map<String, String>> obtenerUrlLecturaS3(@PathVariable String nombreArchivo) {
        String url = "https://ffe-contacto-repositorio.s3.us-east-1.amazonaws.com/perfiles/" + nombreArchivo;
        return ResponseEntity.ok(Map.of("url", url));
    }
}