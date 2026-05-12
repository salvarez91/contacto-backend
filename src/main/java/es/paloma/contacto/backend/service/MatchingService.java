package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.dto.ContactoDTO;
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

    public List<Usuario> sugerirVoluntarios(Long mayorId, String filtroInteres) {
        Usuario mayor = usuarioRepository.findById(mayorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<Long> interesesMayorIds = mayor.getIntereses().stream()
                .filter(i -> i != null && i.getId() != null)
                .map(Interes::getId)
                .collect(Collectors.toSet());

        Set<Long> idsConMatch = matchRepository.findByMayorId(mayorId).stream()
                .filter(Match::isActive)
                .map(match -> match.getVoluntario().getId())
                .collect(Collectors.toSet());

        // CORRECCIÓN ESCALABILIDAD: Llamada a BBDD en lugar de traer todos los usuarios a memoria.
        List<Usuario> voluntariosPosibles = usuarioRepository.findVoluntariosSugeridos(interesesMayorIds);

        return voluntariosPosibles.stream()
                .filter(v -> !idsConMatch.contains(v.getId()))
                .filter(v -> {
                    if (filtroInteres == null || filtroInteres.isBlank()) return true;
                    return v.getIntereses().stream()
                            .anyMatch(i -> i != null && i.getNombre() != null && i.getNombre().equalsIgnoreCase(filtroInteres.trim()));
                })
                // Limitamos a los primeros 20 para no sobrecargar el móvil
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<ContactoDTO> obtenerMisContactos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Match> matches;
        if ("MAYOR".equalsIgnoreCase(usuario.getRol())) {
            matches = matchRepository.findByMayorId(usuarioId);
        } else {
            matches = matchRepository.findByVoluntarioId(usuarioId);
        }

        return matches.stream()
                .filter(Match::isActive)
                .map(match -> {
                    Usuario contacto = "MAYOR".equalsIgnoreCase(usuario.getRol()) ?
                            match.getVoluntario() : match.getMayor();
                    return new ContactoDTO(match, contacto);
                })
                .collect(Collectors.toList());
    }
}