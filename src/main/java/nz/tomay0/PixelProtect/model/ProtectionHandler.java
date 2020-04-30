package nz.tomay0.PixelProtect.model;

import org.bukkit.Location;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The protection handler has a collection of all protections on the server and maintains it.
 */
public class ProtectionHandler {

    /**
     * A map from a protection's name to the protection object
     */
    private Map<String, Protection> protectionsByName = new HashMap<>();

    /**
     * A map from a player's UUID to the protections they have permissions in
     */
    private Map<String, Set<Protection>> protectionsByPlayer = new HashMap<>();

    // TODO a way to get protections by location

    /**
     * Empty protection Handler
     */
    public ProtectionHandler() {

    }

    /**
     * Initialise the collection of protections by the list of yml files in a directory.
     *
     * @param dir directory containing all protections
     */
    public ProtectionHandler(File dir) {

    }

    /**
     * Initialize the collection of protections by a collection of protections
     */
    public ProtectionHandler(Collection<Protection> protections) {
        for (Protection protection : protections) {
            protectionsByName.put(protection.getName(), protection);
        }
    }

    /**
     * Get the protection at a given location. If unprotected, this will be null.
     *
     * @param location location to check
     * @return The protection at that location
     */
    public Protection getProtectionAt(Location location) {
        for (Protection p : protectionsByName.values()) {
            if (p.withinBounds(location)) {
                return p;
            }
        }
        return null;
    }

}
