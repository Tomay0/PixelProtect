package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Rename a protection
 */
public class RenameCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public RenameCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Change the name of your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            commandHelp(sender);
            return;
        }

        Protection existing = getProtections().getProtection(args[1]);
        Protection newTest = getProtections().getProtection(args[2]);

        if (existing == null) {
            sender.sendMessage(ChatColor.DARK_RED + "This protection does not exist: " + args[1]);
            commandHelp(sender);
            return;
        }

        if (!getProtections().hasPermission(sender, existing, Perm.UPDATE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update this protection: " + args[1]);
            return;
        }

        if (newTest != null) {
            sender.sendMessage(ChatColor.DARK_RED + "This protection already exists: " + args[2]);
            sender.sendMessage(ChatColor.YELLOW + "Pick a new protection name that doesn't already exist.");
            return;
        }

        getProtections().renameProtection(args[1], args[2]);

        sender.sendMessage(ChatColor.YELLOW + "Renamed your protection to " + ChatColor.GREEN + args[2] + ChatColor.YELLOW + " successfully!");
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr rename <old name> <new name>");
    }
}
