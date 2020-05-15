package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Size command
 */
public class SizeCommand extends AbstractCommand {
    /**
     * Size Command
     */
    public SizeCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "size";
    }

    @Override
    public String getDescription() {
        return "Display the area and dimensions of a protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // check name of protection is specified
        Protection protection = CommandUtil.getExistingProtection(getProtections(), sender, args);

        if (protection == null) {
            sender.sendMessage(ChatColor.RED + "/pr size <name>");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + protection.getName());
        sender.sendMessage(ChatColor.YELLOW + "Blocks claimed: " + ChatColor.AQUA + protection.getArea());
        sender.sendMessage(ChatColor.YELLOW + "North/South diameter: " + ChatColor.AQUA + (protection.getSouth() - protection.getNorth()) +
                ChatColor.YELLOW + " West/East diameter: " + ChatColor.AQUA + (protection.getEast() - protection.getWest()));

    }
}
