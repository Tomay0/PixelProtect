package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Expansion of a protection
 */
public class ExpandCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ExpandCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "expand";
    }

    @Override
    public String getDescription() {
        return "Expand/Contract the size of your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // check name of protection is specified
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);

        if (protection == null) {
            commandHelp(sender);
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.UPDATE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update this protection: " + protection.getName());
            return;
        }

        boolean isFirstArg = args.length >= 2 && getProtections().isProtection(args[1], protection);

        // get the expansion parameters
        Integer[] size = CommandUtil.getSize(args, isFirstArg ? 2 : 1, true);

        if (size == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Incorrect formatting of direction and size.");
            commandHelp(sender);
            return;
        }
        try {

            Protection newBounds = ProtectionBuilder.expand(protection, size, getProtections());
            getPlayerStateHandler().requestUpdate(sender instanceof Player ? (Player) sender : null, newBounds);

            sender.sendMessage(ChatColor.YELLOW + "Updating the borders of " + ChatColor.GREEN + newBounds.getName());
            sender.sendMessage(ChatColor.YELLOW + "Confirm by typing " + ChatColor.AQUA + "/pr confirm");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr cancel" + ChatColor.LIGHT_PURPLE + " to cancel.");

        }catch(InvalidProtectionException e) {
            CommandUtil.handleUpdateException(sender, e);
        }


    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr expand <name> <direction and size of expansion>");
        sender.sendMessage(ChatColor.YELLOW + "For example, expand 'ExampleProtection1' 5 blocks west:");
        sender.sendMessage(ChatColor.RED + "/pr expand ExampleProtection1 w5");
    }
}
