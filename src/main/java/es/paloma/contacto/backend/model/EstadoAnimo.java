package es.paloma.contacto.backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "estado_animo", indexes = {
        @Index(name = "idx_estado_usuario_fecha", columnList = "usuario_id, fecha")
})
public class EstadoAnimo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nivel_emocional", nullable = false, columnDefinition = "integer check (nivel_emocional >= 1 and nivel_emocional <= 5)")
    private int nivelEmocional;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public EstadoAnimo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNivelEmocional() {
        return nivelEmocional;
    }

    public void setNivelEmocional(int nivelEmocional) {
        this.nivelEmocional = nivelEmocional;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}