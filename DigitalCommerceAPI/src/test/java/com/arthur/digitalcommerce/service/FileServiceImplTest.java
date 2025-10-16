package com.arthur.digitalcommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir; // JUnit criará e limpará esta pasta temporária para nós

    @Test
    void uploadImage_shouldSucceed_andReturnRandomFileName() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );

        String uploadPath = tempDir.toString();
        String returnedFileName = fileService.uploadImage(uploadPath, mockFile);

        assertNotNull(returnedFileName);
        assertNotEquals("hello.txt", returnedFileName);
        assertTrue(returnedFileName.endsWith(".txt"));

        Path expectedFilePath = Paths.get(uploadPath + File.separator + returnedFileName);
        assertTrue(Files.exists(expectedFilePath));
        assertEquals("Hello, World!", Files.readString(expectedFilePath));
    }

    @Test
    void uploadImage_shouldThrowException_whenFileNameIsInvalid() {
        MockMultipartFile mockFileWithNoExtension = new MockMultipartFile(
                "file",
                "filename-without-extension",
                "text/plain",
                "content".getBytes()
        );

        String uploadPath = tempDir.toString();

        IOException exception = assertThrows(IOException.class, () -> {
            fileService.uploadImage(uploadPath, mockFileWithNoExtension);
        });

        assertEquals("Invalid file name", exception.getMessage());
    }

    @Test
    void uploadImage_shouldCreateDirectory_whenPathDoesNotExist() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        String nonExistentPath = tempDir.resolve("new_folder").toString();
        File folder = new File(nonExistentPath);
        assertFalse(folder.exists());

        fileService.uploadImage(nonExistentPath, mockFile);

        assertTrue(folder.exists());
    }
}