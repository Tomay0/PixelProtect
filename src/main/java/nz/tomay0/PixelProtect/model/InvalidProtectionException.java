package nz.tomay0.PixelProtect.model;

/**
 * Exception thrown when a protection is in an invalid state.
 */
public class InvalidProtectionException extends RuntimeException {
    /**
     * Exception with message
     *
     * @param message message
     */
    public InvalidProtectionException(String message) {
        super(message);

    }

    /**
     * Exception with cause
     * @param cause cause
     */
    public InvalidProtectionException(Exception cause) {
        super(cause);
    }

}
