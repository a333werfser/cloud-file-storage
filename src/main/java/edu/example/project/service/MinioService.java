package edu.example.project.service;

import edu.example.project.config.BucketProperties;
import edu.example.project.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    private final BucketProperties bucketProperties;

    protected StatObjectResponse statObject(String path) throws ResourceNotFoundException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            if (exception instanceof ErrorResponseException && exception.getMessage().equals("Object does not exist")) {
                throw new ResourceNotFoundException("Object does not exist", exception);
            }
            throw new RuntimeException(exception);
        }
    }

    protected void putEmptyObject(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected Iterable<Result<Item>> listObjects(String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
    }

    protected Iterable<Result<Item>> listObjects(String prefix, int maxKeys) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .prefix(prefix)
                        .maxKeys(maxKeys)
                        .build()
        );
    }

    protected void removeObject(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void removeObjectsFrom(String prefix) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        listObjects(prefix).forEach(
                object -> {
                    try {
                        deleteObjects.add(new DeleteObject(object.get().objectName()));
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketProperties.getDefaultName())
                        .objects(deleteObjects)
                        .build()
        );
        for (Result<DeleteError> result : results) {
            try {
                result.get();
            } catch(Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public InputStream getObject(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}
