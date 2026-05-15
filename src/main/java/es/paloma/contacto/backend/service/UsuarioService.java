package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import es.paloma.contacto.backend.dto.ActualizarPerfilRequest;
import es.paloma.contacto.backend.dto.UsuarioPerfilDTO;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UsuarioService {

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
    private GestorObjetosS3 gestorObjetosS3;

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        mensajeRepository.borrarTodosLosMensajesDeUsuario(id);
        alertaRepository.deleteByReferidoId(id);
        estadoAnimoRepository.deleteByUsuarioId(id);
        matchRepository.deleteByMayorIdOrVoluntarioId(id, id);
        if (usuario.getFotoPerfilKey() != null) {
            gestorObjetosS3.eliminarObjeto(usuario.getFotoPerfilKey());
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilDTO obtenerPerfil(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        return new UsuarioPerfilDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getPuebloCiudad() != null ? u.getPuebloCiudad() : "No especificado",
                u.getDescripcion() != null ? u.getDescripcion() : "Sin descripción",
                u.getFotoPerfilKey(),
                u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "",
                u.getIntereses().stream().map(es.paloma.contacto.backend.model.Interes::getNombre).toList()
        );
    }

    @Transactional
    public UsuarioPerfilDTO actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (request.getNombre() != null) u.setNombre(request.getNombre());
        if (request.getDescripcion() != null) u.setDescripcion(request.getDescripcion());
        if (request.getPuebloCiudad() != null) u.setPuebloCiudad(request.getPuebloCiudad());
        if (request.getFechaNacimiento() != null && !request.getFechaNacimiento().isBlank()) {
            u.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
        }

        usuarioRepository.save(u);

        return new UsuarioPerfilDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getPuebloCiudad() != null ? u.getPuebloCiudad() : "No especificado",
                u.getDescripcion() != null ? u.getDescripcion() : "Sin descripción",
                u.getFotoPerfilKey(),
                u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "",
                u.getIntereses().stream().map(es.paloma.contacto.backend.model.Interes::getNombre).toList()
        );
    }
}