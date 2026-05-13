package es.paloma.contacto.backend.dto;

public class ActualizarPerfilRequest {
    private String nombre;
    private String descripcion;
    private String puebloCiudad;
    private String fechaNacimiento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}