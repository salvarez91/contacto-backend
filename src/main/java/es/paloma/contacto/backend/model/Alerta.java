package es.paloma.contacto.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean vista = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "referido_id", nullable = false)
    private Usuario referido;

    public Alerta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isVista() {
        return vista;
    }

    public void setVista(boolean vista) {
        this.vista = vista;
    }

    public Usuario getReferido() {
        return referido;
    }

    public void setReferido(Usuario referido) {
        this.referido = referido;
    }
}