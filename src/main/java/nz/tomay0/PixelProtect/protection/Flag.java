package nz.tomay0.PixelProtect.protection;

/**
 * Boolean config value
 */
public enum Flag {
    PVP(false, true),
    ENTITY_DAMAGE_ENTITY(false, true),
    FIRE_SPREAD(false, true),
    MOB_GRIEFING(false, true),
    EXPLOSION_DAMAGE(true, true),
    BORDER_PISTON_PROTECTION(true, false),
    NETHER_PORTAL_PROTECTION(true, false),
    PRESSURE_PLATE_PROTECTION(true, false),
    BORDER_FLUID_PROTECTION(true, false),
    BORDER_TREE_PROTECTION(true, false);


    private boolean defaultValue, noProtections;

    Flag(boolean defaultValue, boolean noProtections) {
        this.defaultValue = defaultValue;
        this.noProtections = noProtections;
    }

    /**
     * Get default value for config field
     *
     * @return
     */
    public boolean getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get default value for when there's no protections at that location
     *
     * @return
     */
    public boolean getNoProtection() {
        return noProtections;
    }

    /**
     * Get a Flag from a string
     *
     * @param s string to check
     * @return null if nothing found
     */
    public static Flag fromString(String s) {
        try {
            return Flag.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
