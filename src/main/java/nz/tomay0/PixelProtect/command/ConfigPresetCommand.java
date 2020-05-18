package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Allows you to configure your protection using presets
 */
public class ConfigPresetCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ConfigPresetCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "configpreset";
    }

    @Override
    public String getDescription() {
        return "Configure your protection based on a preset.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);
        if (protection == null) {
            commandHelp(sender, "<name>");
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.CONFIG)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update the configuration of this protection: " + protection.getName());
            return;
        }
        int presetArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (args.length <= presetArg) {
            commandHelp(sender, "<name>");
            return;
        }

        if (args[presetArg].equalsIgnoreCase("default")) {
            // set default levels
            for (Perm perm : Perm.values()) {
                PermLevel oldLevel = protection.getMinPermissionLevel(perm);
                PermLevel newLevel = perm.getDefaultLevelRequired();

                if (oldLevel != newLevel &&
                        getProtections().hasPermission(sender, protection, perm) &&
                        (!(sender instanceof Player) || protection.isAdminProtection() || protection.getPermissionLevel(((Player) sender).getUniqueId().toString()).hasPermissionsOfLevel(newLevel))) {
                    protection.setDefaultPermissionLevel(perm, newLevel);
                }
            }
            // set flags
            for (Flag flag : Flag.values()) {
                boolean oldValue = protection.getFlag(flag);

                if (oldValue != flag.getDefaultValue()) {
                    protection.setFlag(flag, flag.getDefaultValue());
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Changed the preset of " + ChatColor.GREEN + protection.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "default");
        } else if (args[presetArg].equalsIgnoreCase("wilderness")) {
            // set min levels to none
            for (Perm perm : Arrays.asList(Perm.BUILD, Perm.CHEST, Perm.INTERACT, Perm.KILL_FRIENDLY, Perm.KILL_HOSTILE)) {
                if (getProtections().hasPermission(sender, protection, perm)) {
                    protection.setDefaultPermissionLevel(perm, PermLevel.NONE);
                }
            }

            // set flags
            for (Flag flag : Flag.values()) {
                boolean oldValue = protection.getFlag(flag);

                if (oldValue != flag.getNoProtection()) {
                    protection.setFlag(flag, flag.getNoProtection());
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Changed the preset of " + ChatColor.GREEN + protection.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "wilderness");

        } else if (args[presetArg].equalsIgnoreCase("public_farm") || args[presetArg].equalsIgnoreCase("publicfarm")) {
            // set some min levels to none
            for (Perm perm : Arrays.asList(Perm.CHEST, Perm.INTERACT, Perm.KILL_FRIENDLY, Perm.KILL_HOSTILE, Perm.HOME)) {
                if (getProtections().hasPermission(sender, protection, perm)) {
                    protection.setDefaultPermissionLevel(perm, PermLevel.NONE);
                }
            }
            if (getProtections().hasPermission(sender, protection, Perm.BUILD)) {
                protection.setDefaultPermissionLevel(Perm.BUILD, PermLevel.MEMBER);
            }
            sender.sendMessage(ChatColor.YELLOW + "Changed the preset of " + ChatColor.GREEN + protection.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "public farm");

        } else {
            commandHelp(sender, protection.getName());
        }
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.RED + "/pr configpreset " + name + " default");
        sender.sendMessage(ChatColor.RED + "/pr configpreset " + name + " wilderness");
        sender.sendMessage(ChatColor.RED + "/pr configpreset " + name + " public_farm");
    }
}
