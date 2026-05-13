package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import es.paloma.contacto.backend.dto.ActualizarPerfilRequest;
import es.paloma.contacto.backend.dto.ContactoDTO;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.*;
import es.paloma.contacto.backend.security.JwtUtil;
import es.paloma.contacto.backend.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private EstadoAnimoRepository estadoAnimoRepository;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GestorObjetosS3 gestorObjetosS3;

    @GetMapping
    public List<Usuario> obtenerTodos(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String excluir) {
        PageRequest pageable = PageRequest.of(page, size);
        if (excluir != null && !excluir.isBlank()) {
            return usuarioRepository.findByEmailNot(excluir.trim());
        }
        return usuarioRepository.findAll(pageable).getContent();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("No se puede eliminar: el usuario no existe");
        }

        try {
            mensajeRepository.borrarTodosLosMensajesDeUsuario(id);
            alertaRepository.deleteByReferidoId(id);
            estadoAnimoRepository.deleteByUsuarioId(id);
            matchRepository.deleteByMayorIdOrVoluntarioId(id, id);

            Usuario u = usuarioOpt.get();
            u.getIntereses().clear();
            usuarioRepository.save(u);

            usuarioRepository.deleteById(id);
            log.info("Usuario eliminado correctamente: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar el usuario con id={}", id, e);
            throw new PeticionIncorrectaException("No se pudo eliminar el usuario");
        }
    }

    @GetMapping("/mis-contactos")
    public ResponseEntity<List<ContactoDTO>> getMisContactos(@RequestHeader("Authorization") String authHeader) {
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody ActualizarPerfilRequest datos,
                                                    @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (datos.getNombre() != null) {
            if (datos.getNombre().length() > 100) throw new PeticionIncorrectaException("Nombre muy largo");
            usuario.setNombre(datos.getNombre());
        }
        if (datos.getDescripcion() != null) {
            if (datos.getDescripcion().length() > 200) throw new PeticionIncorrectaException("Descripción muy larga");
            usuario.setDescripcion(datos.getDescripcion());
        }
        if (datos.getPuebloCiudad() != null) {
            if (datos.getPuebloCiudad().length() > 100)
                throw new PeticionIncorrectaException("Nombre de ciudad muy largo");
            usuario.setPuebloCiudad(datos.getPuebloCiudad());
        }
        if (datos.getFechaNacimiento() != null && !datos.getFechaNacimiento().isBlank()) {
            try {
                usuario.setFechaNacimiento(LocalDate.parse(datos.getFechaNacimiento()));
            } catch (DateTimeParseException e) {
                throw new PeticionIncorrectaException("Formato de fecha inválido. Use yyyy-MM-dd");
            }
        }

        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    @GetMapping("/read-url/{nombreArchivo}")
    public ResponseEntity<Map<String, String>> obtenerUrlLecturaS3(@PathVariable String nombreArchivo) {
        String clave = "perfiles/" + nombreArchivo;
        String url = gestorObjetosS3.obtenerURLGetDocumentoEnS3(clave);
        return ResponseEntity.ok(Map.of("url", url));
    }
}