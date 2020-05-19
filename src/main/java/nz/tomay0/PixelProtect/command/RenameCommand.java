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
    public boolean getConsole() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            commandHelp(sender);
            return;
        }

        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args, 3);
        if (protection == null) {
            commandHelp(sender);
            return;
        }
        int newArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (newArg >= args.length) {
            commandHelp(sender);
            return;
        }

        Protection newTest = getProtections().getProtection(args[newArg]);

        if (protection == null) {
            sender.sendMessage(ChatColor.DARK_RED + "This protection does not exist: " + protection.getName());
            commandHelp(sender);
            return;
        }

        if (!getProtections().hasPermission(sender, protection, Perm.UPDATE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update this protection: " + protection.getName());
            return;
        }

        if (newTest != null) {
            sender.sendMessage(ChatColor.DARK_RED + "This protection already exists: " + args[newArg]);
            sender.sendMessage(ChatColor.YELLOW + "Pick a new protection name that doesn't already exist.");
            return;
        }

        String oldName = protection.getName();
        getProtections().renameProtection(oldName, args[newArg]);

        sender.sendMessage(ChatColor.YELLOW + "Renamed "+ ChatColor.GREEN + oldName + " to " + ChatColor.GREEN + args[newArg] + ChatColor.YELLOW + " successfully!");
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr rename <old name> <new name>");
    }
}
