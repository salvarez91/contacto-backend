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
import org.springframework.data.domain.PageRequest;
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
                .orElseThrow(() -> new PeticionIncorrectaException("Usuario inválido"));

        if (!usuario.getEmail().equals(email)) {
            throw new AccesoNoAutorizadoException("No puedes registrar estados de ánimo para otro usuario");
        }

        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        if (estadoAnimoRepository.existsByUsuarioIdAndFecha(usuario.getId(), hoy)) {
            throw new ConflictoException("Ya registraste tu estado hoy");
        }

        estado.setUsuario(usuario);
        estado.setFecha(hoy);
        EstadoAnimo nuevo = estadoAnimoRepository.save(estado);

        verificarRachaTristeza(usuario);

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

    private void verificarRachaTristeza(Usuario usuario) {
        List<EstadoAnimo> ultimos = estadoAnimoRepository.findByUsuarioIdOrderByFechaDesc(
                usuario.getId(), PageRequest.of(0, 3));

        if (ultimos.size() == 3) {
            boolean rachaTriste = ultimos.stream().allMatch(e -> e.getNivelEmocional() == 1);

            if (rachaTriste) {
                crearAlertaTristeza(usuario);
            }
        }
    }

    private void crearAlertaTristeza(Usuario usuario) {
        String desc = "El usuario " + usuario.getNombre() + " ha registrado 3 estados tristes consecutivos.";
        LocalDateTime hace24Horas = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);

        boolean yaAlertado = alertaRepository.existsByReferidoIdAndDescripcionAndFechaCreacionAfter(
                usuario.getId(), desc, hace24Horas);

        if (!yaAlertado) {
            Alerta alerta = new Alerta();
            alerta.setReferido(usuario);
            alerta.setDescripcion(desc);
            alerta.setFechaCreacion(LocalDateTime.now(ZoneOffset.UTC));
            alerta.setVista(false);
            alertaRepository.save(alerta);
        }
    }
}