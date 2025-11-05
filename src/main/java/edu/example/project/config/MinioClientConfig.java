package edu.example.project.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(MinioClientProperties.class)
@RequiredArgsConstructor
@Configuration
public class MinioClientConfig {

    private final MinioClientProperties minioClientProperties;

    @Bean
    public MinioClient configuredMinioClient() {
        return MinioClient.builder()
                .endpoint(minioClientProperties.getEndpoint())
                .credentials(minioClientProperties.getUser(), minioClientProperties.getPassword())
                .build();
    }
}
