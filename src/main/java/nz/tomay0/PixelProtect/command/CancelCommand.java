package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Cancel a protection update
 */
public class CancelCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public CancelCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "cancel";
    }

    @Override
    public String getDescription() {
        return "Cancel a creation/update to a protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = null;

        if (sender instanceof Player)
            player = (Player) sender;

        if (getPlayerStateHandler().cancel(player)) {
            sender.sendMessage(ChatColor.GREEN + "The previous action has been cancelled successfully.");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Nothing to cancel.");
        }
    }
}
