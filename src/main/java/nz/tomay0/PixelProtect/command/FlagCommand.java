package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Update the flags of the protection
 */
public class FlagCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public FlagCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "flag";
    }

    @Override
    public String getDescription() {
        return "Update boolean configurations of the protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);
        if (protection == null) {
            commandHelp(sender);
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.CONFIG)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update the configuration of this protection: " + protection.getName());
            return;
        }
        int fieldArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (args.length <= fieldArg) {
            commandHelp(sender);
            return;
        }


        Flag field = Flag.fromString(args[fieldArg]);

        if (field == null) {
            sender.sendMessage(ChatColor.RED + "Invalid configuration field: " + args[fieldArg]);

            sendConfigurations(sender);
            return;
        }

        if (args.length <= fieldArg + 1) {
            sender.sendMessage(ChatColor.YELLOW + "Enable: " + ChatColor.RED + "/pr config " + protection.getName() + " "
                    + field.toString().toLowerCase() + " true");
            sender.sendMessage(ChatColor.YELLOW + "Disable: " + ChatColor.RED + "/pr config " + protection.getName() + " "
                    + field.toString().toLowerCase() + " false");
            return;
        }

        String value = args[fieldArg + 1].toLowerCase();
        if (!value.equals("false") && !value.equals("true")) {
            sender.sendMessage(ChatColor.YELLOW + "Enable: " + ChatColor.RED + "/pr config " + protection.getName() + " "
                    + field.toString().toLowerCase() + " true");
            sender.sendMessage(ChatColor.YELLOW + "Disable: " + ChatColor.RED + "/pr config " + protection.getName() + " "
                    + field.toString().toLowerCase() + " false");
            return;
        }

        boolean enabled = value.equals("true");

        protection.setFlag(field, enabled);
        sender.sendMessage(ChatColor.YELLOW + (enabled ? "Enabled " : "Disabled ") + ChatColor.GREEN + field.toString().toLowerCase() +
                ChatColor.YELLOW + " for " + ChatColor.GREEN + protection.getName());

    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr flag <name> <flag name> <true/false>");

        sendConfigurations(sender);
    }

    /**
     * Send a list of configurations
     *
     * @param sender
     */
    private void sendConfigurations(CommandSender sender) {
        StringBuilder allConfig = new StringBuilder();
        for (Flag field : Flag.values()) {
            allConfig.append(field.toString().toLowerCase() + ", ");
        }

        sender.sendMessage(ChatColor.YELLOW + "Flags to choose from:");
        sender.sendMessage(allConfig.substring(0, allConfig.length() - 2));
    }
}
