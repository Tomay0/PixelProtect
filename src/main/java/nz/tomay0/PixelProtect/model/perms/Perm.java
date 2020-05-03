package nz.tomay0.PixelProtect.model.perms;

import static nz.tomay0.PixelProtect.model.perms.PermLevel.*;

/**
 * Specific permissions
 * <p>
 * TODO make this configurable and add more permissions where necessary
 */
public enum Perm {
    HOME(MEMBER), BUILD(MEMBER), INTERACT(MEMBER), CHEST(MEMBER), KILL_FRIENDLY(MEMBER), KILL_HOSTILE(MEMBER), UPDATE(ADMIN), REMOVE(OWNER), SETHOME(ADMIN), SETPERMS(ADMIN), CONFIG(ADMIN);

    private PermLevel defaultLevelRequired;

    Perm(PermLevel defaultLevelRequired) {
        this.defaultLevelRequired = defaultLevelRequired;
    }

    /**
     * Get the default level required to have this specific permission
     *
     * @return
     */
    public PermLevel getDefaultLevelRequired() {
        return defaultLevelRequired;
    }


    /**
     * Get a Perm from a string
     *
     * @param s string to check
     * @return null if nothing found
     */
    public static Perm fromString(String s) {
        try {
            return Perm.valueOf(s.toUpperCase());
        }catch(IllegalArgumentException e) {
            return null;
        }
    }
}