package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Mensaje;
import es.paloma.contacto.backend.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @MessageMapping("/enviar")
    @SendTo("/topic/mensajes")
    public Mensaje procesarMensaje(@Payload Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }

    @GetMapping("/api/mensajes/{u1}/{u2}")
    @ResponseBody
    public List<Mensaje> getHistorial(@PathVariable Long u1, @PathVariable Long u2) {
        return mensajeRepository.findByEmisorIdAndReceptorIdOrEmisorIdAndReceptorIdOrderByFechaEnvio(u1, u2, u2, u1);
    }
}