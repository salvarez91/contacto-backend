package es.paloma.contacto.backend.aws;

import es.paloma.contacto.backend.exception.PeticionIncorrectaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class GestorObjetosS3 {

    private static final Logger log = LoggerFactory.getLogger(GestorObjetosS3.class);
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public GestorObjetosS3(S3Client s3Client, S3Presigner s3Presigner,
                           @Value("") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public String generarNombreUnico(Long usuarioId, String extensionOriginal) {
        String extension = normalizarExtension(extensionOriginal);
        return "usuarios/" + usuarioId + "/perfil/" + UUID.randomUUID() + "." + extension;
    }

    public String generarUrlSubida(String key, int minutosExpiracion) {
        return generarUrlSubida(key, obtenerContentType(key), minutosExpiracion);
    }

    public String generarUrlSubida(String key, String contentType, int minutosExpiracion) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(minutosExpiracion))
                .putObjectRequest(putObjectRequest)
                .build();
        return this.s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public String obtenerUrlLectura(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24))
                .getObjectRequest(getObjectRequest)
                .build();
        return this.s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void eliminarObjeto(String key) {
        if (key == null || key.isBlank()) {
            log.warn("Intento de eliminar objeto con clave nula o vacia");
            return;
        }
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        this.s3Client.deleteObject(deleteObjectRequest);
    }

    private String normalizarExtension(String extensionOriginal) {
        String extension = (extensionOriginal == null || extensionOriginal.isBlank()) ? "jpg" : extensionOriginal;
        extension = extension.trim().toLowerCase(Locale.ROOT);
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new PeticionIncorrectaException("Extension de imagen no permitida");
        }
        return extension;
    }

    private String obtenerContentType(String key) {
        int indicePunto = key == null ? -1 : key.lastIndexOf('.');
        if (indicePunto < 0 || indicePunto == key.length() - 1) {
            throw new PeticionIncorrectaException("La clave de imagen no tiene extension valida");
        }
        String extension = normalizarExtension(key.substring(indicePunto + 1));
        return switch (extension) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}