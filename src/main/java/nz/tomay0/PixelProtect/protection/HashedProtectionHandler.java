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
 * Each world is split into a grid of 100x100 blocks which is hashed into an integer. Protections are looked up by location of that hash.
 * Close to O(1) for getting by location and close to O(N) for initialization.
 */
public class HashedProtectionHandler extends ProtectionHandler {
    private static final int CELL_SIZE = 100;

    /**
     * A map from a protection's name to the protection object
     */
    private Map<String, Protection> protectionsByName = new HashMap<>();

    /**
     * Location hash may map to multiple protections
     */
    private Map<Integer, Set<Protection>> protectionsByLocationHash = new HashMap<>();


    /**
     * Empty protection Handler
     */
    public HashedProtectionHandler() {
        super(null);
    }

    /**
     * Initialise the collection of protections by the list of yml files in a directory.
     *
     * @param dir directory containing all protections
     */
    public HashedProtectionHandler(File dir) {
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
    public HashedProtectionHandler(Collection<Protection> protections) {
        super(null);

        for (Protection protection : protections) {
            addNewProtection(protection);
        }
    }

    @Override
    public Protection getProtection(String name) {
        return protectionsByName.get(getIdSafeName(name));
    }

    @Override
    public Set<Protection> getOverlappingProtections(Protection protection) {
        Set<Protection> prs = new HashSet<>();

        for (int hash : getHashes(protection)) {
            Set<Protection> protections = protectionsByLocationHash.get(hash);
            if (protections == null) continue;
            for (Protection otherPr : protections) {
                if (otherPr.getIdSafeName().equals(protection.getIdSafeName())) continue; // ignore self

                if (isOverlapping(protection, otherPr)) prs.add(otherPr);
            }
        }

        return prs;
    }

    @Override
    public Protection getProtectionAt(Location location) {
        Set<Protection> protections = protectionsByLocationHash.get(getHash(location));

        if (protections == null) return null;
        for (Protection p : protections) {
            if (p.withinBounds(location)) {
                return p;
            }
        }
        return null;
    }

    @Override
    protected void setBounds(Protection protection, Protection newBounds) {
        Set<Integer> oldHashes = getHashes(protection);

        protection.setBounds(newBounds.getWorld(), newBounds.getWest(), newBounds.getEast(), newBounds.getNorth(), newBounds.getSouth());

        Set<Integer> newHashes = getHashes(protection);

        // add new hash values
        for (int hash : newHashes) {
            oldHashes.remove(hash);

            if (!protectionsByLocationHash.containsKey(hash)) {
                protectionsByLocationHash.put(hash, new HashSet<>());
            }
            protectionsByLocationHash.get(hash).add(protection);
        }

        // clean up old ones
        for (int hash : oldHashes) {
            if (!protectionsByLocationHash.containsKey(hash)) continue;
            protectionsByLocationHash.get(hash).remove(protection);
            if (protectionsByLocationHash.get(hash).size() == 0)
                protectionsByLocationHash.remove(hash);
        }
    }

    /**
     * Get a hash for a location
     *
     * @param location
     * @return
     */
    public int getHash(Location location) {
        int x = location.getBlockX() / CELL_SIZE;
        int z = location.getBlockZ() / CELL_SIZE;
        String world = location.getWorld().getName();

        return Objects.hash(x, z, world);
    }

    /**
     * Get all hashes in a region
     *
     * @param protection
     * @return
     */
    public Set<Integer> getHashes(Protection protection) {
        Set<Integer> hashes = new HashSet<>();
        String world = protection.getWorld();

        int x1 = protection.getWest() / CELL_SIZE;
        int z1 = protection.getNorth() / CELL_SIZE;
        int x2 = protection.getEast() / CELL_SIZE;
        int z2 = protection.getSouth() / CELL_SIZE;

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                hashes.add(Objects.hash(x, z, world));
            }
        }

        return hashes;
    }

    /**
     * Getter
     *
     * @return
     */
    public Map<Integer, Set<Protection>> getProtectionsByLocationHash() {
        return Collections.unmodifiableMap(protectionsByLocationHash);
    }


    @Override
    protected void add(Protection protection) {
        protectionsByName.put(protection.getIdSafeName(), protection);

        for (int hash : getHashes(protection)) {
            if (!protectionsByLocationHash.containsKey(hash)) {
                protectionsByLocationHash.put(hash, new HashSet<>());
            }
            protectionsByLocationHash.get(hash).add(protection);
        }
    }

    @Override
    protected void rename(Protection protection, String newName) {
        protectionsByName.remove(protection.getIdSafeName());
        protectionsByName.put(getIdSafeName(newName), protection);
    }

    @Override
    protected void remove(Protection protection) {
        protectionsByName.remove(protection.getIdSafeName());

        for (int hash : getHashes(protection)) {
            if (!protectionsByLocationHash.containsKey(hash)) continue;
            protectionsByLocationHash.get(hash).remove(protection);
            if (protectionsByLocationHash.get(hash).size() == 0)
                protectionsByLocationHash.remove(hash);
        }
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
            if (protection.getPermissionLevel(player.getUniqueId().toString()) == PermLevel.OWNER) {
                protections.add(protection);
            }
        }
        return protections;
    }
}
