package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Update player permissions
 */
public class SetPermCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public SetPermCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setperm";
    }

    @Override
    public String getDescription() {
        return "Update the permissions of another player.";
    }

    @Override
    public boolean getConsole() {
        return true;
    }
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            commandHelp(sender);
            return;
        }

        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);
        if (protection == null) {
            commandHelp(sender);
            return;
        }
        if (protection.isAdminProtection()) {
            sender.sendMessage(ChatColor.DARK_RED + "This command is not avaliable for admin protections.");
            return;
        }

        int usernameArg = (getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (args.length < usernameArg + 2) {
            commandHelp(sender);
            return;
        }

        // check your perms
        if (!getProtections().hasPermission(sender, protection, Perm.SETPERMS)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to set permissions of other players in this protection.");
            return;
        }

        // get username TODO use non deprecated version (this is very slow)

        String username = args[usernameArg];
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);

        username = player.getName();
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "The player, " + username + ", has not joined this server before.");
            return;
        }
        String uuid = player.getUniqueId().toString();

        // get perm level
        PermLevel permLevel = PermLevel.fromString(args[usernameArg + 1]);

        // perm
        if (permLevel == null) {
            Perm perm = Perm.fromString(args[usernameArg + 1]);

            if (perm == null) {
                // unknown permission
                if (args.length == usernameArg + 1) {
                    commandHelp(sender);
                } else {
                    permissionOptions(sender);
                }
                return;
            }

            if (args.length < usernameArg + 3) {
                sender.sendMessage(ChatColor.YELLOW + "Enable " + args[usernameArg + 1] + ": " +
                        ChatColor.RED + "/pr setperm " + protection.getName() + " " + username + " " + perm.toString().toLowerCase() + "true");
                sender.sendMessage(ChatColor.YELLOW + "Disable " + args[usernameArg + 1] + ": " +
                        ChatColor.RED + "/pr setperm " + protection.getName() + " " + username + " " + perm.toString().toLowerCase() + "false");
                return;
            }

            String value = args[usernameArg + 2].toLowerCase();
            if (!value.equals("false") && !value.equals("true")) {
                sender.sendMessage(ChatColor.YELLOW + "Enable " + args[usernameArg + 1] + ": " +
                        ChatColor.RED + "/pr setperm " + protection.getName() + " " + username + " " + perm.toString().toLowerCase() + "true");
                sender.sendMessage(ChatColor.YELLOW + "Disable " + args[usernameArg + 1] + ": " +
                        ChatColor.RED + "/pr setperm " + protection.getName() + " " + username + " " + perm.toString().toLowerCase() + "false");
                return;
            }

            boolean enabled = value.equals("true");

            if (!getProtections().hasPermissionToSetSpecificPermission(sender, protection, uuid, perm)) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update that permission for that player.");
                return;
            }

            protection.setSpecificPermission(uuid, perm, enabled);

            sender.sendMessage(ChatColor.GREEN + username + ChatColor.YELLOW + " now has the " +
                    ChatColor.GREEN + perm.toString().toLowerCase() + ChatColor.YELLOW + " permission " +
                    (enabled ? "enabled" : " disabled"));
        }

        // perm level
        else {
            if (!getProtections().hasPermissionToSetPermissionLevel(sender, protection, uuid, permLevel)) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update the permission level of that player to that level.");
                return;
            }
            // check the player you are setting won't go over the max protections
            if (permLevel == PermLevel.OWNER && PluginConfig.getInstance().getMaxProtections() != -1 &&
                    getProtections().getProtectionsOwned(player).size() >= PluginConfig.getInstance().getMaxProtections()) {
                sender.sendMessage(ChatColor.DARK_RED + "This player has reached the maximum number of protections. They cannot be set to owner.");
                return;
            }


            protection.setPermissionLevel(uuid, permLevel);

            sender.sendMessage(ChatColor.GREEN + username + ChatColor.YELLOW + " now has the permission level of " +
                    ChatColor.GREEN + permLevel.toString().toLowerCase());
        }
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr setperm <name> <username> <permission level>");
        sender.sendMessage(ChatColor.YELLOW + "Permission Level options: " + ChatColor.WHITE + "none, member, admin, owner");
        sender.sendMessage(ChatColor.RED + "/pr setperm <name> <username> <permission> <true/false>");
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
