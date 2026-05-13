package es.paloma.contacto.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class InteresesRequest {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email vÃ¡lido")
    private String email;

    private List<String> intereses;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getIntereses() {
        return intereses;
    }

    public void setIntereses(List<String> intereses) {
        this.intereses = intereses;
    }
}