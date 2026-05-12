package es.paloma.contacto.backend.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class GestorObjetosS3 {

    private static final String NOMBRE_BUCKET_EN_S3 = "ffe-contacto-repositorio";
    private static final int TIEMPO_VIDA_MINUTOS_URL = 10;

    @Autowired
    private S3Presigner presigner;

    public String obtenerURLPutDocumentoEnS3(String claveDocumento, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(NOMBRE_BUCKET_EN_S3)
                .key(claveDocumento)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(TIEMPO_VIDA_MINUTOS_URL))
                .putObjectRequest(putObjectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url().toString();
    }

    public String obtenerURLGetDocumentoEnS3(String claveDocumento) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(NOMBRE_BUCKET_EN_S3)
                .key(claveDocumento)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(builder -> builder
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(TIEMPO_VIDA_MINUTOS_URL))
        );

        return presignedRequest.url().toString();
    }
}