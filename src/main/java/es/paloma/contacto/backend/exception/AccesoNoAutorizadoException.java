package es.paloma.contacto.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}