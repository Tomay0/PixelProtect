package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Show a protection's borders
 */
public class HideCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public HideCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "hide";
    }

    @Override
    public boolean getConsole() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Hide the borders of the protection currently being shown.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        getPlayerStateHandler().showBorders(player, null);
    }
}
