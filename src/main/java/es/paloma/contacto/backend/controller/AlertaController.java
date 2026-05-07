package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Alerta;
import es.paloma.contacto.backend.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    @GetMapping("/usuario/{usuarioId}")
    public List<Alerta> getAlertasByUsuarioId(@PathVariable Long usuarioId) {
        return alertaRepository.findByReferidoId(usuarioId);
    }

    @GetMapping("/pendientes")
    public List<Alerta> getAlertasPendientes() {
        return alertaRepository.findByVistaFalse();
    }

    @PostMapping
    public ResponseEntity<Alerta> createAlerta(@RequestBody Alerta alerta) {
        Alerta nuevaAlerta = alertaRepository.save(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAlerta);
    }

    @PutMapping("/{id}/vista")
    public ResponseEntity<Alerta> marcarAlertaComoVista(@PathVariable Long id) {
        return alertaRepository.findById(id).map(alerta -> {
            alerta.setVista(true);
            Alerta actualizada = alertaRepository.save(alerta);
            return ResponseEntity.ok(actualizada);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}