package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Update the flags of the
 */
public class SetMinLevelCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public SetMinLevelCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setminlevel";
    }

    @Override
    public String getDescription() {
        return "Set the minimum level to acquire a specific permission.";
    }

    @Override
    public boolean getConsole() {
        return true;
    }
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args, 4);
        if (protection == null) {
            commandHelp(sender);
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.CONFIG)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update the configuration of this protection: " + protection.getName());
            return;
        }
        int permissionArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (args.length <= permissionArg) {
            commandHelp(sender);
            return;
        }

        Perm perm = Perm.fromString(args[permissionArg]);

        if (perm == null) {
            sender.sendMessage(ChatColor.RED + "Unknown permission: " + args[permissionArg]);
            permissionOptions(sender);
            return;
        }

        // check that you have permission to set this
        if (!getProtections().hasPermission(sender, protection, perm)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to change the default level of this permission.");
            return;
        }

        // get permission level
        if (args.length <= permissionArg + 1) {
            sender.sendMessage(ChatColor.RED + "/pr setminlevel " + protection.getName() + " " + perm.toString().length() + " <permission level>");
            sender.sendMessage(ChatColor.YELLOW + "Permission Level options: " + ChatColor.WHITE + "none, member, admin, owner");

            return;
        }

        PermLevel level = PermLevel.fromString(args[permissionArg + 1]);

        if (level == null) {
            sender.sendMessage(ChatColor.RED + "Unknown permission level: " + args[permissionArg + 1]);
            sender.sendMessage(ChatColor.YELLOW + "Permission Level options: " + ChatColor.WHITE + "none, member, admin, owner");

            return;
        }


        // if the level you changed this to is higher than your current level - don't allow
        if (sender instanceof Player && !protection.isAdminProtection() && !protection.getPermissionLevel(((Player) sender).getUniqueId().toString()).hasPermissionsOfLevel(level)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to set the default permission level to higher than your own permission level.");
            return;
        }

        // set default level
        protection.setDefaultPermissionLevel(perm, level);
        sender.sendMessage(ChatColor.YELLOW + "Set the minimum permission level for " + ChatColor.GREEN + perm.toString().toLowerCase() +
                ChatColor.YELLOW + " to " + ChatColor.GREEN + level.toString().toLowerCase());
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr setminlevel <name> <permission> <permission level>");
        sender.sendMessage(ChatColor.YELLOW + "Permission Level options: " + ChatColor.WHITE + "none, member, admin, owner");
        permissionOptions(sender);
    }

    private void permissionOptions(CommandSender sender) {
        StringBuilder allPerms = new StringBuilder();
        for (Perm perm : Perm.values()) {
            allPerms.append(perm.toString().toLowerCase() + ", ");
        }

        sender.sendMessage(ChatColor.YELLOW + "Permission options: " + ChatColor.WHITE + allPerms.substring(0, allPerms.length() - 2));
    }
}
