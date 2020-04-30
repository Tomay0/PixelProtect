package nz.tomay0.PixelProtect.model.perms;

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
}