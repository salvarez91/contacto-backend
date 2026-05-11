package es.paloma.contacto.backend.aws;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class GestorObjetosS3 {

    private static final String NOMBRE_BUCKET_EN_S3 = "ffe-contacto-repositorio";
    private static final int TIEMPO_VIDA_MINUTOS_URL = 10;

    public String obtenerURLPutDocumentoEnS3(String claveDocumento) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(NOMBRE_BUCKET_EN_S3)
                .key(claveDocumento)
                .contentType("image/jpeg")
                .build();

        S3Presigner presigner = GestorClientesServiciosAWS.getClientePrefirmadorBucketS3();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(TIEMPO_VIDA_MINUTOS_URL))
                .putObjectRequest(putObjectRequest)
                .build();

        URL presignedUrl = presigner.presignPutObject(presignRequest).url();
        presigner.close();
        return presignedUrl.toString();
    }

    public String obtenerURLGetDocumentoEnS3(String claveDocumento) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(NOMBRE_BUCKET_EN_S3)
                .key(claveDocumento)
                .build();

        S3Presigner presigner = GestorClientesServiciosAWS.getClientePrefirmadorBucketS3();
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(builder -> builder
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(TIEMPO_VIDA_MINUTOS_URL))
        );

        URL presignedUrl = presignedRequest.url();
        presigner.close();
        return presignedUrl.toString();
    }
}