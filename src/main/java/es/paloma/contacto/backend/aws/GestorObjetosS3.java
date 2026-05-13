package es.paloma.contacto.backend.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class GestorObjetosS3 {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public GestorObjetosS3(S3Client s3Client, S3Presigner s3Presigner,
                           @Value("${aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public String generarNombreUnico(Long usuarioId, String extensionOriginal) {
        String ext = (extensionOriginal == null || extensionOriginal.isBlank()) ? ".jpg" : extensionOriginal;
        if (!ext.startsWith(".")) ext = "." + ext;
        return "perfiles/perfil_" + usuarioId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
    }

    public String generarUrlSubida(String key, int minutosExpiracion) {
        return generarUrlSubida(key, "image/jpeg", minutosExpiracion);
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

        return s3Presigner.presignPutObject(presignRequest).url().toString();
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

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
