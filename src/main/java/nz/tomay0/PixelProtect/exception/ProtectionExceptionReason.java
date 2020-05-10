package nz.tomay0.PixelProtect.exception;

public enum ProtectionExceptionReason {
    PROTECTION_DOES_NOT_EXIST, PROTECTION_ALREADY_EXISTS, PROTECTION_OVERLAPPING, INVALID_NAME, YML_EXCEPTION, COMMAND_FORMAT_EXCEPTION,
    UNEXPECTED_EXCEPTION, INVALID_BORDERS, INVALID_OWNER, INSUFFICIENT_PERMISSIONS, DISABLED_COMMAND, NO_HOME, DEFAULT_HOME_REQUIRED,
    INVALID_HOME
}
