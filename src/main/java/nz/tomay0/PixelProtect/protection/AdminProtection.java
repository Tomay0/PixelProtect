package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static nz.tomay0.PixelProtect.exception.ProtectionExceptionReason.*;

/**
 * Class representing an admin protection
 */
public class AdminProtection extends Protection {
    /**
     * The minimal requirements for a valid admin protection.
     *
     * @param name  Name of the protection
     * @param world World the protection is contained within
     * @param west  Western boundary coordinate
     * @param east  Eastern boundary coordinate
     * @param north Northern boundary coordinate
     * @param south Southern boundary coordinate
     */
    public AdminProtection(String name, String world, int west, int east, int north, int south) {
        super(name, world, west, east, north, south);

        defaultPermissions = new HashMap<>();
        flags = new HashMap<>();

        update();
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
     * @param defaultPermissions defaultPerms
     * @param flags              flags
     * @param yml                yml config file
     * @param dir                directory containing config files
     */
    public AdminProtection(String name, String world, int west, int east, int north, int south, Map<Perm, PermLevel> defaultPermissions,
                           Map<Flag, Boolean> flags, YamlConfiguration yml, File dir) {
        super(name, world, west, east, north, south);

        super.defaultPermissions = defaultPermissions;
        super.flags = flags;

        super.yml = yml;
        super.dir = dir;

        validate();
    }

    @Override
    protected void update() {
        validate();

        // update the yml file
        if (yml != null) {
            yml.set("name", getName());
            yml.set("world", getWorld());
            yml.set("west", getWest());
            yml.set("east", getEast());
            yml.set("north", getNorth());
            yml.set("south", getSouth());
            yml.set("default-perms", null);

            yml.set("admin", true);

            // default perms
            for (Perm perm : defaultPermissions.keySet()) {
                PermLevel defaultLevel = defaultPermissions.get(perm);

                yml.set("default-perms." + perm.toString(), defaultLevel.toString());
            }

            // flags
            for (Flag flag : flags.keySet()) {
                boolean value = flags.get(flag);

                yml.set("flags." + flag.toString(), value);
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

    @Override
    protected void validate() {
        // check name has no spaces
        if (getName().contains(" ")) {
            throw new InvalidProtectionException("Protection name can't have spaces", INVALID_NAME);
        }

        // check world
        if (Bukkit.getWorld(getWorld()) == null) {
            throw new InvalidProtectionException("The world name is invalid", YML_EXCEPTION);
        }

        // check coordinates are valid. south > north, east > west

        if (getEast() - getWest() < PluginConfig.getInstance().getMinDiameter() - 1) {
            throw new InvalidProtectionException("Eastern boundary must be at least " + PluginConfig.getInstance().getMinDiameter() + " of the western boundary.", INVALID_BORDERS);
        }

        if (getSouth() - getNorth() < PluginConfig.getInstance().getMinDiameter() - 1) {
            throw new InvalidProtectionException("Southern boundary must be at least " + PluginConfig.getInstance().getMinDiameter() + " of the northern boundary.", INVALID_BORDERS);
        }

        if (PluginConfig.getInstance().getMaxArea() != -1 && getArea() > PluginConfig.getInstance().getMaxArea())
            throw new InvalidProtectionException("Protection is above the max area of " + PluginConfig.getInstance().getMaxArea(), TOO_LARGE);

        // check perm levels aren't null
        if (defaultPermissions == null) {
            throw new InvalidProtectionException(new NullPointerException(), UNEXPECTED_EXCEPTION);
        }
        if (flags == null) {
            throw new InvalidProtectionException(new NullPointerException(), UNEXPECTED_EXCEPTION);
        }
    }


    @Override
    public void setPermissionLevel(String uuid, PermLevel level) {
        throw new InvalidProtectionException("This action cannot be done to an admin protection.", ADMIN_PROTECTION);
    }

    @Override
    public void setSpecificPermission(String uuid, Perm perm, boolean value) {
        throw new InvalidProtectionException("This action cannot be done to an admin protection.", ADMIN_PROTECTION);
    }

    @Override
    public void setDefaultPermissionLevel(Perm perm, PermLevel minLevel) {
        if (minLevel != PermLevel.NONE) minLevel = PermLevel.ADMIN; // only two possible levels
        super.setDefaultPermissionLevel(perm, minLevel);
    }

    @Override
    public void setHome(String home, Location location) {
        throw new InvalidProtectionException("This action cannot be done to an admin protection.", ADMIN_PROTECTION);
    }

    @Override
    public Location getHome(String home) {
        throw new InvalidProtectionException("This action cannot be done to an admin protection.", ADMIN_PROTECTION);
    }

    @Override
    public void deleteHome(String home) {
        throw new InvalidProtectionException("This action cannot be done to an admin protection.", ADMIN_PROTECTION);
    }

    @Override
    public boolean hasPermission(String uuid, Perm perm) {
        return defaultPermissions.containsKey(perm) && defaultPermissions.get(perm) == PermLevel.NONE;
    }

    @Override
    public PermLevel getPermissionLevel(String uuid) {
        return PermLevel.NONE;
    }

    @Override
    public boolean isAdminProtection() {
        return true;
    }

    @Override
    public String getOwnerID() {
        return null;
    }

    @Override
    public String getMotd() {
        return ChatColor.YELLOW + "Entered " + ChatColor.AQUA + getName();
    }

    @Override
    public PlayerPerms getPlayerPerms(String uuid) {
        return null;
    }

    @Override
    public void showPerms(CommandSender sender) {
    }

    @Override
    public void showHomes(CommandSender sender) {
    }
}
