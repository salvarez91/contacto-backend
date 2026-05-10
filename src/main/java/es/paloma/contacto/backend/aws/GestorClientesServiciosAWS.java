package es.paloma.contacto.backend.aws;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class GestorClientesServiciosAWS {
    private static final String PERFIL_CREDENCIALES_AWS = "default";

    public final static Region REGION_AWS = Region.US_EAST_1;

    private GestorClientesServiciosAWS() {
    }

    public static S3Presigner getClientePrefirmadorBucketS3() {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create(PERFIL_CREDENCIALES_AWS);

        return S3Presigner.builder()
                .region(REGION_AWS)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}