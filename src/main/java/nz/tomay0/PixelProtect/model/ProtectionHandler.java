package nz.tomay0.PixelProtect.model;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

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
    // private Map<String, Set<Protection>> protectionsByPlayer = new HashMap<>();

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
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".yml")) continue;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

            addNewProtection(ProtectionBuilder.fromYaml(config));
        }
    }

    /**
     * Initialize the collection of protections by a collection of protections
     */
    public ProtectionHandler(Collection<Protection> protections) {
        for (Protection protection : protections) {
            addNewProtection(protection);
        }
    }


    /**
     * Add a new protection to the handler. This will check that the new protection added does not overlap with other protections
     *
     * @param protection
     */
    public void addNewProtection(Protection protection) {
        // check that there isn't another protection with that name
        if (getProtection(protection.getName()) != null)
            throw new InvalidProtectionException("A protection already exists with the name: " + protection.getName());

        // check that it does not overlap
        if (getOverlappingProtections(protection).size() > 0)
            throw new InvalidProtectionException("A protection cannot overlap another protection");

        // add to protection map
        protectionsByName.put(protection.getName().toLowerCase(), protection);
    }


    /**
     * Get a protection by name
     *
     * @param name name of the protection
     * @return
     */
    public Protection getProtection(String name) {
        return protectionsByName.get(name.toLowerCase());
    }

    /**
     * Test to see how many protections this new protection would overlap
     *
     * @param protection protection to add
     * @return set of all overlapping protections
     */
    private Set<Protection> getOverlappingProtections(Protection protection) {
        Set<Protection> prs = new HashSet<>();

        // TODO implement a quad tree or something to lower the complexity

        for (Protection otherPr : protectionsByName.values()) {
            if (isOverlapping(protection, otherPr)) prs.add(otherPr);
        }

        return prs;
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


    /**
     * Test if 2 protections are overlapping
     *
     * @param protection1 first protection
     * @param protection2 second protection
     * @return if they overlap
     */
    public boolean isOverlapping(Protection protection1, Protection protection2) {
        // check world
        if (!protection1.getWorld().equals(protection2.getWorld())) return false;

        // check east of pr1
        if (protection1.getEast() < protection2.getWest()) return false;

        // check west of pr1
        if (protection1.getWest() > protection2.getEast()) return false;

        // check south of pr1
        if (protection1.getSouth() < protection2.getNorth()) return false;

        // check north of pr1
        if (protection1.getNorth() > protection2.getSouth()) return false;

        return true;
    }

}
