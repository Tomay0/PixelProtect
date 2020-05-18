package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Show homes of a protection
 */
public class HomesCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public HomesCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "homes";
    }

    @Override
    public String getDescription() {
        return "Show protection homes.";
    }

    @Override
    public boolean getConsole() {
        return true;
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
                sender.sendMessage(ChatColor.RED + "/pr homes <name>");
                return;
            }

            Player player = (Player) sender;

            Collection<Protection> protections = getProtections().getAvaliableHomes(player);

            if (protections.size() > 0) {
                player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.RED + "/pr homes <name>" + ChatColor.YELLOW + " to see a list of homes from that protection you can teleport to.");
                StringBuilder sb = new StringBuilder();

                for (Protection pr : protections) {
                    sb.append(pr.getName() + ", ");
                }

                player.sendMessage(sb.substring(0, sb.length() - 2));
            } else {
                player.sendMessage(ChatColor.RED + "There are no protections you can teleport to.");
            }
        } else {
            if (protection.isAdminProtection()) {
                sender.sendMessage(ChatColor.DARK_RED + "This command is not avaliable for admin protections.");
                return;
            }
            // perms for a protection
            protection.showHomes(sender);
        }
    }
}
