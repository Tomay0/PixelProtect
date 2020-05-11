package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.perms.Perm;
import nz.tomay0.PixelProtect.perms.PermLevel;
import nz.tomay0.PixelProtect.perms.PlayerPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static nz.tomay0.PixelProtect.exception.ProtectionExceptionReason.*;

/**
 * Class representing a protection
 */
public class Protection {
    public static final int MIN_SIZE = 5;
    public static final String DEFAULT_HOME = "home";

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
     * Homes
     */
    private Map<String, Location> homes;

    /**
     * Yml configuration to save to
     */
    private YamlConfiguration yml = null;

    /**
     * Directory to save yml configuration to
     */
    private File dir = null;

    /**
     * Temporary protection with only bounds and no permissions
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
    }

    /**
     * The minimal requirements for a valid protection. All parameters set when using /pr create
     *
     * @param name      Name of the protection
     * @param world     World the protection is contained within
     * @param west      Western boundary coordinate
     * @param east      Eastern boundary coordinate
     * @param north     Northern boundary coordinate
     * @param south     Southern boundary coordinate
     * @param ownerUuid owner id
     * @param home      location of the home
     */
    public Protection(String name, String world, int west, int east, int north, int south, String ownerUuid, Location home) {
        this.name = name;
        this.world = world;
        this.west = west;
        this.north = north;
        this.east = east;
        this.south = south;


        playerPermissions = new HashMap<>();
        defaultPermissions = new HashMap<>();
        homes = new HashMap<>();

        homes.put(Protection.DEFAULT_HOME, home);
        setPermissionLevel(ownerUuid, PermLevel.OWNER);
    }

    /**
     * Create a protection from all data
     *
     * @param name               Name of the protection
     * @param world              World the protection is contained within
     * @param west               Western boundary coordinate
     * @param east               Eastern boundary coordinate
     * @param north              Northern boundary coordinate
     * @param south              Southern boundary coordinate
     * @param homes              all homes
     * @param playerPermissions  playerPerms
     * @param defaultPermissions defaultPerms
     * @param yml                yml config file
     * @param dir                directory containing config files
     */
    public Protection(String name, String world, int west, int east, int north, int south, Map<String, Location> homes,
                      Map<String, PlayerPerms> playerPermissions, Map<Perm, PermLevel> defaultPermissions, YamlConfiguration yml, File dir) {
        this.name = name;
        this.world = world;
        this.west = west;
        this.north = north;
        this.east = east;
        this.south = south;

        this.playerPermissions = playerPermissions;
        this.defaultPermissions = defaultPermissions;
        this.homes = homes;

        this.yml = yml;
        this.dir = dir;

        // get owner
        for (PlayerPerms perms : playerPermissions.values()) {
            if (perms.getPermissionLevel() == PermLevel.OWNER) {
                ownerUuid = perms.getPlayerUUID();
            }
        }

        validate();
    }

    /**
     * Update the protection by updating the config file and validating the data
     */
    private void update() {
        validate();

        // update the yml file
        if (yml != null) {
            yml.set("name", name);
            yml.set("world", world);
            yml.set("west", west);
            yml.set("east", east);
            yml.set("north", north);
            yml.set("south", south);
            yml.set("player-perms", null);
            yml.set("default-perms", null);

            // homes
            for (String home : homes.keySet()) {
                Location location = homes.get(home);

                yml.set("homes." + home + ".world", location.getWorld().getName());
                yml.set("homes." + home + ".x", location.getX());
                yml.set("homes." + home + ".y", location.getY());
                yml.set("homes." + home + ".z", location.getZ());
                yml.set("homes." + home + ".yaw", location.getYaw());
                yml.set("homes." + home + ".pitch", location.getPitch());
            }

            // player perms
            for (String uuid : playerPermissions.keySet()) {
                PlayerPerms perms = playerPermissions.get(uuid);

                yml.set("player-perms." + uuid + ".level", perms.getPermissionLevel().toString());

                for (Perm perm : perms.getSpecificPermissions()) {
                    yml.set("player-perms." + uuid + ".perms." + perm.toString(), perms.getSpecificPermission(perm));
                }
            }

            // default perms
            for (Perm perm : defaultPermissions.keySet()) {
                PermLevel defaultLevel = defaultPermissions.get(perm);

                yml.set("default-perms." + perm.toString(), defaultLevel.toString());
            }

            try {
                File file = getFile();

                if (file != null)
                    yml.save(file);
            } catch (IOException e) {
                throw new InvalidProtectionException(e, UNEXPECTED_EXCEPTION);
            }
        }
    }

