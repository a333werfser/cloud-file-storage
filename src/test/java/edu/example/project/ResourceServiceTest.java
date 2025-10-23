package edu.example.project;

import edu.example.project.config.BucketProperties;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceAlreadyExistsException;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.service.ResourceService;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ResourceServiceTest {

    @Autowired
    ResourceService resourceService;

    @Autowired
    MinioClient minioClient;

    @Autowired
    BucketProperties bucketProperties;

    @BeforeEach
    void clearTestBucket() {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        try {
            for (Result<Item> result : minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .recursive(true)
                            .build()
            )) {
                Item item = result.get();
                deleteObjects.add(new DeleteObject(item.objectName()));
            }
            if (deleteObjects.isEmpty()) {
                return;
            }
            for (Result<DeleteError> result : minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketProperties.getDefaultName())
                            .objects(deleteObjects)
                            .build()
            )) {
                result.get();
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    List<MultipartFile> getFilesListWithMockedOne(String originalFileName) {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile("file", originalFileName, "text/plain", "mockedText".getBytes())
        );
        return files;
    }

    List<MultipartFile> getFilesListWithMockedFiles() {
        List<MultipartFile> files = new ArrayList<>();
        int fileIndex = 1;
        for (int i = 0; i < 10; i++) {
            files.add(
                    new MockMultipartFile(
                            "file",
                            "file" + fileIndex + ".txt",
                            "text/plain",
                            "mockedText".getBytes()
                    )
            );
            fileIndex++;
        }
        return files;
    }

    @Test
    void whenCreateFolder_thenFolderAppear() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        String path = "path/";
        resourceService.createFolder(userId, path);
        resourceService.getResourceInfo(userId, path);
    }

    @Test
    void whenCreateFolderWhichAlreadyExists_thenThrowException() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        String path = "path/";
        resourceService.createFolder(userId, path);
        assertThrows(ResourceAlreadyExistsException.class, () -> resourceService.createFolder(userId, path));
    }

    @Test
    void whenCreateFolderAlongInvalidPath_thenThrowException() {
        int userId = 1;
        String path = "path/folder/";
        assertThrows(ResourceNotFoundException.class, () -> resourceService.createFolder(userId, path));
    }

    @Test
    void whenUploadResource_thenResourceAppear() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedOne("folder/file.txt");
        int userId = 1;

        resourceService.uploadResources(userId, "", files);
    }

    @Test
    void whenUploadResourceAlongThePathThatNotExists_thenThrowException() {
        List<MultipartFile> files = getFilesListWithMockedOne("folder/file.txt");
        int userId = 1;
        String invalidPath = "path/";

        assertThrows(ResourceNotFoundException.class, () -> resourceService.uploadResources(userId, invalidPath, files));
    }

    @Test
    void whenUploadResourceThatAlreadyExistsAlongThePath_thenThrowException() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedOne("folder/file.txt");
        int userId = 1;

        resourceService.uploadResources(userId, "", files);
        assertThrows(ResourceAlreadyExistsException.class, () -> resourceService.uploadResources(userId, "", files));
    }

    @Test
    void whenUploadFolder_thenAllFoldersShouldExist() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        String fileName = "folder/f1/f2/f3/f4/file.txt";
        List<MultipartFile> files = getFilesListWithMockedOne(fileName);
        int userId = 1;

        resourceService.uploadResources(userId, "", files);

        String[] names = fileName.split("/");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < names.length - 1; i++) {
            name.append(names[i]).append("/");
            resourceService.getResourceInfo(userId, name.toString());
        }
    }

    @Test
    void shouldRemoveFile() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedOne("file.txt");
        int userId = 1;

        resourceService.uploadResources(userId, "", files);
        resourceService.removeResource(userId, "file.txt");
        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceInfo(userId, "file.txt"));
    }

    @Test
    void shouldRemoveFolder() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        String path = "folder/";
        resourceService.createFolder(userId, path);
        resourceService.removeResource(userId, path);
        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceInfo(userId, path));
    }

    @Test
    void shouldRemoveFilledFolder() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedFiles();
        String path = "folder/";
        int userId = 1;

        resourceService.createFolder(userId, path);
        resourceService.uploadResources(userId, path, files);
        resourceService.removeResource(userId, path);
        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceInfo(userId, path));
    }

    @Test
    void shouldMoveFile() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedOne("file.txt");
        int userId = 1;

        resourceService.createFolder(userId, "from/");
        resourceService.createFolder(userId, "to/");
        resourceService.uploadResources(userId, "from/", files);
        resourceService.moveResource(userId, "from/file.txt", "to/file.txt");
        resourceService.getResourceInfo(userId, "to/file.txt");
    }

    @Test
    void shouldRenameFile() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedOne("file.txt");
        int userId = 1;
        resourceService.uploadResources(userId, "", files);
        resourceService.moveResource(userId, "file.txt", "renamed.txt");
        resourceService.getResourceInfo(userId, "renamed.txt");
    }

    @Test
    void shouldMoveEmptyFolder() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        resourceService.createFolder(userId, "folder/");
        resourceService.createFolder(userId, "destination/");
        resourceService.moveResource(userId, "folder/", "destination/folder/");
        resourceService.getResourceInfo(userId, "destination/folder/");
    }

    @Test
    void shouldMoveFilledFolder() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedFiles();
        int userId = 1;
        resourceService.createFolder(userId, "folder/");
        resourceService.createFolder(userId, "folder/from/");
        resourceService.uploadResources(userId, "folder/from/", files);
        resourceService.moveResource(userId, "folder/from/", "from/");
        resourceService.getResourceInfo(userId, "from/");
    }

    @Test
    void givenPathHasThisFolderName_whenMoveFolder_thenThrowException() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        resourceService.createFolder(userId, "folder/");
        resourceService.createFolder(userId, "folder/from/");
        resourceService.createFolder(userId, "from/");
        assertThrows(ResourceAlreadyExistsException.class, () -> resourceService.moveResource(userId, "folder/from/", "from/"));
    }

    @Test
    void whenTargetPathNotExists_thenThrowException() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        resourceService.createFolder(userId, "folder/");
        assertThrows(ResourceNotFoundException.class, () -> resourceService.moveResource(userId, "folder/", "directory/f1/"));
    }

    @Test
    void shouldRenameFolder() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        int userId = 1;
        resourceService.createFolder(userId, "folder/");
        resourceService.moveResource(userId, "folder/", "directory/");
        resourceService.getResourceInfo(userId, "directory/");
    }

    @Test
    void whenSearchByPrefix_thenResultsShouldStartWithPrefix() throws ResourceAlreadyExistsException, ResourceNotFoundException {
        List<MultipartFile> files = getFilesListWithMockedFiles();
        int userId = 1;

        resourceService.createFolder(userId, "folder/");
        resourceService.uploadResources(userId, "folder/", files);
        List<ResourceDto> list = resourceService.getResourcesInfo(userId, "f");

        for (ResourceDto item : list) {
            String key = item.getPath() + item.getName();
            assertTrue(key.startsWith("f"));
        }
    }

    @Test
    void whenGetFolderContents_thenResultShouldNotIncludeFolderItself() throws ResourceNotFoundException, ResourceAlreadyExistsException {
        List<MultipartFile> files = getFilesListWithMockedFiles();
        int userId = 1;

        resourceService.createFolder(userId, "folder/");
        resourceService.uploadResources(userId, "folder/", files);

        List<ResourceDto> list = resourceService.getFolderContents(userId, "folder/");
        assertEquals(files.size(), list.size());
    }

    @Test
    void whenGetResourceInfo_thenMapCorrectly() throws ResourceNotFoundException, ResourceAlreadyExistsException {
        List<MultipartFile> files = getFilesListWithMockedOne("file.txt");
        int userId = 1;
        resourceService.createFolder(1,"folder/");
        resourceService.uploadResources(1, "", files);

        var folder = resourceService.getResourceInfo(userId, "folder/");
        var file = resourceService.getResourceInfo(userId, "file.txt");

        assertAll(
                () -> assertEquals("", folder.getPath()),
                () -> assertEquals("folder/", folder.getName()),
                () -> assertNull(folder.getSize()),
                () -> assertEquals("DIRECTORY", folder.getType())
        );
        assertAll(
                () -> assertEquals("", file.getPath()),
                () -> assertEquals("file.txt", file.getName()),
                () -> assertTrue(file.getSize() > 0),
                () -> assertEquals("FILE", file.getType())
        );
    }

    @Test
    void whenDownloadFilledFolder_thenDownloadSameContentZip() throws ResourceAlreadyExistsException, ResourceNotFoundException, IOException {
        List<MultipartFile> files = getFilesListWithMockedFiles();
        int userId = 1;

        resourceService.createFolder(userId, "folder/");
        resourceService.createFolder(userId, "folder/dir/");
        resourceService.createFolder(userId, "folder/another/");

        resourceService.uploadResources(userId, "folder/", files);
        resourceService.uploadResources(userId, "folder/dir/", files);
        resourceService.uploadResources(userId, "folder/another/", files);

        byte[] zip = resourceService.getResourceBinaryContent(userId, "folder/");

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(zip);
             ZipInputStream zipIn = new ZipInputStream(byteIn)
        ) {
            Set<String> fileNames = new HashSet<>();
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                fileNames.add(entry.getName());
            }
            assertTrue(fileNames.contains("folder/file1.txt"));
            assertTrue(fileNames.contains("folder/dir/file1.txt"));
            assertTrue(fileNames.contains("folder/another/file1.txt"));
        }
    }

    @Test
    void whenDownloadEmptyFolder_thenDownloadEmptyZip() throws ResourceAlreadyExistsException, ResourceNotFoundException, IOException {
        int userId = 1;

        resourceService.createFolder(userId, "folder/");

        byte[] zip = resourceService.getResourceBinaryContent(userId, "folder/");

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(zip);
             ZipInputStream zipIn = new ZipInputStream(byteIn)
        ) {
            assertNull(zipIn.getNextEntry());
        }
    }

    @Test
    void shouldDownloadFile() throws ResourceAlreadyExistsException, ResourceNotFoundException, IOException {
        List<MultipartFile> files = getFilesListWithMockedOne("file.txt");
        int userId = 1;

        resourceService.uploadResources(userId, "", files);

        byte[] zip = resourceService.getResourceBinaryContent(userId, "file.txt");
        assertArrayEquals("mockedText".getBytes(), zip);
    }
}
