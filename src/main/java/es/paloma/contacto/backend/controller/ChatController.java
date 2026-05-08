package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Mensaje;
import es.paloma.contacto.backend.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @GetMapping("/{u1}/{u2}")
    public List<Mensaje> getHistorial(@PathVariable Long u1, @PathVariable Long u2) {
        return mensajeRepository.findConversacion(u1, u2);
    }

    @PostMapping("/enviar")
    public Mensaje enviarMensaje(@RequestBody Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }
}