    /**
     * Validate that the state of the protection is valid. If not, an IllegalStateException will be thrown.
     */
    private void validate() {
        // check name has no spaces
        if (name.contains(" ")) {
            throw new InvalidProtectionException("Protection name can't have spaces", INVALID_NAME);
        }

        // check world
        if (Bukkit.getWorld(world) == null) {
            throw new InvalidProtectionException("The world name is invalid", YML_EXCEPTION);
        }

        // check coordinates are valid. south > north, east > west

        if (east - west < MIN_SIZE - 1) {
            throw new InvalidProtectionException("Eastern boundary must be at least " + MIN_SIZE + " of the western boundary.", INVALID_BORDERS);
        }

        if (south - north < MIN_SIZE - 1) {
            throw new InvalidProtectionException("Southern boundary must be at least " + MIN_SIZE + " of the northern boundary.", INVALID_BORDERS);
        }

        // check perm levels aren't null
        if (playerPermissions == null) {
            throw new InvalidProtectionException(new NullPointerException(), UNEXPECTED_EXCEPTION);
        }

        // check perm levels aren't null
        if (defaultPermissions == null) {
            throw new InvalidProtectionException(new NullPointerException(), UNEXPECTED_EXCEPTION);
        }

        // check owner isn't null
        if (ownerUuid == null) {
            throw new InvalidProtectionException("There must be an owner", INVALID_OWNER);
        }

        // check there is only 1 owner
        int numOwners = 0;
        for (PlayerPerms perms : playerPermissions.values()) {
            if (perms.getPermissionLevel() == PermLevel.OWNER) {
                if (!ownerUuid.equals(perms.getPlayerUUID())) {
                    throw new InvalidProtectionException("Only the owner of the protection must have owner permission level", INVALID_OWNER);
                }
                numOwners++;
            }
        }
        if (numOwners != 1) {
            throw new InvalidProtectionException("There must only be one owner", INVALID_OWNER);
        }

        // check there is a home
        if (homes == null) {
            throw new InvalidProtectionException("There must be at least one home", NO_HOME);
        }
        if (!homes.containsKey("home") || homes.get("home") == null) {
            throw new InvalidProtectionException("There must be at least one home", DEFAULT_HOME_REQUIRED);
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
            throw new InvalidProtectionException("Cannot set the permission levels of the owner", INSUFFICIENT_PERMISSIONS);

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
        update();
    }

    /**
     * Update the bounds of the protection
     *
     * @param world
     * @param west
     * @param east
     * @param north
     * @param south
     */
    public void setBounds(String world, int west, int east, int north, int south) {
        String oldWorld = this.world;
        int oldWest = this.west;
        int oldEast = this.east;
        int oldNorth = this.north;
        int oldSouth = this.south;

        this.world = world;
        this.west = west;
        this.east = east;
        this.north = north;
        this.south = south;

        try {
            update();
        } catch (InvalidProtectionException e) {
            // go back to old bounds
            this.world = oldWorld;
            this.west = oldWest;
            this.east = oldEast;
            this.north = oldNorth;
            this.south = oldSouth;
        }
    }

    /**
     * Set the location of a home for the protection
     *
     * @param home     home name
     * @param location location
     */
    public void setHome(String home, Location location) {
        homes.put(ProtectionHandler.getIdSafeName(home), location);

        update();
    }

    /**
     * Get location of a home
     *
     * @param home home name
     * @return location, null if not exist
     */
    public Location getHome(String home) {
        String idSafeHome = ProtectionHandler.getIdSafeName(home);

        if (homes.containsKey(idSafeHome)) return homes.get(idSafeHome);
        return null;
    }

    /**
     * Delete a home (cannot delete the default)
     *
     * @param home home
     */
    public void deleteHome(String home) {
        String idSafeHome = ProtectionHandler.getIdSafeName(home);

        if (idSafeHome.equals(DEFAULT_HOME))
            throw new InvalidProtectionException("Cannot delete the default home.", DEFAULT_HOME_REQUIRED);

        if (!homes.containsKey(idSafeHome)) return;

        homes.remove(idSafeHome);
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
     * Set the directory of the protection so it can save
     *
     * @param dir dir
     */
    public void setDir(File dir) {
        this.dir = dir;
        updateYmlFile();
    }

    /**
     * Rename the protection. This updates the yml file
     *
     * @param newName
     */
    public void rename(String newName) {
        File file = getFile();
        if (file != null && file.exists())
            file.delete();

        this.name = newName;

        updateYmlFile();
    }

    /**
     * Update the yml file if the directory or name changes
     */
    private void updateYmlFile() {
        File file = getFile();
        if (file != null)
            yml = YamlConfiguration.loadConfiguration(file);
        else
            yml = null;

        update();
    }

    /**
     * Get the file to write to
     *
     * @return a file
     */
    public File getFile() {
        if (dir != null) {
            return new File(dir, getIdSafeName() + ".yml");
        }
        return null;
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name that can be used for filenames and protection hashmap
     *
     * @return
     */
    public String getIdSafeName() {
        return ProtectionHandler.getIdSafeName(name);
    }

    /**
     * Get the world name of the protection
     *
     * @return string
     */
    public String getWorld() {
        return world;
    }

    /**
     * Get the western border of the protection
     *
     * @return int
     */
    public int getWest() {
        return west;
    }

    /**
     * Get the eastern border of the protection
     *
     * @return int
     */
    public int getEast() {
        return east;
    }

    /**
     * Get the northern border of the protection
     *
     * @return int
     */
    public int getNorth() {
        return north;
    }

    /**
     * Get the southern border of the protection
     *
     * @return int
     */
    public int getSouth() {
        return south;
    }

    /**
     * Get the ID of the owner
     *
     * @return owner id
     */
    public String getOwnerID() {
        return ownerUuid;
    }

    /**
     * Return a message for when you enter the borders of the protection
     *
     * @return string
     */
    public String getMotd() {
        return ChatColor.YELLOW + "Entered " + ChatColor.GREEN + name;
    }
}
