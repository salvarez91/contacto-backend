package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Mensaje;
import es.paloma.contacto.backend.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @GetMapping("/match/{matchId}")
    public List<Mensaje> getMensajesByMatchId(@PathVariable Long matchId) {
        return mensajeRepository.findByMatchIdOrderByTimestampAsc(matchId);
    }

    @PostMapping
    public ResponseEntity<Mensaje> createMensaje(@RequestBody Mensaje mensaje) {
        Mensaje nuevoMensaje = mensajeRepository.save(mensaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMensaje);
    }
}