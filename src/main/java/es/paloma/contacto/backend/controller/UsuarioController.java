package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.security.JwtUtil;
import es.paloma.contacto.backend.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(matchingService.obtenerMisContactos(usuario.getId()));
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