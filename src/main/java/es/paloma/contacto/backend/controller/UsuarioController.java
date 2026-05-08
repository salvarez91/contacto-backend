package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Usuario;
import es.paloma.contacto.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> obtenerTodos(@RequestParam(required = false) String excluir) {
        // Si recibimos un email para excluir, aplicamos trim por seguridad
        if (excluir != null && !excluir.isBlank()) {
            return usuarioRepository.findByEmailNot(excluir.trim());
        }
        // Si no hay parámetro, devuelve todos los usuarios
        return usuarioRepository.findAll();
    }
}