package es.paloma.contacto.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PeticionIncorrectaException extends RuntimeException {
    public PeticionIncorrectaException(String mensaje) {
        super(mensaje);
    }
}