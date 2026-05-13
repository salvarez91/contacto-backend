package es.paloma.contacto.backend.dto;

public class LoginResponse {
    private String token;
    private String id;
    private String rol;
    private String email;

    public LoginResponse(String token, String id, String rol, String email) {
        this.token = token;
        this.id = id;
        this.rol = rol;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

    public String getEmail() {
        return email;
    }
}