package nz.tomay0.PixelProtect.protection;

/**
 * Boolean config value
 */
public enum Flag {
    PVP(false), ENTITY_DAMAGE_ENTITY(false), FIRE_SPREAD(false), MOB_GRIEFING(false),
    BORDER_PISTON_PROTECTION(true), NETHER_PORTAL_PROTECTION(true), PRESSURE_PLATE_PROTECTION(true),
    BORDER_FLUID_PROTECTION(true), EXPLOSION_DAMAGE(true);


    private boolean defaultValue;

    Flag(boolean defaultValue) {
        this.defaultValue = defaultValue;
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
