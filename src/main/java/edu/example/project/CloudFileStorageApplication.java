package edu.example.project;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "CloudFileStorage API",
				version = "1.0",
				description = "API for managing files and folders in CloudFileStorageApplication"
		)
)
@SecurityScheme(
		name = "Session-based",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.COOKIE,
		paramName = "JSESSIONID"
)
@SpringBootApplication
public class CloudFileStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudFileStorageApplication.class, args);
	}

}
