package es.paloma.contacto.backend.service;

import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.EstadoAnimoRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class EstadoAnimoService {

    @Autowired
    private EstadoAnimoRepository estadoAnimoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public EstadoAnimo registrar(EstadoAnimo estado, String email) {
        if (estado.getUsuario() == null || estado.getUsuario().getId() == null) {
            throw new PeticionIncorrectaException("Falta el usuario");
        }

        Usuario usuario = usuarioRepository.findById(estado.getUsuario().getId())
                .orElseThrow(() -> new PeticionIncorrectaException("Usuario invalido"));

        if (!usuario.getEmail().equals(email)) {
            throw new AccesoNoAutorizadoException("No puedes registrar estados de animo para otro usuario");
        }

        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        if (estadoAnimoRepository.existsByUsuarioIdAndFecha(usuario.getId(), hoy)) {
            throw new ConflictoException("Ya registraste tu estado hoy");
        }

        estado.setUsuario(usuario);
        estado.setFecha(hoy);
        EstadoAnimo nuevo = estadoAnimoRepository.save(estado);
        crearAlertaSiHayTresEstadosTristes(nuevo);
        return nuevo;
    }

    public boolean haRegistradoHoy(Long usuarioId, String email) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new PeticionIncorrectaException("Usuario invalido"));

        if (!usuario.getEmail().equals(email)) {
            throw new AccesoNoAutorizadoException("Acceso denegado");
        }

        return estadoAnimoRepository.existsByUsuarioIdAndFecha(usuarioId, LocalDate.now(ZoneOffset.UTC));
    }

    private void crearAlertaSiHayTresEstadosTristes(EstadoAnimo nuevo) {
        if (nuevo.getNivelEmocional() != 1) {
            return;
        }

        List<EstadoAnimo> ultimos = estadoAnimoRepository.findByUsuarioIdOrderByFechaDesc(nuevo.getUsuario().getId());
        if (ultimos.size() < 3 || ultimos.stream().limit(3).anyMatch(e -> e.getNivelEmocional() != 1)) {
            return;
        }

        Alerta alerta = new Alerta();
        alerta.setDescripcion("El usuario " + nuevo.getUsuario().getNombre() + " ha registrado 3 estados tristes consecutivos.");
        alerta.setReferido(nuevo.getUsuario());
        alerta.setFechaCreacion(LocalDateTime.now(ZoneOffset.UTC));
        alerta.setVista(false);
        alertaRepository.save(alerta);
    }
}
