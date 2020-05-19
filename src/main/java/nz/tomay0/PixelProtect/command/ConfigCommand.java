package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Config command
 */
public class ConfigCommand extends AbstractCommand {
    /**
     * Config Command
     */
    public ConfigCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "config";
    }

    @Override
    public boolean getConsole() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Display the configuration of a protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // check name of protection is specified
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args, 2);

        if (protection == null) {
            sender.sendMessage(ChatColor.RED + "/pr config <name>");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Displaying config for: " + ChatColor.GREEN + "" + ChatColor.BOLD + protection.getName());

        // flags
        sender.sendMessage(ChatColor.AQUA + "Flags:");
        StringBuilder flags = new StringBuilder();
        for (Flag flag : Flag.values()) {
            flags.append((protection.getFlag(flag) ? ChatColor.GREEN : ChatColor.RED) + flag.toString().toLowerCase() + " ");
        }
        sender.sendMessage(flags.substring(0, flags.length() - 2));

        // minimum level
        sender.sendMessage(ChatColor.AQUA + "Minimum permission levels:");
        StringBuilder levels = new StringBuilder();
        for (Perm perm : Perm.values()) {
            levels.append(ChatColor.YELLOW + perm.toString().toLowerCase() + ChatColor.WHITE + ": " + protection.getMinPermissionLevel(perm) + ", ");
        }
        sender.sendMessage(levels.substring(0, levels.length() - 2));

    }
}
