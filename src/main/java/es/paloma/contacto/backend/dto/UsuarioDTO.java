// UsuarioPerfilDTO.java
package es.paloma.contacto.backend.dto;

import java.util.List;

public record UsuarioPerfilDTO(
        Long id,
        String nombre,
        String email,
        String puebloCiudad,
        String descripcion,
        String fotoPerfilKey,
        List<String> intereses
) {
}