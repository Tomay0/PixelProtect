package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;

/**
 * Toggle Override command
 */
public class ToggleOverrideCommand extends AbstractCommand {
    /**
     * Toggle Override Command
     */
    public ToggleOverrideCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "toggleoverride";
    }

    @Override
    public String getDescription() {
        return "Toggle whether you are overriding other player's protection permissions.";
    }

    @Override
    public boolean getConsole() {
        return false;
    }
    @Override
    public String getPermission() {
        return "pixelprotect.override";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
        }

        getPlayerStateHandler().togglePermissionOverride((Player) sender);
    }
}
