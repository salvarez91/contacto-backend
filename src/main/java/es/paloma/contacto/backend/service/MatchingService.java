package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.MatchRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchRepository matchRepository;

    public List<Usuario> obtenerMisContactos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if ("MAYOR".equalsIgnoreCase(usuario.getRol())) {
            return matchRepository.findByMayorId(usuarioId).stream()
                    .filter(Match::isActive)
                    .map(Match::getVoluntario)
                    .collect(Collectors.toList());
        } else {
            return matchRepository.findByVoluntarioId(usuarioId).stream()
                    .filter(Match::isActive)
                    .map(Match::getMayor)
                    .collect(Collectors.toList());
        }
    }

    public List<Usuario> sugerirVoluntarios(Long mayorId) {
        Usuario mayor = usuarioRepository.findById(mayorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<Long> interesesMayorIds = mayor.getIntereses().stream()
                .map(Interes::getId)
                .collect(Collectors.toSet());

        Set<Long> idsConMatch = matchRepository.findByMayorId(mayorId).stream()
                .filter(Match::isActive)
                .map(match -> match.getVoluntario().getId())
                .collect(Collectors.toSet());

        return usuarioRepository.findAll().stream()
                .filter(u -> "VOLUNTARIO".equals(u.getRol()))
                .filter(v -> v.getIntereses().stream()
                        .anyMatch(i -> interesesMayorIds.contains(i.getId())))
                .filter(v -> !idsConMatch.contains(v.getId()))
                .collect(Collectors.toList());
    }
}