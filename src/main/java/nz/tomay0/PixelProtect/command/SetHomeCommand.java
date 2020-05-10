package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Set a protections home
 */
public class SetHomeCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public SetHomeCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "sethome";
    }

    @Override
    public String getDescription() {
        return "Set a protection's home.";
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

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.SETHOME)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to set this protection's home: " + protection.getName());
            return;
        }
        int homeArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        String home = Protection.DEFAULT_HOME;

        if (args.length > homeArg) {
            home = args[homeArg];
        }

        protection.setHome(home, player.getLocation());

        sender.sendMessage(ChatColor.YELLOW + "Set the home: " + ChatColor.GREEN + home + ChatColor.YELLOW + " of " + ChatColor.GREEN + protection.getName());
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr sethome <name> <home name>");
        sender.sendMessage(ChatColor.YELLOW + "Leave out the home name to set to the default home.");
    }
}
