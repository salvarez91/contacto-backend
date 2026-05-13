package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import es.paloma.contacto.backend.dto.UsuarioPerfilDTO;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        gestorObjetosS3.eliminarObjeto(usuario.getFotoPerfilKey());
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilDTO obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        List<String> intereses = usuario.getIntereses().stream()
                .map(Interes::getNombre)
                .collect(Collectors.toList());
        return new UsuarioPerfilDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getPuebloCiudad(),
                usuario.getDescripcion(),
                usuario.getFotoPerfilKey(),
                intereses
        );
    }
}