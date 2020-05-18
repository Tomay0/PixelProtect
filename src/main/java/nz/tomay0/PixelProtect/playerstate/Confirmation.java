package nz.tomay0.PixelProtect.playerstate;

import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.exception.ProtectionExceptionReason;
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
     * Cost of the confirmation. Can be negative.
     */
    private double cost;


    /**
     * Confirmation for creating/updating/removing a protection
     *
     * @param player         player
     * @param tempProtection temporary protection which only comes into affect once creating
     * @param type           confirmation type
     */
    public Confirmation(Player player, Protection tempProtection, ConfirmationType type, double cost) {
        this.player = player;
        this.tempProtection = tempProtection;
        this.type = type;
        this.cost = cost;
    }

    /**
     * Confirm this confirmation
     */
    public void confirm(ProtectionHandler protections, Economy economy) {
        CommandSender sender = player == null ? Bukkit.getConsoleSender() : player;

        // check funds
        if (player != null) {
            double balance = economy.getBalance(player);

            // check that the player has enough funds
            if (cost > balance) {
                throw new InvalidProtectionException("Not enough funds. Cost: " + cost + " Balance: " + balance, ProtectionExceptionReason.INSUFFICIENT_FUNDS);
            }
        }


        switch (type) {
            case CREATE:
                protections.addNewProtection(tempProtection);

                sender.sendMessage(ChatColor.YELLOW + "Created " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
                sender.sendMessage(ChatColor.YELLOW + "Check out " + ChatColor.AQUA + PluginConfig.getInstance().getHelpLink() + ChatColor.YELLOW + " for additional help with managing your protection.");
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

        // remove funds
        if (cost > 0) {
            economy.withdrawPlayer(player, cost);
        } else if (cost < 0) {
            economy.depositPlayer(player, -cost);
        }
    }

    /**
     * Get temp protection
     *
     * @return protection
     */
    public Protection getProtection() {
        return tempProtection;
    }
}
