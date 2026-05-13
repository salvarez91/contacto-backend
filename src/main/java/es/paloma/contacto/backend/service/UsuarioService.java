package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.EstadoAnimoRepository;
import es.paloma.contacto.backend.repository.MatchRepository;
import es.paloma.contacto.backend.repository.MensajeRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
