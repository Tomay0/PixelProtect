package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Confirm a protection update
 */
public class ConfirmCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ConfirmCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "confirm";
    }

    @Override
    public String getDescription() {
        return "Confirm a creation/update to a protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = null;

        if (sender instanceof Player)
            player = (Player) sender;

        try {
            if (!getPlayerStateHandler().confirm(player, getEconomy())) {
                sender.sendMessage(ChatColor.DARK_RED + "Nothing to confirm.");
            }

        } catch (InvalidProtectionException e) {
            sender.sendMessage(ChatColor.DARK_RED + "This protection can no longer be created...");
            sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
        }
    }
}
