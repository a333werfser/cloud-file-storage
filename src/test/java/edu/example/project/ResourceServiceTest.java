package edu.example.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.service.FolderService;
import edu.example.project.service.ResourceService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ResourceServiceTest {

    @Autowired
    MinioClient minioClient;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FolderService folderService;

    @Test
    void test() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object("file.txt")
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void test2() throws ResourceNotFoundException {
        resourceService.removeResource("fold/", 2);
    }

}
