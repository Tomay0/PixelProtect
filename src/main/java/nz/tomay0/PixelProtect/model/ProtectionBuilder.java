package nz.tomay0.PixelProtect.model;

import nz.tomay0.PixelProtect.model.perms.Perm;
import nz.tomay0.PixelProtect.model.perms.PermLevel;
import nz.tomay0.PixelProtect.model.perms.PlayerPerms;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
            throw new InvalidProtectionException("Invalid Protection yml. Missing values.");

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
                throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.");

            if (!section.contains("level"))
                throw new InvalidProtectionException("Invalid Protection yml. Player perms must contain the level");

            PermLevel level = PermLevel.fromString(section.getString("level"));

            if (level == null)
                throw new InvalidProtectionException("Invalid Protection yml. Unknown perm level.");

            PlayerPerms perms = new PlayerPerms(uuid, level);

            if (section.contains("perms")) {
                ConfigurationSection permSection = section.getConfigurationSection("perms");

                if (permSection == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.");

                for (String permName : permSection.getKeys(false)) {
                    Perm perm = Perm.fromString(permName);

                    if (perm == null)
                        throw new InvalidProtectionException("Invalid Protection yml. Unknown perm " + permName + ".");

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
                    throw new InvalidProtectionException("Invalid Protection yml. Unknown perm/perm level.");

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
     * @return a protection
     */
    public static Protection fromCommand(String protectionName, Player player, Integer[] size) {
        if (size.length != 4) throw new InvalidProtectionException("Invalid size arguments.");

        Location l = player.getLocation();

        String world = l.getWorld().getName();
        int west = l.getBlockX() - size[0];
        int east = l.getBlockX() + size[1];
        int north = l.getBlockZ() - size[2];
        int south = l.getBlockZ() + size[3];

        String uuid = player.getUniqueId().toString();

        return new Protection(protectionName, world, west, east, north, south, uuid);

    }
}
