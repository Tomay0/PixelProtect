package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Remove a protection
 */
public class RemoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public RemoveCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove your protection.";
    }

    @Override
    public boolean getConsole() {
        return true;
    }
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // check name of protection is specified
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);

        if (protection == null) {
            sender.sendMessage(ChatColor.RED + "/pr remove <name>");
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.REMOVE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to remove this protection: " + protection.getName());
            return;
        }

        // check amount of money you get back
        double cost = 0;
        if (sender instanceof Player) {
            cost = -PluginConfig.getInstance().getCostPerBlock() * protection.getArea();
        }


        getPlayerStateHandler().requestRemove(sender instanceof Player ? (Player) sender : null, protection, cost);

        sender.sendMessage(ChatColor.YELLOW + "Removing " + ChatColor.GREEN + protection.getName());
        sender.sendMessage(ChatColor.YELLOW + "You will receive " + ChatColor.AQUA + "$" + String.format("%.2f", -cost));
        sender.sendMessage(ChatColor.YELLOW + "Confirm by typing " + ChatColor.AQUA + "/pr confirm");
    }
}
