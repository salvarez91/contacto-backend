package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public Page<Usuario> obtenerTodos(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String excluir) {
        Pageable pageable = PageRequest.of(page, size);
        if (excluir != null && !excluir.isBlank()) {
            return usuarioRepository.findByEmailNot(excluir.trim(), pageable);
        }
        return usuarioRepository.findAll(pageable);
    }
}