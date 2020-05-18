package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        } else {
            newBounds = new Protection(protection.getName(), world, west, east, north, south,
                    protection.getOwnerID(), protection.getHome(Protection.DEFAULT_HOME));
        }

        if (protections.getOverlappingProtections(newBounds).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return newBounds;
    }


    /**
     * Create a new temporary protection which moves the north/west corner of a protection elsewhere.
     *
     * @param protection  protection to update
     * @param north       northern border
     * @param west        western border
     * @param world       world name
     * @param protections protection handler
     * @return
     */
    public static Protection move(Protection protection, int north, int west, String world, ProtectionHandler protections) {
        if (protection == null)
            throw new InvalidProtectionException("Protection to expand not specified.", PROTECTION_DOES_NOT_EXIST);


        int xlen = protection.getEast() - protection.getWest();
        int zlen = protection.getSouth() - protection.getNorth();
        int east = west + xlen;
        int south = north + zlen;

        Protection newBounds;
        if (protection.isAdminProtection()) {
            newBounds = new AdminProtection(protection.getName(), world, west, east, north, south);
        } else {
            newBounds = new Protection(protection.getName(), world, west, east, north, south, protection.getOwnerID(), protection.getHome(Protection.DEFAULT_HOME));
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
        } else {
            newBounds = new Protection(protection.getName(), world, west, east, north, south,
                    protection.getOwnerID(), protection.getHome(Protection.DEFAULT_HOME));
        }

        if (protections.getOverlappingProtections(newBounds).size() > 0) {
            throw new InvalidProtectionException("This protection will overlap other protections.", PROTECTION_OVERLAPPING);
        }

        return newBounds;
    }

    /**
     * Import from grief prevention
     *
     * @param yml         yml file to import from
     * @param newDir      claims directory for pixel protect
     * @param protections created protections
     * @return
     */
    public static Protection fromGriefPreventionYaml(YamlConfiguration yml, File newDir, ProtectionHandler protections) {
        String lesser = yml.getString("Lesser Boundary Corner");
        String greater = yml.getString("Greater Boundary Corner");

        String owner = yml.getString("Owner");

        // can build - members
        List<String> builders = yml.getStringList("Builders");

        // can access chests - chest access
        List<String> containers = yml.getStringList("Containers");

        // something ? - interact?
        List<String> accessors = yml.getStringList("Accessors");

        // managers ? - admins?
        List<String> managers = yml.getStringList("Managers");

        if (lesser == null || greater == null || owner == null || builders == null || containers == null || accessors == null || managers == null)
            throw new InvalidProtectionException("Invalid Grief Prevention yml. Missing values", YML_EXCEPTION);

        // get dimensions
        String[] lessSplit = lesser.split(";");
        String[] greaterSplit = greater.split(";");

        if (lessSplit.length < 4 || greaterSplit.length < 4)
            throw new InvalidProtectionException("Invalid Grief Prevention yml. Incorrectly formatted corners.", YML_EXCEPTION);

        try {
            String world = lessSplit[0];
            int west = Integer.parseInt(lessSplit[1]);
            int east = Integer.parseInt(greaterSplit[1]);
            int north = Integer.parseInt(lessSplit[3]);
            int south = Integer.parseInt(greaterSplit[3]);

            // admin protection
            if (owner.equals("")) {
                int i = 1;
                // get a name
                while (protections.getProtection("admin" + i) != null) {
                    i++;
                }
                String name = "admin" + i;

                YamlConfiguration newYml = YamlConfiguration.loadConfiguration(new File(newDir, name + ".yml"));

                return new AdminProtection(name, world, west, east, north, south, new HashMap<>(), new HashMap<>(), newYml, newDir);
            }
            // player protection
            else {
                // create a name based on the user's name
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(owner));
                String name = player.getName();
                if (name == null) name = "protection"; // unknown player

                if (protections.getProtection(name) != null) {
                    int i = 2;
                    while (protections.getProtection(name + i) != null) {
                        i++;
                    }
                    name = name + i;
                }

                YamlConfiguration newYml = YamlConfiguration.loadConfiguration(new File(newDir, name + ".yml"));

                // work out the home
                World worldActual = Bukkit.getWorld(world);
                int x = (west + east) / 2;
                int z = (south + north) / 2;
                int y = 255;

                Location home = new Location(worldActual, x, y, z);
                while (home.getBlock().getType() == Material.AIR) {
                    home = home.add(0, -1, 0);
                }
                home = home.add(0.5, 1, 0.5);

                Map<String, Location> homes = new HashMap<>();
                homes.put(Protection.DEFAULT_HOME, home);

                // perms
                Map<String, PlayerPerms> perms = new HashMap<>();

                for (String uuid : builders) {
                    perms.put(uuid, new PlayerPerms(uuid, PermLevel.MEMBER));
                }

                perms.put(owner, new PlayerPerms(owner, PermLevel.OWNER));

                for (String uuid : containers) {
                    if (!perms.containsKey(uuid)) {
                        PlayerPerms perm = new PlayerPerms(uuid, PermLevel.NONE);
                        perm.setSpecificPermission(Perm.CHEST, true);
                        perm.setSpecificPermission(Perm.INTERACT, true);
                        perms.put(uuid, perm);
                    }
                }

                for (String uuid : accessors) {
                    if (!perms.containsKey(uuid)) {
                        PlayerPerms perm = new PlayerPerms(uuid, PermLevel.NONE);
                        perm.setSpecificPermission(Perm.INTERACT, true);
                        perms.put(uuid, perm);
                    }
                }
                for (String uuid : managers) {
                    if (!perms.containsKey(uuid)) {
                        perms.put(uuid, new PlayerPerms(uuid, PermLevel.ADMIN));
                    } else {
                        perms.get(uuid).setPermissionLevel(PermLevel.ADMIN);
                    }
                }

                return new Protection(name, world, west, east, north, south, homes, perms, new HashMap<>(), new HashMap<>(), newYml, newDir);
            }

        } catch (NumberFormatException e) {
            throw new InvalidProtectionException(e, YML_EXCEPTION);
        }

    }
}
