package es.paloma.contacto.backend.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class GestorClientesServiciosAWS {

    public final static Region REGION_AWS = Region.US_EAST_1;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(REGION_AWS)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}