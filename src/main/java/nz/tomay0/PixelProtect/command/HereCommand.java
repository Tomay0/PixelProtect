package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Here command
 */
public class HereCommand extends AbstractCommand {
    /**
     * Here Command
     */
    public HereCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "here";
    }

    @Override
    public String getDescription() {
        return "Say the protection you are standing in.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        Protection protection = getProtections().getProtectionAt(player.getLocation());

        if (protection == null) {
            player.sendMessage(ChatColor.YELLOW + "You are in the wilderness");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You are in " + ChatColor.GREEN + protection.getName());
        }
    }
}
