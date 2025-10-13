package edu.example.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minio.bucket")
@Component
@Getter
@Setter
public class BucketProperties {

    private String defaultName;

}
