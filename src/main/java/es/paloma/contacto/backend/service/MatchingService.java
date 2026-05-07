package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.model.Usuario;
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

    public List<Usuario> sugerirVoluntarios(Long mayorId) {
        Usuario mayor = usuarioRepository.findById(mayorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<Long> interesesMayorIds = mayor.getIntereses().stream()
                .map(Interes::getId)
                .collect(Collectors.toSet());

        return usuarioRepository.findAll().stream()
                .filter(u -> "VOLUNTARIO".equals(u.getRol()))
                .filter(v -> v.getIntereses().stream()
                        .anyMatch(i -> interesesMayorIds.contains(i.getId())))
                .collect(Collectors.toList());
    }
}