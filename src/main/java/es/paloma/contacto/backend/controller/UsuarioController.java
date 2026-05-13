package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import es.paloma.contacto.backend.dto.ActualizarPerfilRequest;
import es.paloma.contacto.backend.dto.ContactoDTO;
import es.paloma.contacto.backend.dto.UsuarioPerfilDTO;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import es.paloma.contacto.backend.service.MatchingService;
import es.paloma.contacto.backend.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final long MAX_FOTO_BYTES = 5L * 1024L * 1024L;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private GestorObjetosS3 gestorObjetosS3;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public Page<Usuario> obtenerTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String excluir) {
        Pageable pageable = PageRequest.of(page, size);
        if (excluir != null && !excluir.isBlank()) {
            return usuarioRepository.findByEmailNot(excluir.trim(), pageable);
        }
        return usuarioRepository.findAll(pageable);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mis-contactos")
    public ResponseEntity<List<ContactoDTO>> getMisContactos(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return ResponseEntity.ok(matchingService.obtenerMisContactos(usuario.getId()));
    }

    @GetMapping("/mi-perfil")
    public ResponseEntity<UsuarioPerfilDTO> getMiPerfil(Principal principal) {
        UsuarioPerfilDTO perfil = usuarioService.obtenerPerfil(principal.getName());
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/perfil")
    @Transactional
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody ActualizarPerfilRequest datos, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (datos.getNombre() != null) usuario.setNombre(datos.getNombre());
        if (datos.getDescripcion() != null) usuario.setDescripcion(datos.getDescripcion());
        if (datos.getPuebloCiudad() != null) usuario.setPuebloCiudad(datos.getPuebloCiudad());
        if (datos.getFechaNacimiento() != null && !datos.getFechaNacimiento().isBlank()) {
            try {
                usuario.setFechaNacimiento(LocalDate.parse(datos.getFechaNacimiento()));
            } catch (DateTimeParseException e) {
                throw new PeticionIncorrectaException("Formato de fecha invalido");
            }
        }
        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    @GetMapping("/upload-url-foto")
    public ResponseEntity<Map<String, String>> getUploadUrl(Principal principal,
                                                            @RequestParam(required = false) String extension,
                                                            @RequestParam(required = false) Long sizeBytes) {
        if (sizeBytes != null && sizeBytes > MAX_FOTO_BYTES) {
            throw new PeticionIncorrectaException("La imagen no puede superar 5 MB");
        }
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        String ext = (extension != null && !extension.isBlank()) ? extension : "jpg";
        String key = gestorObjetosS3.generarNombreUnico(usuario.getId(), ext);
        String uploadUrl = gestorObjetosS3.generarUrlSubida(key, 15);
        usuario.setFotoPerfilKey(key);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of(
                "uploadUrl", uploadUrl,
                "key", key,
                "maxBytes", String.valueOf(MAX_FOTO_BYTES)
        ));
    }

    @GetMapping("/read-url-foto")
    public ResponseEntity<Map<String, String>> getReadUrl(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (usuario.getFotoPerfilKey() == null || usuario.getFotoPerfilKey().isBlank()) {
            return ResponseEntity.ok(Map.of("url", ""));
        }
        String url = gestorObjetosS3.obtenerUrlLectura(usuario.getFotoPerfilKey());
        return ResponseEntity.ok(Map.of("url", url));
    }
}