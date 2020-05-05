package nz.tomay0.PixelProtect.playerstate;

import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A confirmation to creating or updating the bounds of a protection
 */
public class Confirmation {

    public enum ConfirmationType {
        CREATE, UPDATE, REMOVE
    }

    /**
     * Player updating/creating
     */
    private Player player;

    /**
     * Temporary bounds of the protection
     */
    private Protection tempProtection;

    /**
     * Type of action
     */
    private ConfirmationType type;

    /**
     * Confirmation for creating/updating/removing a protection
     *
     * @param player         player
     * @param tempProtection temporary protection which only comes into affect once creating
     * @param type           confirmation type
     */
    public Confirmation(Player player, Protection tempProtection, ConfirmationType type) {
        this.player = player;
        this.tempProtection = tempProtection;
        this.type = type;
    }

    /**
     * Confirm this confirmation
     */
    public void confirm(ProtectionHandler protections) {
        CommandSender sender = player == null ? Bukkit.getConsoleSender() : player;

        switch (type) {
            case CREATE:
                protections.addNewProtection(tempProtection);

                sender.sendMessage(ChatColor.YELLOW + "Created " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr setperm " + tempProtection.getIdSafeName() + " <name> member" + ChatColor.LIGHT_PURPLE + " to add new members to your protection.");
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "You can see more commands you can use to manage your protection by typing " + ChatColor.RED + "/pr help");
                break;
            case UPDATE:
                protections.updateBounds(tempProtection);
                sender.sendMessage(ChatColor.YELLOW + "Updated " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
                break;
            case REMOVE:
                protections.removeProtection(tempProtection.getName());
                sender.sendMessage(ChatColor.YELLOW + "Removed " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
                break;
        }
    }

    /**
     * Get temp protection
     * @return protection
     */
    public Protection getProtection() {
        return tempProtection;
    }
}
