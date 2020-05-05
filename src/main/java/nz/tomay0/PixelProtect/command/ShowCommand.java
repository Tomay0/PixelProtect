package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Show a protection's borders
 */
public class ShowCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ShowCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Show the border of your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        // check name of protection is specified
        Protection protection = CommandUtil.getExistingProtection(getProtections(), player, args);

        if (protection == null) {
            sender.sendMessage(ChatColor.RED + "/pr show <name>");
            return;
        }

        getPlayerStateHandler().showBorders(player, protection);
    }
}
