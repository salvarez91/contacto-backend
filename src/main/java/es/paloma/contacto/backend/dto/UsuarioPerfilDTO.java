package es.paloma.contacto.backend.dto;

import es.paloma.contacto.backend.model.Interes;

import java.util.List;

public record UsuarioPerfilDTO(
        Long id,
        String nombre,
        String email,
        String puebloCiudad,
        String descripcion,
        String fotoPerfilKey,
        String fechaNacimiento,
        List<String> intereses
) {
}