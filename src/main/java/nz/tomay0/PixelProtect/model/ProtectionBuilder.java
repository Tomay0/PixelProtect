package nz.tomay0.PixelProtect.model;

import nz.tomay0.PixelProtect.model.perms.Perm;
import nz.tomay0.PixelProtect.model.perms.PermLevel;
import nz.tomay0.PixelProtect.model.perms.PlayerPerms;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

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
    public static Protection fromYaml(YamlConfiguration yml) {
        if (!yml.contains("name") || !yml.contains("world") || !yml.contains("west") || !yml.contains("east") || !yml.contains("north") || !yml.contains("south")
                || !yml.contains("player-perms"))
            throw new InvalidProtectionException("Invalid Protection yml. Missing values.");

        String name = yml.getString("name");
        String world = yml.getString("world");
        int west = yml.getInt("west");
        int east = yml.getInt("east");
        int north = yml.getInt("north");
        int south = yml.getInt("south");

        Map<String, PlayerPerms> playerPermissions = new HashMap<>();

        ConfigurationSection playerPerms = yml.getConfigurationSection("player-perms");

        for (String uuid : playerPerms.getKeys(false)) {
            // TODO check valid uuid?

            ConfigurationSection section = playerPerms.getConfigurationSection(uuid);

            if (section == null)
                throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.");

            if (!section.contains("level"))
                throw new InvalidProtectionException("Invalid Protection yml. Player perms must contain the level");

            PermLevel level = PermLevel.fromString(section.getString("level"));

            PlayerPerms perms = new PlayerPerms(uuid, level);

            if (section.contains("perms")) {
                ConfigurationSection permSection = section.getConfigurationSection("perms");

                if (section == null)
                    throw new InvalidProtectionException("Invalid Protection yml. Player perms not formatted correctly.");

                for (String permName : permSection.getKeys(false)) {
                    Perm perm = Perm.fromString(permName);

                    if(perm == null) throw new InvalidProtectionException("Invalid Protection yml. Unknown perm " + permName + ".");

                    boolean value = permSection.getBoolean(permName);

                    perms.setSpecificPermission(perm, value);
                }

            }

            playerPermissions.put(uuid, perms);

        }

        return new Protection(name, world, west, east, north, south, playerPermissions, new HashMap<Perm, PermLevel>());
    }
}
