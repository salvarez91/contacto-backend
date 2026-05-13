package es.paloma.contacto.backend.dto;

public class MatchAdminDTO {
    private Long id;
    private String mayorNombre;
    private String voluntarioNombre;
    private String fechaCreacion;
    private boolean activo;

    public MatchAdminDTO(Long id, String mayorNombre, String voluntarioNombre, String fechaCreacion, boolean activo) {
        this.id = id;
        this.mayorNombre = mayorNombre;
        this.voluntarioNombre = voluntarioNombre;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getMayorNombre() {
        return mayorNombre;
    }

    public String getVoluntarioNombre() {
        return voluntarioNombre;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }
}