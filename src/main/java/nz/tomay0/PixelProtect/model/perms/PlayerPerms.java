package nz.tomay0.PixelProtect.model.perms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Decides the permissions for a specific player
 */
public class PlayerPerms {

    /**
     * Player id
     */
    private String playerUUID;

    /**
     * Specific permissions
     */
    private Map<Perm, Boolean> specificPermissions;

    /**
     * The permission level
     */
    private PermLevel permissionLevel;

    /**
     * Create permissions for a player
     *
     * @param playerUUID          player's id
     * @param permissionLevel     permission level
     * @param specificPermissions specific permissions for the player
     */
    public PlayerPerms(String playerUUID, PermLevel permissionLevel, Map<Perm, Boolean> specificPermissions) {
        this.playerUUID = playerUUID;
        this.permissionLevel = permissionLevel;
        this.specificPermissions = new HashMap<>(specificPermissions);
    }

    /**
     * Create permissions for a player without specifying the specific permissions
     *
     * @param playerUUID      player's id
     * @param permissionLevel permission level
     */
    public PlayerPerms(String playerUUID, PermLevel permissionLevel) {
        this.playerUUID = playerUUID;
        this.permissionLevel = permissionLevel;
        this.specificPermissions = new HashMap<>();
    }


    /**
     * Return specific permission of this player
     *
     * @param perm specific permission
     * @return returns if they have that permission if specifically set, otherwise null
     */
    public Boolean getSpecificPermission(Perm perm) {
        if (specificPermissions.containsKey(perm)) {
            return specificPermissions.get(perm);
        }
        return null;
    }


    /**
     * Returns the permission level of the player
     *
     * @return permission level
     */
    public PermLevel getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * Get the player id
     *
     * @return player id
     */
    public String getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Set the permission level
     *
     * @param level level
     */
    public void setPermissionLevel(PermLevel level) {
        this.permissionLevel = level;
    }

    /**
     * Set specific permission
     *
     * @param perm  specific permission
     * @param value value
     */
    public void setSpecificPermission(Perm perm, boolean value) {
        specificPermissions.put(perm, value);
    }

    /**
     * Remove all specific permissions
     */
    public void clearSpecificPermissions() {
        specificPermissions.clear();
    }

    /**
     * Iterable for specific permissions
     * @return an iterable for all specific permissions
     */
    public Iterable<Perm> getSpecificPermissions() {
        return specificPermissions.keySet();
    }
}
