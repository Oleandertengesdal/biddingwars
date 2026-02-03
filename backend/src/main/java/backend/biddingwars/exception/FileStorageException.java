package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when file storage operations fail.
 * Examples: invalid file type, file size exceeded, storage errors.
 * Returns HTTP 400 Bad Request or 413 Payload Too Large.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class FileStorageException extends AppException {

    public FileStorageException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "FILE_STORAGE_ERROR");
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "FILE_STORAGE_ERROR");
    }

    /**
     * Creates an exception for file size exceeded.
     *
     * @param message the error message
     * @return FileStorageException with 413 status
     */
    public static FileStorageException fileSizeExceeded(String message) {
        return new FileStorageException(message) {
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.PAYLOAD_TOO_LARGE;
            }
        };
    }
}
