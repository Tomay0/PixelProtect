package nz.tomay0.PixelProtect.protection;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

import static nz.tomay0.PixelProtect.exception.ProtectionExceptionReason.*;

/**
 * The protection handler has a collection of all protections on the server and maintains it.
 */
public abstract class ProtectionHandler implements Iterable<Protection> {

    /**
     * Directory containing all protections
     */
    private File dir;

    /**
     * Initialise the collection of protections by the list of yml files in a directory.
     *
     * @param dir directory containing all protections
     */
    public ProtectionHandler(File dir) {
        this.dir = dir;
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
        add(protection);

        protection.setDir(getDir());
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

        setBounds(protection, newBounds);
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

        rename(protection, newName);

        protection.rename(newName);
    }

    /**
     * Set new protection bounds
     *
     * @param protection protection
     * @param newBounds  new bounds
     */
    protected abstract void setBounds(Protection protection, Protection newBounds);

    /**
     * Rename a protection
     *
     * @param protection
     * @param newName
     */
    protected abstract void rename(Protection protection, String newName);

    /**
     * Remove a protection
     *
     * @param protection
     */
    protected abstract void remove(Protection protection);

    /**
     * Add a protection
     *
     * @param protection
     */
    protected abstract void add(Protection protection);


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
        remove(protection);

        // remove file
        File f = protection.getFile();
        if (f != null && f.exists()) {
            f.delete();
        }
    }

    /**
     * Remove all protections
     */
    public void removeAll() {
        for (Protection protection : this) {
            removeProtection(protection.getName());
        }
    }

    /**
     * Get directory
     *
     * @return
     */
    public File getDir() {
        return dir;
    }

    /**
     * Get a protection by name
     *
     * @param name name of the protection
     * @return
     */
    public abstract Protection getProtection(String name);

    /**
     * Test to see how many protections this new protection would overlap
     *
     * @param protection protection to add
     * @return set of all overlapping protections
     */
    public abstract Set<Protection> getOverlappingProtections(Protection protection);

    /**
     * Get the protection(s) at a given location. If unprotected, this will be empty.
     *
     * @param location location to check
     * @return The protection(s) at that location
     */
    public abstract Set<Protection> getProtectionsAt(Location location);


    /**
     * Get a singular protection at a location. If there are multiple, pick the smallest.
     *
     * @param location location to check
     * @return a protection, null if none.
     */
    public Protection getMainProtectionAt(Location location) {
        Set<Protection> protections = getProtectionsAt(location);

        if (protections.size() == 0) return null;

        if (protections.size() == 1) return protections.iterator().next();

        Protection smallest = null;
        int area = 0;

        for (Protection protection : protections) {
            if (smallest == null || protection.getArea() < area) {
                smallest = protection;
                area = protection.getArea();
            }
        }

        return smallest;
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
        Set<Protection> protections = getProtectionsAt(location);
        if (protections.size() == 0) return true;
        else if (protections.size() == 1) {

            return hasPermission(player, protections.iterator().next(), perm);
        }

        // multiple protections, you need permission in all of them.
        else {
            for (Protection protection : protections) {
                if (!hasPermission(player, protection, perm)) return false;
            }

            return true;
        }
    }

    /**
     * Get the flags at a location
     *
     * @param location location to check
     * @param flag     flag to lookup
     * @return
     */
    public boolean getFlagAt(Location location, Flag flag) {
        Set<Protection> protections = getProtectionsAt(location);
        if (protections.size() == 0) return flag.getNoProtection();
        else if (protections.size() == 1) {
            return protections.iterator().next().getFlag(flag);
        }

        // multiple protections, if any don't have the no protection flag it returns that.
        else {
            for (Protection protection : protections) {
                if (protection.getFlag(flag) != flag.getNoProtection()) {
                    return !flag.getNoProtection();
                }
            }

            return flag.getNoProtection();
        }
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
     * An action that occurs outside a protection is not allowed to cross the border.
     * <p>
     * If any of the protections in the destination are protected by the flag, then the source must contain that protection.
     *
     * @param source source location
     * @param dest   dest location
     * @param flag   flag to test
     * @return if the action is protected and should be cancelled
     */
    public boolean testInsideOutsideProtected(Location source, Location dest, Flag flag) {
        Set<Protection> sourcePrs = getProtectionsAt(source);
        Set<Protection> destPrs = getProtectionsAt(dest);

        Set<Protection> allProtected = new HashSet<>();

        for (Protection protection : destPrs) {
            if (protection.getFlag(flag) != flag.getNoProtection()) {
                allProtected.add(protection);
            }
        }
        if (allProtected.size() == 0) return false;

        // source prs must contain all of allProtected
        return !sourcePrs.containsAll(allProtected);
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

        // admin protection
        if (protection.isAdminProtection() && player.hasPermission("pixelprotect.admin")) {
            return true;
        }

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
        // if both are admin protections, ignore.
        if (protection1.isAdminProtection() && protection2.isAdminProtection()) return false;

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
     * Get all protections this player can teleport to
     *
     * @param player player
     * @return
     */
    public abstract Collection<Protection> getAvaliableHomes(OfflinePlayer player);

    /**
     * Get all protections where the player has permissions
     *
     * @param player player
     * @return
     */
    public abstract Collection<Protection> getAllProtections(OfflinePlayer player);

    /**
     * Get all protections the player owns
     *
     * @param player player
     * @return
     */
    public abstract Collection<Protection> getProtectionsOwned(OfflinePlayer player);


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
