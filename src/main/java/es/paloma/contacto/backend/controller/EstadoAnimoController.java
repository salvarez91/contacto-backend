package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.service.EstadoAnimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/estados-animo")
public class EstadoAnimoController {

    @Autowired
    private EstadoAnimoService estadoAnimoService;

    @PostMapping
    public ResponseEntity<EstadoAnimo> registrar(@RequestBody EstadoAnimo estado, Principal principal) {
        EstadoAnimo nuevo = estadoAnimoService.registrar(estado, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping("/hoy/{usuarioId}")
    public ResponseEntity<Boolean> haRegistradoHoy(@PathVariable Long usuarioId, Principal principal) {
        return ResponseEntity.ok(estadoAnimoService.haRegistradoHoy(usuarioId, principal.getName()));
    }
}
