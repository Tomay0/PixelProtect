package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Show permissions of a protection
 */
public class PermsCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public PermsCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "perms";
    }

    @Override
    public String getDescription() {
        return "Show protection permissions.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // check name of protection is specified

        Protection protection = null;
        if (args.length > 1) {
            protection = getProtections().getProtection(args[1]);
            if (protection == null) {
                sender.sendMessage(ChatColor.RED + "Unknown protection: " + args[1]);
                return;
            }
        }

        if (protection == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "/pr perms <name>");
                return;
            }

            Player player = (Player) sender;

            getProtections().showPerms(player);
        } else {
            // perms for a protection
            protection.showPerms(sender);
        }
    }
}
