package backend.biddingwars.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file upload and storage.
 * Stores files locally in configured upload directory.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("Upload directory initialized: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    /**
     * Store a file and return its stored filename.
     *
     * @param file the file to store
     * @return the stored filename (UUID-based)
     * @throws IllegalArgumentException if file is invalid
     * @throws RuntimeException if file cannot be stored
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            // Security check: ensure filename doesn't contain path separators
            if (storedFilename.contains("..")) {
                throw new IllegalArgumentException("Invalid file path sequence: " + storedFilename);
            }

            Path targetLocation = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {} -> {}", originalFilename, storedFilename);
            return storedFilename;

        } catch (IOException e) {
            logger.error("Failed to store file: {}", originalFilename, e);
            throw new RuntimeException("Could not store file " + originalFilename, e);
        }
    }

    /**
     * Load a file as a resource.
     *
     * @param filename the filename to load
     * @return the file as a Resource
     * @throws RuntimeException if file not found
     */
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

    /**
     * Delete a file.
     *
     * @param filename the filename to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filename, e);
            return false;
        }
    }

    /**
     * Validate the uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename is required");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS);
        }
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Get the content type for a file.
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
