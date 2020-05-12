package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

import static nz.tomay0.PixelProtect.exception.ProtectionExceptionReason.*;

/**
 * The protection handler has a collection of all protections on the server and maintains it.
 */
public class ProtectionHandler {

    /**
     * A map from a protection's name to the protection object
     */
    private Map<String, Protection> protectionsByName = new HashMap<>();

    /**
     * Directory containing all protections
     */
    private File dir;

    /**
     * Empty protection Handler
     */
    public ProtectionHandler() {
        dir = null;
    }

    /**
     * Initialise the collection of protections by the list of yml files in a directory.
     *
     * @param dir directory containing all protections
     */
    public ProtectionHandler(File dir) {
        this.dir = dir;
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".yml")) continue;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

            addNewProtection(ProtectionBuilder.fromYaml(config, dir));
        }
    }

    /**
     * Initialize the collection of protections by a collection of protections
     */
    public ProtectionHandler(Collection<Protection> protections) {
        dir = null;

        for (Protection protection : protections) {
            addNewProtection(protection);
        }
    }


    /**
     * Add a new protection to the handler. This will check that the new protection added does not overlap with other protections
     *
     * @param protection
     */
    public void addNewProtection(Protection protection) {
        // check that there isn't another protection with that name
        Protection sameName = getProtection(protection.getName());
        if (sameName != null)
            throw new InvalidProtectionException("A protection already exists with the name: " + sameName.getName(), PROTECTION_ALREADY_EXISTS);

        // check that it does not overlap
        if (getOverlappingProtections(protection).size() > 0)
            throw new InvalidProtectionException("A protection cannot overlap another protection", PROTECTION_OVERLAPPING);

        // add to protection map
        protectionsByName.put(protection.getIdSafeName(), protection);

        protection.setDir(dir);
    }

    /**
     * Update the bounds of an existing protection
     *
     * @param newBounds a temporary protection with the updated bounds.
     */
    public void updateBounds(Protection newBounds) {
        Protection protection = getProtection(newBounds.getName());
        if (protection == null)
            throw new InvalidProtectionException("Protection does not exist.", PROTECTION_DOES_NOT_EXIST);

        // check that it does not overlap
        if (getOverlappingProtections(newBounds).size() > 0)
            throw new InvalidProtectionException("A protection cannot overlap another protection.", PROTECTION_OVERLAPPING);


        // update
        protection.setBounds(newBounds.getWorld(), newBounds.getWest(), newBounds.getEast(), newBounds.getNorth(), newBounds.getSouth());
    }

    /**
     * Rename a protection
     *
     * @param oldName old protection name
     * @param newName new name
     */
    public void renameProtection(String oldName, String newName) {
        Protection protection = getProtection(oldName);

        // no protection with that name
        if (protection == null)
            throw new InvalidProtectionException("Unknown protection: " + oldName, PROTECTION_DOES_NOT_EXIST);

        // check newName doesn't exist
        Protection duplicate = getProtection(newName);
        if (duplicate != null) {
            throw new InvalidProtectionException("Cannot rename. Name already exists: " + duplicate.getName(), PROTECTION_ALREADY_EXISTS);
        }

        if (newName.contains(" "))
            throw new InvalidProtectionException("Name cannot contain spaces.", INVALID_NAME);

        protectionsByName.remove(protection.getIdSafeName());

        protection.rename(newName);

        protectionsByName.put(getIdSafeName(newName), protection);

    }


    /**
     * Remove a protections
     *
     * @param name name of the protection
     */
    public void removeProtection(String name) {
        Protection protection = getProtection(name);

        // no protection with that name
        if (protection == null)
            throw new InvalidProtectionException("Unknown protection: " + name, PROTECTION_DOES_NOT_EXIST);

        // remove from map
        protectionsByName.remove(protection.getIdSafeName());

        // remove file
        File f = protection.getFile();
        if (f != null && f.exists()) {
            f.delete();
        }
    }

    /**
     * Get a protection by name
     *
     * @param name name of the protection
     * @return
     */
    public Protection getProtection(String name) {
        return protectionsByName.get(getIdSafeName(name));
    }

    /**
     * Test to see how many protections this new protection would overlap
     *
     * @param protection protection to add
     * @return set of all overlapping protections
     */
    public Set<Protection> getOverlappingProtections(Protection protection) {
        Set<Protection> prs = new HashSet<>();

        // TODO implement a quad tree or something to lower the complexity

        for (Protection otherPr : protectionsByName.values()) {
            if (otherPr.getIdSafeName().equals(protection.getIdSafeName())) continue; // ignore self

            if (isOverlapping(protection, otherPr)) prs.add(otherPr);
        }

        return prs;
    }

    /**
     * Get the protection at a given location. If unprotected, this will be null.
     *
     * @param location location to check
     * @return The protection at that location
     */
    public Protection getProtectionAt(Location location) {
        for (Protection p : protectionsByName.values()) {
            if (p.withinBounds(location)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Test if a player has permission to do an action at a location
     *
     * @param player   player to test
     * @param location location to test
     * @param perm     permission to test
     * @return boolean if allowed
     */
    public boolean hasPermission(Player player, Location location, Perm perm) {
        Protection protection = getProtectionAt(location);

        if (protection == null) return true; // you have permission if there is no protection

        return protection.hasPermission(player.getUniqueId().toString(), perm);
    }

    /**
     * Get the flags at a location
     *
     * @param location location to check
     * @param flag     flag to lookup
     * @return
     */
    public boolean getFlagAt(Location location, Flag flag) {
        Protection protection = getProtectionAt(location);
        // TODO admin protections mutliple protections
        if (protection == null) return flag.getNoProtection();

        return protection.getFlag(flag);
    }


    /**
     * Test if a command sender has permissions to set the permission of another player (assuming they already have the Perm.SETPERMS permission)
     *
     * @param sender     player to test
     * @param protection protection to test
     * @param uuid       uuid of the person to set the permissions of
     * @param level      level to set the permission to
     * @return
     */
    public boolean hasPermissionToSetPermissionLevel(CommandSender sender, Protection protection, String uuid, PermLevel level) {
        if (!(sender instanceof Player)) {
            // console. Can't promote or demote owner.
            if (level == PermLevel.OWNER) {
                return false;
            }
            if (protection.getPermissionLevel(uuid) == PermLevel.OWNER) {
                return false;
            }

            return true;
        }

        // player
        String senderUuid = ((Player) sender).getUniqueId().toString();

        // rules: perm level must be lower to the person that is being updated, to a level that is lower.
        // this does not apply if the player is owner
        // You cannot set the permissions of yourself either.
        PermLevel senderLevel = protection.getPermissionLevel(senderUuid);
        PermLevel setterLevel = protection.getPermissionLevel(uuid);

        return ((senderLevel.isAboveLevel(setterLevel) && senderLevel.isAboveLevel(level)) || senderLevel == PermLevel.OWNER) && !uuid.equals(senderUuid);
    }

    /**
     * Test if a command sender has permissions to set the specific permission of another player (assuming they already have the Perm.SETPERMS permission)
     *
     * @param sender     player to test
     * @param protection protection to test
     * @param uuid       uuid of the person to set the permissions of
     * @param perm       perm to update
     * @return
     */
    public boolean hasPermissionToSetSpecificPermission(CommandSender sender, Protection protection, String uuid, Perm perm) {
        if (!(sender instanceof Player)) {
            // console. Can't update permissions of the owner
            return protection.getPermissionLevel(uuid) != PermLevel.OWNER;
        }

        // player
        String senderUuid = ((Player) sender).getUniqueId().toString();

        // rules: you must already have this permission in order to update, and they must be at an equal or lower permission level.
        // You cannot set the permissions of yourself either.
        PermLevel senderLevel = protection.getPermissionLevel(senderUuid);
        PermLevel setterLevel = protection.getPermissionLevel(uuid);

        return senderLevel.isAboveLevel(setterLevel) && protection.hasPermission(senderUuid, perm) && !uuid.equals(senderUuid);
    }

    /**
     * Test if a command sender has the permission do an action for a protection
     *
     * @param sender     command sender to test
     * @param protection protection to test
     * @param perm       permission to test
     * @return boolean if allowed
     */
    public boolean hasPermission(CommandSender sender, Protection protection, Perm perm) {
        if ((sender instanceof ConsoleCommandSender)) return true;

        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        return protection.hasPermission(player.getUniqueId().toString(), perm);
    }


    /**
     * Test if 2 protections are overlapping
     *
     * @param protection1 first protection
     * @param protection2 second protection
     * @return if they overlap
     */
    public boolean isOverlapping(Protection protection1, Protection protection2) {
        // check world
        if (!protection1.getWorld().equals(protection2.getWorld())) return false;

        // check east of pr1
        if (protection1.getEast() < protection2.getWest()) return false;

        // check west of pr1
        if (protection1.getWest() > protection2.getEast()) return false;

        // check south of pr1
        if (protection1.getSouth() < protection2.getNorth()) return false;

        // check north of pr1
        if (protection1.getNorth() > protection2.getSouth()) return false;

        return true;
    }

    /**
     * Test if a name corresponds to a protection
     *
     * @param name       name
     * @param protection protection
     * @return boolean
     */
    public boolean isProtection(String name, Protection protection) {
        if (protection == null) return false;

        String idName = getIdSafeName(name);
        return idName.equals(protection.getIdSafeName());
    }


    /**
     * Get an ID safe name from an unchecked string
     *
     * @param name name, potentially unsafe
     * @return safe name to be used for ids
     */
    public static String getIdSafeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
