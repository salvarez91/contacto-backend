package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.service.EstadoAnimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/estados-animo")
public class EstadoAnimoController {

    @Autowired
    private EstadoAnimoService estadoAnimoService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody EstadoAnimo estado, Principal principal) {
        EstadoAnimo nuevo = estadoAnimoService.registrar(estado, principal.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("id", nuevo.getId());
        response.put("nivelEmocional", nuevo.getNivelEmocional());
        response.put("fecha", nuevo.getFecha());
        response.put("usuarioId", nuevo.getUsuario().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/hoy/{usuarioId}")
    public ResponseEntity<Boolean> haRegistradoHoy(@PathVariable Long usuarioId, Principal principal) {
        return ResponseEntity.ok(estadoAnimoService.haRegistradoHoy(usuarioId, principal.getName()));
    }
}