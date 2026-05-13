package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.dto.MensajeDTO;
import es.paloma.contacto.backend.exception.AccesoNoAutorizadoException;
import es.paloma.contacto.backend.exception.RecursoNoEncontradoException;
import es.paloma.contacto.backend.model.Mensaje;
import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.MensajeRepository;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{u1}/{u2}")
    public List<Mensaje> getHistorial(@PathVariable Long u1, @PathVariable Long u2) {
        return mensajeRepository.findConversacion(u1, u2);
    }

    @PostMapping
    @Transactional
    public Mensaje enviarMensaje(@RequestBody MensajeDTO mensajeDTO, Principal principal) {
        if (mensajeDTO == null || mensajeDTO.getReceptorId() == null ||
                mensajeDTO.getContenido() == null || mensajeDTO.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("Datos del mensaje incompletos");
        }

        Usuario emisor = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisorId(emisor.getId());
        mensaje.setReceptorId(mensajeDTO.getReceptorId());
        mensaje.setContenido(mensajeDTO.getContenido());
        mensaje.setFechaEnvio(LocalDateTime.now(ZoneOffset.UTC));

        Mensaje guardado = mensajeRepository.save(mensaje);

        mensajeDTO.setId(guardado.getId());
        mensajeDTO.setEmisorId(emisor.getId());
        mensajeDTO.setFechaEnvio(guardado.getFechaEnvio());
        messagingTemplate.convertAndSend("/topic/messages/" + mensajeDTO.getReceptorId(), mensajeDTO);

        return guardado;
    }

    @MessageMapping("/chat")
    @Transactional
    public void processMessage(MensajeDTO mensajeDTO, Principal principal) {
        if (mensajeDTO == null ||
                mensajeDTO.getEmisorId() == null || mensajeDTO.getEmisorId() <= 0 ||
                mensajeDTO.getReceptorId() == null || mensajeDTO.getReceptorId() <= 0 ||
                mensajeDTO.getContenido() == null || mensajeDTO.getContenido().trim().isEmpty()) {
            return;
        }

        Usuario emisor = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!emisor.getId().equals(mensajeDTO.getEmisorId())) {
            throw new AccesoNoAutorizadoException("No puedes enviar mensajes como otro usuario");
        }

        try {
            Mensaje mensaje = new Mensaje();
            mensaje.setEmisorId(mensajeDTO.getEmisorId());
            mensaje.setReceptorId(mensajeDTO.getReceptorId());
            mensaje.setContenido(mensajeDTO.getContenido());
            mensaje.setFechaEnvio(LocalDateTime.now(ZoneOffset.UTC));

            Mensaje guardado = mensajeRepository.save(mensaje);

            mensajeDTO.setId(guardado.getId());
            mensajeDTO.setFechaEnvio(guardado.getFechaEnvio());

            messagingTemplate.convertAndSend("/topic/messages/" + mensajeDTO.getReceptorId(), mensajeDTO);
        } catch (RuntimeException ex) {
            messagingTemplate.convertAndSend("/topic/errors/" + mensajeDTO.getEmisorId(), "No se pudo enviar el mensaje");
            throw ex;
        }
    }
}