package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
    public Set<Protection> getProtectionsAt(Location location) {
        Set<Protection> protections = new HashSet<>();
        for (Protection p : protectionsByName.values()) {
            if (p.withinBounds(location)) {
                protections.add(p);
            }
        }
        return protections;
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

    @Override
    public Collection<Protection> getAvaliableHomes(OfflinePlayer player) {
        Collection<Protection> protections = new ArrayList<>();

        for (Protection protection : this) {
            if (protection.hasPermission(player.getUniqueId().toString(), Perm.HOME)) {
                protections.add(protection);
            }
        }
        return protections;
    }

    @Override
    public Collection<Protection> getAllProtections(OfflinePlayer player) {
        Collection<Protection> protections = new ArrayList<>();

        for (Protection protection : this) {
            PlayerPerms perms = protection.getPlayerPerms(player.getUniqueId().toString());
            if (perms != null) {
                protections.add(protection);
            }
        }
        return protections;
    }

    @Override
    public Collection<Protection> getProtectionsOwned(OfflinePlayer player) {
        Collection<Protection> protections = new ArrayList<>();

        for (Protection protection : this) {
            if (protection.getPermissionLevel(player.getUniqueId().toString()) == PermLevel.OWNER && !protection.isAdminProtection()) {
                protections.add(protection);
            }
        }
        return protections;
    }
}
