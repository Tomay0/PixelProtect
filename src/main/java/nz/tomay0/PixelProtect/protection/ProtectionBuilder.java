package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.perms.Perm;
import nz.tomay0.PixelProtect.perms.PermLevel;
import nz.tomay0.PixelProtect.perms.PlayerPerms;
import org.bukkit.Location;
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
        if (!yml.contains("name") || !yml.contains("world") || !yml.contains("west") || !yml.contains("east") || !yml.contains("north") || !yml.contains("south")
                || !yml.contains("player-perms"))
            throw new InvalidProtectionException("Invalid Protection yml. Missing values.", YML_EXCEPTION);

        // name
        String name = yml.getString("name");

        // location
        String world = yml.getString("world");
        int west = yml.getInt("west");
        int east = yml.getInt("east");
        int north = yml.getInt("north");
        int south = yml.getInt("south");


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

        return new Protection(name, world, west, east, north, south, playerPermissions, defaultPermissions, yml, dir);
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

        Protection protection = new Protection(protectionName, world, west, east, north, south, uuid);

        if (protections.getOverlappingProtections(protection).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return protection;
    }

    /**
     * Create a new temporary protection with updated bounds that are an expanded version of
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

        Protection newBounds = new Protection(protection.getName(), world, west, east, north, south, protection.getOwnerID());

        if (protections.getOverlappingProtections(newBounds).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return newBounds;
    }
}
