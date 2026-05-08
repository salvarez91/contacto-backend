package es.paloma.contacto.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("emisorId") // Fuerza el nombre exacto que manda Android
    @Column(name = "emisor_id")
    private Long emisorId;

    @JsonProperty("receptorId") // Fuerza el nombre exacto que manda Android
    @Column(name = "receptor_id")
    private Long receptorId;

    private String contenido;
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    // GETTERS Y SETTERS (Asegúrate de que existan todos)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmisorId() { return emisorId; }
    public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }
    public Long getReceptorId() { return receptorId; }
    public void setReceptorId(Long receptorId) { this.receptorId = receptorId; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}