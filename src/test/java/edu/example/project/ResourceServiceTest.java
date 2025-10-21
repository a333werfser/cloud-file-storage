package edu.example.project;

import edu.example.project.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ResourceServiceTest {

    @Autowired
    ResourceService resourceService;

    @Test
    void test() {
    }

}
