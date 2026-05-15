package es.paloma.contacto.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email vÃ¡lido")
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @NotBlank(message = "La contraseÃ±a es obligatoria")
    @Size(min = 6, message = "La contraseÃ±a debe tener al menos 6 caracteres")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(length = 200)
    private String descripcion;

    @Column(length = 100)
    private String puebloCiudad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "foto_perfil_key")
    private String fotoPerfilKey;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_intereses",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "interes_id")
    )
    private Set<Interes> intereses = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "referido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Alerta> alertas = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EstadoAnimo> estadosAnimo = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "mayor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> matchesComoMayor = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "voluntario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> matchesComoVoluntario = new HashSet<>();

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPuebloCiudad() {
        return puebloCiudad;
    }

    public void setPuebloCiudad(String puebloCiudad) {
        this.puebloCiudad = puebloCiudad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getFotoPerfilKey() {
        return fotoPerfilKey;
    }

    public void setFotoPerfilKey(String fotoPerfilKey) {
        this.fotoPerfilKey = fotoPerfilKey;
    }

    public Set<Interes> getIntereses() {
        return intereses;
    }

    public void setIntereses(Set<Interes> intereses) {
        this.intereses = intereses;
    }
}