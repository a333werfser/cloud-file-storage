package edu.example.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("minio.client")
@Getter
@Setter
public class MinioClientProperties {

    private String endpoint;

    private String user;

    private String password;

}
