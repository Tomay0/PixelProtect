package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Teleport to the home of a protection
 */
public class HomeCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public HomeCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "home";
    }

    @Override
    public boolean getConsole() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Teleport to a protection's home.";
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
        if (!getProtections().hasPermission(sender, protection, Perm.HOME)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to teleport to this protection: " + protection.getName());
            return;
        }
        int homeArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        String home = Protection.DEFAULT_HOME;

        if (args.length > homeArg) {
            home = args[homeArg];
        }

        Location location = protection.getHome(home);
        if (location == null) {
            player.sendMessage(ChatColor.DARK_RED + "That home does not exist.");
            return;
        }

        getPlayerStateHandler().requestTeleport(player, location);

        sender.sendMessage(ChatColor.YELLOW + "Teleporting to " + ChatColor.GREEN + home + ChatColor.YELLOW + " of " + ChatColor.GREEN + protection.getName());
        sender.sendMessage(ChatColor.YELLOW + "Stand still for 3 seconds to confirm your teleport...");
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr home <name> <home name>");
        sender.sendMessage(ChatColor.YELLOW + "Leave out the home name to teleport to the default home.");
    }
}
