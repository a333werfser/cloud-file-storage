package edu.example.project.config;

import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestMinioClientConfig {

    private static final DockerImageName MINIO_IMAGE_NAME = DockerImageName.parse("quay.io/minio/minio");

    @Bean
    public MinIOContainer configuredMinioContainer() {
        return new MinIOContainer(MINIO_IMAGE_NAME);
    }

    @Bean
    public MinioClient minioClient(MinIOContainer container) {
        return MinioClient.builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
    }

}
