package nz.tomay0.PixelProtect.confirm;

import nz.tomay0.PixelProtect.model.Protection;
import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * A confirmation to creating or updating the bounds of a protection
 */
public class Confirmation {
    /**
     * Player updating/creating
     */
    private Player player;

    /**
     * Temporary bounds of the protection
     */
    private Protection tempProtection;

    /**
     * If this protection already exists.
     */
    private boolean existing;

    /**
     * Create protection
     *
     * @param player player
     * @param tempProtection temporary protection which only comes into affect once creating
     * @param exists exists
     */
    public Confirmation(Player player, Protection tempProtection, boolean exists) {
        this.player = player;
        this.tempProtection = tempProtection;
        this.existing = exists;

    }

    /**
     * Confirm this confirmation
     */
    public void confirm(ProtectionHandler protections) {
        if (!existing) {
            // create new protection
            protections.addNewProtection(tempProtection);

            player.sendMessage(ChatColor.YELLOW + "Created " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr setperm " + tempProtection.getIdSafeName() + " <name> member" + ChatColor.LIGHT_PURPLE + " to add new members to your protection.");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You can see more commands you can use to manage your protection by typing " + ChatColor.RED + "/pr help");
        }
        else {
            // update bounds
            protections.updateBounds(tempProtection);
        }
    }
}
