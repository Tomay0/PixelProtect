package nz.tomay0.PixelProtect.exception;

/**
 * Exception thrown when a protection is in an invalid state.
 */
public class InvalidProtectionException extends RuntimeException {
    private ProtectionExceptionReason reason;

    /**
     * Exception with message
     *
     * @param message message
     */
    public InvalidProtectionException(String message, ProtectionExceptionReason reason) {
        super(message);

        this.reason = reason;
    }

    /**
     * Exception with cause
     *
     * @param cause cause
     */
    public InvalidProtectionException(Exception cause, ProtectionExceptionReason reason) {
        super(cause);
        this.reason = reason;
    }

    /**
     * Return the reason for the exception
     * @return ProtectionExceptionReason
     */
    public ProtectionExceptionReason getReason() {
        return reason;
    }

}
