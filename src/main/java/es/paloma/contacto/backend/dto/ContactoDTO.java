package es.paloma.contacto.backend.dto;

import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.model.Usuario;

public class ContactoDTO {
    private Long matchId;
    private Long id;
    private String nombre;
    private String email;
    private String rol;

    public ContactoDTO(Match match, Usuario contacto) {
        this.matchId = match.getId();
        this.id = contacto.getId();
        this.nombre = contacto.getNombre();
        this.email = contacto.getEmail();
        this.rol = contacto.getRol();
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}