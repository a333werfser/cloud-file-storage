package edu.example.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.project.dto.ResourceDto;
import edu.example.project.exception.ResourceNotFoundException;
import edu.example.project.service.ResourceService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ResourceServiceTest {

    @Autowired
    ResourceService resourceService;

    @Autowired
    ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"fold/", "file.txt", "fold/file.txt", "folder/", "folder/file.txt"})
    void test(String path) throws ResourceNotFoundException {
        ResourceDto resourceDto = resourceService.getResourceInfo(path, 2);
    }

}
