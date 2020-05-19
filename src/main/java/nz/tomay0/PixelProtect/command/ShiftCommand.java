package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Shift a protection
 */
public class ShiftCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ShiftCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "shift";
    }

    @Override
    public String getDescription() {
        return "Move your protection a set number of blocks in a chosen direction.";
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
        Protection protection = getProtections().getProtection(args[1]);
        boolean isFirstArg = protection != null;

        Integer[] shift;

        if (!isFirstArg) {
            protection = CommandUtil.getExistingProtection(getProtections(), sender, args, Integer.MAX_VALUE);
            shift = CommandUtil.getSize(args, 1, true);

            if (shift == null) {
                commandHelp(sender);
                return;
            }
        } else {
            shift = CommandUtil.getSize(args, 2, true);
        }

        if (protection == null) {
            commandHelp(sender);
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.UPDATE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update this protection: " + protection.getName());
            return;
        }

        // prevent typing both south and north and east and west
        if (shift == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Incorrect formatting of direction and size.");
            commandHelp(sender);
            return;
        } else if ((shift[0] != 0 && shift[1] != 0) && (shift[2] != 0 && shift[3] != 0)) {
            sender.sendMessage(ChatColor.DARK_RED + "You must specify a direction.");
            commandHelp(sender);
            return;
        } else if ((shift[0] != 0 && shift[1] != 0)) {
            sender.sendMessage(ChatColor.DARK_RED + "You cannot specify both west and east components.");
            commandHelp(sender);
            return;
        } else if ((shift[2] != 0 && shift[3] != 0)) {
            sender.sendMessage(ChatColor.DARK_RED + "You cannot specify both north and south components.");
            commandHelp(sender);
            return;
        }

        try {

            Protection newBounds = ProtectionBuilder.shift(protection, shift, getProtections());
            getPlayerStateHandler().requestUpdate(sender instanceof Player ? (Player) sender : null, newBounds, 0);

            sender.sendMessage(ChatColor.YELLOW + "Updating the borders of " + ChatColor.GREEN + newBounds.getName());
            sender.sendMessage(ChatColor.YELLOW + "Confirm by typing " + ChatColor.AQUA + "/pr confirm");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr cancel" + ChatColor.LIGHT_PURPLE + " to cancel.");

        } catch (InvalidProtectionException e) {
            CommandUtil.handleUpdateException(sender, e);
        }


    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr shift <name> <direction and size of shift>");
        sender.sendMessage(ChatColor.YELLOW + "For example, shift 'ExampleProtection1' 5 blocks west and 10 blocks south:");
        sender.sendMessage(ChatColor.RED + "/pr shift ExampleProtection1 w5 s10");
    }
}
