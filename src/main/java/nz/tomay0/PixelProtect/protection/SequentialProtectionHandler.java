package nz.tomay0.PixelProtect.protection;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;


/**
 * This protection handler searches for protections using a for loop. This process is O(n) for finding a protection by location, and O(n^2) for initialising
 */
public class SequentialProtectionHandler extends ProtectionHandler {

    /**
     * A map from a protection's name to the protection object
     */
    private Map<String, Protection> protectionsByName = new HashMap<>();


    /**
     * Empty protection Handler
     */
    public SequentialProtectionHandler() {
        super(null);
    }

    /**
     * Initialise the collection of protections by the list of yml files in a directory.
     *
     * @param dir directory containing all protections
     */
    public SequentialProtectionHandler(File dir) {
        super(dir);


        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".yml")) continue;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

            addNewProtection(ProtectionBuilder.fromYaml(config, dir));
        }
    }
    /**
     * Initialize the collection of protections by a collection of protections
     */
    public SequentialProtectionHandler(Collection<Protection> protections) {
        super(null);

        for (Protection protection : protections) {
            addNewProtection(protection);
        }
    }


    @Override
    protected void setBounds(Protection protection, Protection newBounds) {
        protection.setBounds(newBounds.getWorld(), newBounds.getWest(), newBounds.getEast(), newBounds.getNorth(), newBounds.getSouth());
    }


    @Override
    public Protection getProtection(String name) {
        return protectionsByName.get(getIdSafeName(name));
    }

    @Override
    public Set<Protection> getOverlappingProtections(Protection protection) {
        Set<Protection> prs = new HashSet<>();

        for (Protection otherPr : protectionsByName.values()) {
            if (otherPr.getIdSafeName().equals(protection.getIdSafeName())) continue; // ignore self

            if (isOverlapping(protection, otherPr)) prs.add(otherPr);
        }

        return prs;
    }

    @Override
    public Protection getProtectionAt(Location location) {
        for (Protection p : protectionsByName.values()) {
            if (p.withinBounds(location)) {
                return p;
            }
        }
        return null;
    }

    @Override
    protected void rename(Protection protection, String newName) {
        protectionsByName.remove(protection.getIdSafeName());
        protectionsByName.put(getIdSafeName(newName), protection);
    }

    @Override
    protected void add(Protection protection) {
        protectionsByName.put(protection.getIdSafeName(), protection);
    }

    @Override
    protected void remove(Protection protection) {
        protectionsByName.remove(protection.getIdSafeName());
    }

    @Override
    public Iterator<Protection> iterator() {
        return protectionsByName.values().iterator();
    }
}
