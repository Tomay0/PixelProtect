package nz.tomay0.PixelProtect.perms;

/**
 * Permission level enum
 */
public enum PermLevel {
    NONE(0), MEMBER(1), ADMIN(2), OWNER(3);

    int level;

    PermLevel(int level) {
        this.level = level;
    }

    /**
     * If this level is higher or equal to the other level
     *
     * @return boolean
     */
    public boolean hasPermissionsOfLevel(PermLevel otherLevel) {
        return level >= otherLevel.level;
    }

    /**
     * If this level is higher than the other level
     *
     * @return boolean
     */
    public boolean isAboveLevel(PermLevel otherLevel) {
        return level > otherLevel.level;
    }


    /**
     * Get a PermLevel from a string
     *
     * @param s string to check
     * @return null if nothing found
     */
    public static PermLevel fromString(String s) {
        try {
            return PermLevel.valueOf(s.toUpperCase());
        }catch(IllegalArgumentException e) {
            return null;
        }
    }
}