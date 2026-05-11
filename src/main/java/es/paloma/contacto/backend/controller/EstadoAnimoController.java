package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.exception.ConflictoException;
import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.repository.AlertaRepository;
import es.paloma.contacto.backend.repository.EstadoAnimoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/estados-animo")
public class EstadoAnimoController {

    @Autowired
    private EstadoAnimoRepository estadoAnimoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @PostMapping
    public ResponseEntity<EstadoAnimo> registrar(@RequestBody EstadoAnimo estado) {
        if (estado.getUsuario() == null || estado.getUsuario().getId() == null) {
            throw new PeticionIncorrectaException("Falta el usuario");
        }

        LocalDate hoy = LocalDate.now();
        if (estadoAnimoRepository.existsByUsuarioIdAndFecha(estado.getUsuario().getId(), hoy)) {
            throw new ConflictoException("Ya registraste tu estado hoy");
        }

        estado.setFecha(hoy);
        EstadoAnimo nuevo = estadoAnimoRepository.save(estado);

        if (estado.getNivelEmocional() == 1) {
            List<EstadoAnimo> ultimos = estadoAnimoRepository.findByUsuarioIdOrderByFechaDesc(estado.getUsuario().getId());
            if (ultimos.size() >= 3) {
                List<EstadoAnimo> tresUltimos = ultimos.subList(0, 3);
                boolean todosTristes = tresUltimos.stream().allMatch(e -> e.getNivelEmocional() == 1);
                if (todosTristes) {
                    Alerta alerta = new Alerta();
                    alerta.setDescripcion("El usuario " + estado.getUsuario().getId() + " ha registrado 3 estados tristes consecutivos.");
                    alerta.setReferido(estado.getUsuario());
                    alerta.setFechaCreacion(LocalDateTime.now());
                    alerta.setVista(false);
                    alertaRepository.save(alerta);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping("/hoy/{usuarioId}")
    public ResponseEntity<Boolean> haRegistradoHoy(@PathVariable Long usuarioId) {
        boolean exists = estadoAnimoRepository.existsByUsuarioIdAndFecha(usuarioId, LocalDate.now());
        return ResponseEntity.ok(exists);
    }
}