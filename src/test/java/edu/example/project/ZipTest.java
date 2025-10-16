package edu.example.project;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTest {

    @Test
    void test() throws IOException {
        Path pathToZip = Paths.get("where/a.zip");
        Path pathToFile = Paths.get("where/sooqa.txt");
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(pathToZip))) {
            zipOut.putNextEntry(new ZipEntry("file.txt"));
            zipOut.write(Files.readAllBytes(pathToFile));
            zipOut.closeEntry();
            zipOut.putNextEntry(new ZipEntry("folder/file.txt"));
            zipOut.write(Files.readAllBytes(pathToFile));
            zipOut.closeEntry();
            zipOut.putNextEntry(new ZipEntry("folder/zalupa.txt"));
            zipOut.write(Files.readAllBytes(pathToFile));
            zipOut.closeEntry();
        }
    }
}
