package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.PlayerPerms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

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

            Collection<Protection> protections = getProtections().getAllProtections(player);

            if (protections.size() > 0) {
                // display all perms
                for (Protection pr : protections) {
                    PlayerPerms perms = pr.getPlayerPerms(player.getUniqueId().toString());
                    player.sendMessage(ChatColor.AQUA + pr.getName() + ChatColor.YELLOW + ": " + ChatColor.GREEN + perms.getPermissionLevel().toString());
                    perms.sendAdditionalPermissions(player);
                }
            } else {
                // no perms found
                player.sendMessage(ChatColor.RED + "You do not have permissions in any protections.");
            }
        } else {
            if (protection.isAdminProtection()) {
                sender.sendMessage(ChatColor.DARK_RED + "This command is not avaliable for admin protections.");
                return;
            }
            // perms for a protection
            protection.showPerms(sender);
        }
    }
}
