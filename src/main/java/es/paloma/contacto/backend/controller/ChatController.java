package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.dto.MensajeDTO;
import es.paloma.contacto.backend.model.Mensaje;
import es.paloma.contacto.backend.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{u1}/{u2}")
    public List<Mensaje> getHistorial(@PathVariable Long u1, @PathVariable Long u2) {
        return mensajeRepository.findConversacion(u1, u2);
    }

    @MessageMapping("/chat")
    public void processMessage(MensajeDTO mensajeDTO) {
        if (mensajeDTO == null || 
            mensajeDTO.getEmisorId() == null || mensajeDTO.getEmisorId() <= 0 ||
            mensajeDTO.getReceptorId() == null || mensajeDTO.getReceptorId() <= 0 ||
            mensajeDTO.getContenido() == null || mensajeDTO.getContenido().trim().isEmpty()) {
            return;
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisorId(mensajeDTO.getEmisorId());
        mensaje.setReceptorId(mensajeDTO.getReceptorId());
        mensaje.setContenido(mensajeDTO.getContenido());
        mensaje.setFechaEnvio(LocalDateTime.now(ZoneOffset.UTC));

        Mensaje guardado = mensajeRepository.save(mensaje);

        mensajeDTO.setId(guardado.getId());
        mensajeDTO.setFechaEnvio(guardado.getFechaEnvio());

        messagingTemplate.convertAndSend("/topic/messages/" + mensajeDTO.getReceptorId(), mensajeDTO);
    }
}