package nz.tomay0.PixelProtect.model;

import nz.tomay0.PixelProtect.model.perms.Perm;
import nz.tomay0.PixelProtect.model.perms.PermLevel;
import nz.tomay0.PixelProtect.model.perms.PlayerPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

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
     * Owner
     */
    private String ownerUuid;

    /**
     * Player permissions
     */
    private Map<String, PlayerPerms> playerPermissions;

    /**
     * Default minimum perm level permissions
     */
    private Map<Perm, PermLevel> defaultPermissions;

    /**
     * Create a protection
     *
     * @param name      Name of the protection
     * @param world     World the protection is contained within
     * @param west      Western boundary coordinate
     * @param east      Eastern boundary coordinate
     * @param north     Northern boundary coordinate
     * @param south     Southern boundary coordinate
     * @param ownerUuid owner id
     */
    public Protection(String name, String world, int west, int east, int north, int south, String ownerUuid) {
        this.name = name;
        this.world = world;
        this.west = west;
        this.north = north;
        this.east = east;
        this.south = south;

        playerPermissions = new HashMap<>();
        defaultPermissions = new HashMap<>();

        setPermissionLevel(ownerUuid, PermLevel.OWNER);

        validate();
    }

    /**
     * Update the protection by updating the config file and validating the data
     */
    private void update() {
        // TODO update config

        validate();
    }

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

        // check perm levels aren't null
        if (playerPermissions == null) {
            throw new InvalidProtectionException("Null player permissions");
        }

        // check perm levels aren't null
        if (defaultPermissions == null) {
            throw new InvalidProtectionException("Null default permissions");
        }

        // check there is only 1 owner
        int numOwners = 0;
        for (PlayerPerms perms : playerPermissions.values()) {
            if (perms.getPermissionLevel() == PermLevel.OWNER) {
                if (!ownerUuid.equals(perms.getPlayerUUID())) {
                    throw new InvalidProtectionException("Only the owner of the protection must have owner permission level");
                }
                numOwners++;
            }
        }
        if (numOwners != 1) {
            throw new InvalidProtectionException("There must only be one owner");
        }
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

    /**
     * Set the permission level of a player
     *
     * @param uuid
     * @param level
     */
    public void setPermissionLevel(String uuid, PermLevel level) {
        if (playerPermissions.containsKey(uuid)) {
            playerPermissions.get(uuid).setPermissionLevel(level);
        } else {
            PlayerPerms perms = new PlayerPerms(uuid, level);
            playerPermissions.put(uuid, perms);
        }

        // if setting this to owner, demote the other owner to admin
        if (level == PermLevel.OWNER) {
            String oldOwner = ownerUuid;
            ownerUuid = uuid;

            if (oldOwner != null)
                setPermissionLevel(oldOwner, PermLevel.ADMIN);

            // clear all specific permissions
            playerPermissions.get(uuid).clearSpecificPermissions();
        }
        update();
    }

    /**
     * Set the specific permissions of a player
     *
     * @param uuid  player id
     * @param perm  specific permission
     * @param value value
     */
    public void setSpecificPermission(String uuid, Perm perm, boolean value) {
        // can't set owner permission
        if (getPermissionLevel(uuid) == PermLevel.OWNER)
            throw new InvalidProtectionException("Cannot set the permission levels of the owner");

        if (!playerPermissions.containsKey(uuid)) {
            setPermissionLevel(uuid, PermLevel.NONE);
        }

        playerPermissions.get(uuid).setSpecificPermission(perm, value);

        update();
    }

    /**
     * Set the default minimum permission level needed to obtain a specific permission
     *
     * @param perm     permission
     * @param minLevel permission level
     */
    public void setDefaultPermissionLevel(Perm perm, PermLevel minLevel) {
        defaultPermissions.put(perm, minLevel);
    }

    /**
     * Returns if this player have this permission in this protection
     *
     * @param uuid player id
     * @param perm permission to check
     * @return
     */
    public boolean hasPermission(String uuid, Perm perm) {
        PermLevel level = PermLevel.NONE;

        // check if player has specific perms
        if (playerPermissions.containsKey(uuid)) {
            PlayerPerms perms = playerPermissions.get(uuid);

            Boolean permission = perms.getSpecificPermission(perm);

            // note to ignore specific permissions for owner
            if (permission == null || perms.getPermissionLevel() == PermLevel.OWNER) {
                // no specific permission set, consider their level
                level = perms.getPermissionLevel();
            } else {
                // specific permission is set
                return permission;
            }
        }

        // check if the level the player is on has perms
        if (defaultPermissions.containsKey(perm)) {
            return level.hasPermissionsOfLevel(defaultPermissions.get(perm));
        }

        // use default level permissions
        return level.hasPermissionsOfLevel(perm.getDefaultLevelRequired());
    }

    /**
     * Get the permission level of a player
     *
     * @param uuid player uuid
     * @return the permission level
     */
    public PermLevel getPermissionLevel(String uuid) {
        if (playerPermissions.containsKey(uuid)) {
            return playerPermissions.get(uuid).getPermissionLevel();
        }
        return PermLevel.NONE;
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
