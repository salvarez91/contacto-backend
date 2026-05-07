package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.EstadoAnimo;
import es.paloma.contacto.backend.repository.EstadoAnimoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-animo")
public class EstadoAnimoController {

    @Autowired
    private EstadoAnimoRepository estadoAnimoRepository;

    @GetMapping("/usuario/{usuarioId}")
    public List<EstadoAnimo> getEstadosAnimoByUsuarioId(@PathVariable Long usuarioId) {
        return estadoAnimoRepository.findByUsuarioId(usuarioId);
    }

    @PostMapping
    public ResponseEntity<EstadoAnimo> createEstadoAnimo(@RequestBody EstadoAnimo estadoAnimo) {
        EstadoAnimo nuevoEstadoAnimo = estadoAnimoRepository.save(estadoAnimo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEstadoAnimo);
    }
}