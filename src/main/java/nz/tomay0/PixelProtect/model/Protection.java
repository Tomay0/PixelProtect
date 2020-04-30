package nz.tomay0.PixelProtect.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Class representing a protection
 */
public class Protection {
    private static final int MIN_SIZE = 5;

    /**
     * Name of the protection
     */
    private String name;

    /**
     * World the protection is in
     */
    private String world;

    /**
     * Boundaries of the protection
     */
    private int west, east, north, south;


    /**
     * Validate that the state of the protection is valid. If not, an IllegalStateException will be thrown.
     */
    private void validate() {
        // check name has no spaces
        if (name.contains(" ")) {
            throw new InvalidProtectionException("Protection name can't have spaces");
        }

        // check world
        if (Bukkit.getServer() != null && Bukkit.getWorld(world) == null) {
            throw new InvalidProtectionException("The world name is invalid");
        }

        // check coordinates are valid. south > north, east > west

        if (east - west <= MIN_SIZE) {
            throw new InvalidProtectionException("Eastern boundary must be at least " + MIN_SIZE + " of the western boundary.");
        }

        if (south - north <= MIN_SIZE) {
            throw new InvalidProtectionException("Southern boundary must be at least " + MIN_SIZE + " of the northern boundary.");
        }
    }

    /**
     * Create a protection
     *
     * @param name  Name of the protection
     * @param world World the protection is contained within
     * @param west  Western boundary coordinate
     * @param east  Eastern boundary coordinate
     * @param north Northern boundary coordinate
     * @param south Southern boundary coordinate
     */
    public Protection(String name, String world, int west, int east, int north, int south) {
        this.name = name;
        this.world = world;
        this.west = west;
        this.north = north;
        this.east = east;
        this.south = south;

        validate();
    }


    /**
     * Check if a location is within this protection
     *
     * @param location location to check
     * @return boolean: true if within this protection
     */
    public boolean withinBounds(Location location) {
        if (!location.getWorld().getName().equals(world)) return false;
        if (location.getBlockX() < west) return false;
        if (location.getBlockX() > east) return false;
        if (location.getBlockZ() < north) return false;
        if (location.getBlockZ() > south) return false;

        return true;
    }

}
