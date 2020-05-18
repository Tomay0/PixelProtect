package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Delete a protections home
 */
public class DelHomeCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public DelHomeCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "delhome";
    }

    @Override
    public boolean getConsole() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Delete a protection's home.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        Protection protection = CommandUtil.getExistingProtection(getProtections(), player, args);
        if (protection == null) {
            commandHelp(player);
            return;
        }

        if (protection.isAdminProtection()) {
            sender.sendMessage(ChatColor.DARK_RED + "This command is not avaliable for admin protections.");
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.SETHOME)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to delete this protection's home: " + protection.getName());
            return;
        }
        int homeArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        String home = Protection.DEFAULT_HOME;

        if (args.length > homeArg) {
            home = args[homeArg];
        }

        if (protection.getHome(home) == null) {
            sender.sendMessage(ChatColor.RED + home + " does not exist as a home of this protection.");
            return;
        }

        try {
            protection.deleteHome(home);

            sender.sendMessage(ChatColor.YELLOW + "Deleted the home: " + ChatColor.GREEN + home + ChatColor.YELLOW + " of " + ChatColor.GREEN + protection.getName());
        } catch (InvalidProtectionException e) {
            commandHelp(sender);
        }

    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr delhome <name> <home name>");
        sender.sendMessage(ChatColor.YELLOW + "Note that you cannot delete the default home.");
    }
}
