package es.paloma.contacto.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private ResponseEntity<Object> crearRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Object> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PeticionIncorrectaException.class)
    public ResponseEntity<Object> manejarPeticionIncorrecta(PeticionIncorrectaException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Object> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> manejarAccesoDenegado(AccessDeniedException ex) {
        return crearRespuesta(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta accion");
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Object> manejarConflicto(ConflictoException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        
        String primerMensaje = errores.values().iterator().next();
        return crearRespuesta(HttpStatus.BAD_REQUEST, primerMensaje);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> manejarErrorGeneral(Exception ex) {
        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado en el servidor");
    }
}
