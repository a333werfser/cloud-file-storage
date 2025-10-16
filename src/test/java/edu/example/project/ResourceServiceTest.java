package edu.example.project;

import edu.example.project.config.BucketProperties;
import edu.example.project.service.FolderService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest
public class ResourceServiceTest {

    @Autowired
    MinioClient minioClient;

    @Autowired
    BucketProperties bucketProperties;

    @Autowired
    FolderService folderService;

    @Test
    void test() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .object("file.txt")
                        .build()
        )) {
            int i = 1;
        }
    }

}
