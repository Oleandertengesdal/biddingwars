package backend.biddingwars.exception;

/**
 * Exception thrown when image upload fails validation.
 * This is a specialized version of FileStorageException.
 *
 * @deprecated Use {@link FileStorageException} instead
 * @author Oleander Tengesdal
 * @version 2.0
 * @since 03-02-2026
 */
@Deprecated(since = "2.0", forRemoval = true)
public class IllegalImageUploadException extends FileStorageException {
    
    public IllegalImageUploadException(String message) {
        super(message);
    }
}