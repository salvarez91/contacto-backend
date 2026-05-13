package es.paloma.contacto.backend.dto;

public class RegistroResponse {
    private String mensaje;

    public RegistroResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}