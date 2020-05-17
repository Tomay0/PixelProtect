package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static nz.tomay0.PixelProtect.exception.ProtectionExceptionReason.*;

/**
 * Class with some methods of generating protections in different ways.
 */
public class ProtectionBuilder {
    /**
     * Build a protection from a yml file
     *
     * @param yml
     * @return
     */
    public static Protection fromYaml(YamlConfiguration yml, File dir) {
        if (!yml.contains("name") || !yml.contains("world") || !yml.contains("west") || !yml.contains("east") || !yml.contains("north") || !yml.contains("south"))
            throw new InvalidProtectionException("Invalid Protection yml. Missing values.", YML_EXCEPTION);

        // name
        String name = yml.getString("name");

        // location
        String world = yml.getString("world");
        int west = yml.getInt("west");
        int east = yml.getInt("east");
        int north = yml.getInt("north");
        int south = yml.getInt("south");

        boolean isAdmin = yml.contains("admin") && yml.getBoolean("admin");


        // level perms
        Map<Perm, PermLevel> defaultPermissions = new HashMap<>();

        ConfigurationSection levelPerms = yml.getConfigurationSection("default-perms");

        if (levelPerms != null) {
            for (String permName : levelPerms.getKeys(false)) {
                String permLevelName = levelPerms.getString(permName);

                Perm perm = Perm.fromString(permName);
                PermLevel permLevel = PermLevel.fromString(permLevelName);

                if (perm == null || permLevel == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Unknown perm/perm level.", YML_EXCEPTION);

                defaultPermissions.put(perm, permLevel);

            }
        }

        //config
        Map<Flag, Boolean> configuration = new HashMap<>();

        ConfigurationSection configSection = yml.getConfigurationSection("flags");

        if (configSection != null) {
            for (String configField : configSection.getKeys(false)) {
                boolean value = configSection.getBoolean(configField);

                Flag field = Flag.fromString(configField);

                if (field == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Unknown flag.", YML_EXCEPTION);

                configuration.put(field, value);
            }
        }

        // NON ADMIN PROTECTIONS
        if (!isAdmin) {
            if (!yml.contains("player-perms") || !yml.contains("homes"))
                throw new InvalidProtectionException("Invalid Protection yml. Missing values.", YML_EXCEPTION);

            // homes
            Map<String, Location> homes = new HashMap<>();

            ConfigurationSection homeSection = yml.getConfigurationSection("homes");

            if (homeSection == null) {
                throw new InvalidProtectionException("Invalid Protection yml. Homes not formatted correctly.", YML_EXCEPTION);
            }

            for (String home : homeSection.getKeys(false)) {
                ConfigurationSection section = homeSection.getConfigurationSection(home);

                if (section == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Homes not formatted correctly.", YML_EXCEPTION);

                if (!section.contains("world") || !section.contains("x") || !section.contains("y") || !section.contains("z"))
                    throw new InvalidProtectionException("Invalid Protection yml. Home needs world, x, y and z.", INVALID_HOME);

                World homeWorld = Bukkit.getWorld(section.getString("world"));
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");

                if (homeWorld == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Invalid world name.", INVALID_HOME);

                if (section.contains("yaw") && section.contains("pitch")) {
                    float yaw = (float) section.getDouble("yaw");
                    float pitch = (float) section.getDouble("pitch");
                    Location location = new Location(homeWorld, x, y, z, yaw, pitch);
                    homes.put(home, location);

                } else {
                    Location location = new Location(homeWorld, x, y, z);
                    homes.put(home, location);
                }
            }


            // player perms

            Map<String, PlayerPerms> playerPermissions = new HashMap<>();

            ConfigurationSection playerPerms = yml.getConfigurationSection("player-perms");

            for (String uuid : playerPerms.getKeys(false)) {
                ConfigurationSection section = playerPerms.getConfigurationSection(uuid);

                if (section == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.", YML_EXCEPTION);

                if (!section.contains("level"))
                    throw new InvalidProtectionException("Invalid Protection yml. Player perms must contain the level", YML_EXCEPTION);

                PermLevel level = PermLevel.fromString(section.getString("level"));

                if (level == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Unknown perm level.", YML_EXCEPTION);

                PlayerPerms perms = new PlayerPerms(uuid, level);

                if (section.contains("perms")) {
                    ConfigurationSection permSection = section.getConfigurationSection("perms");

                    if (permSection == null)
                        throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.", YML_EXCEPTION);

                    for (String permName : permSection.getKeys(false)) {
                        Perm perm = Perm.fromString(permName);

                        if (perm == null)
                            throw new InvalidProtectionException("Invalid Protection yml. Unknown perm " + permName + ".", YML_EXCEPTION);

                        boolean value = permSection.getBoolean(permName);

                        perms.setSpecificPermission(perm, value);
                    }

                }

                playerPermissions.put(uuid, perms);

            }
            return new Protection(name, world, west, east, north, south, homes, playerPermissions, defaultPermissions, configuration, yml, dir);
        }

        // admin protection
        return new AdminProtection(name, world, west, east, north, south, defaultPermissions, configuration, yml, dir);

    }

    /**
     * Protection creation from a command. The size is specified as blocks from the player's location.
     *
     * @param protectionName name
     * @param player         player who is creating
     * @param size           size of the protection as blocks from the centre.
     * @param protections    protection handler to test that it does not overlap
     * @return a protection
     */
    public static Protection fromCommand(String protectionName, Player player, Integer[] size, ProtectionHandler protections) {
        if (size.length != 4) throw new InvalidProtectionException("Invalid size arguments.", COMMAND_FORMAT_EXCEPTION);

        if (protections.getProtection(protectionName) != null)
            throw new InvalidProtectionException("A protection with that name already exists.", PROTECTION_ALREADY_EXISTS);

        Location l = player.getLocation();

        String world = l.getWorld().getName();
        int west = l.getBlockX() - size[0];
        int east = l.getBlockX() + size[1];
        int north = l.getBlockZ() - size[2];
        int south = l.getBlockZ() + size[3];

        String uuid = player.getUniqueId().toString();

        Protection protection = new Protection(protectionName, world, west, east, north, south, uuid, player.getLocation());

        if (protections.getOverlappingProtections(protection).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return protection;
    }

    /**
     * Admin protection creation from a command. The size is specified as blocks from the player's location.
     *
     * @param protectionName name
     * @param player         player who is creating
     * @param size           size of the protection as blocks from the centre.
     * @param protections    protection handler to test that it does not overlap
     * @return a protection
     */
    public static Protection fromAdminCommand(String protectionName, Player player, Integer[] size, ProtectionHandler protections) {
        if (size.length != 4) throw new InvalidProtectionException("Invalid size arguments.", COMMAND_FORMAT_EXCEPTION);

        if (protections.getProtection(protectionName) != null)
            throw new InvalidProtectionException("A protection with that name already exists.", PROTECTION_ALREADY_EXISTS);

        Location l = player.getLocation();

        String world = l.getWorld().getName();
        int west = l.getBlockX() - size[0];
        int east = l.getBlockX() + size[1];
        int north = l.getBlockZ() - size[2];
        int south = l.getBlockZ() + size[3];

        Protection protection = new AdminProtection(protectionName, world, west, east, north, south);

        if (protections.getOverlappingProtections(protection).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return protection;
    }

    /**
     * Create a new temporary protection with updated bounds that are an expanded version of the original
     *
     * @param protection
     * @param size
     * @param protections
     * @return new bounds
     */
    public static Protection expand(Protection protection, Integer[] size, ProtectionHandler protections) {
        if (size.length != 4) throw new InvalidProtectionException("Invalid size arguments.", COMMAND_FORMAT_EXCEPTION);

        if (protection == null)
            throw new InvalidProtectionException("Protection to expand not specified.", PROTECTION_DOES_NOT_EXIST);


        String world = protection.getWorld();
        int west = protection.getWest() - size[0];
        int east = protection.getEast() + size[1];
        int north = protection.getNorth() - size[2];
        int south = protection.getSouth() + size[3];

        Protection newBounds;
        if (protection.isAdminProtection()) {
            newBounds = new AdminProtection(protection.getName(), world, west, east, north, south);
        }else{
            newBounds = new Protection(protection.getName(), world, west, east, north, south,
                    protection.getOwnerID(), protection.getHome(Protection.DEFAULT_HOME));
        }

        if (protections.getOverlappingProtections(newBounds).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return newBounds;
    }

    /**
     * Create a new temporary protection with updated bounds that are a shifted version of the original
     *
     * @param protection
     * @param shift
     * @param protections
     * @return new bounds
     */
    public static Protection shift(Protection protection, Integer[] shift, ProtectionHandler protections) {
        if (shift.length != 4)
            throw new InvalidProtectionException("Invalid size arguments.", COMMAND_FORMAT_EXCEPTION);

        if (protection == null)
            throw new InvalidProtectionException("Protection to shift not specified.", PROTECTION_DOES_NOT_EXIST);


        String world = protection.getWorld();
        int west = protection.getWest() - shift[0] + shift[1];
        int east = protection.getEast() - shift[0] + shift[1];
        int north = protection.getNorth() - shift[2] + shift[3];
        int south = protection.getSouth() - shift[2] + shift[3];

        Protection newBounds;
        if (protection.isAdminProtection()) {
            newBounds = new AdminProtection(protection.getName(), world, west, east, north, south);
        }else{
            newBounds = new Protection(protection.getName(), world, west, east, north, south,
                    protection.getOwnerID(), protection.getHome(Protection.DEFAULT_HOME));
        }

        if (protections.getOverlappingProtections(newBounds).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return newBounds;
    }
}
