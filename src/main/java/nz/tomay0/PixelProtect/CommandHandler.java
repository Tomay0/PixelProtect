package nz.tomay0.PixelProtect;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles the implementation of the /protect /pr command
 */
public class CommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("pr") && !label.equalsIgnoreCase("protect"))
            return false;

        // TODO handle commands
        sender.sendMessage(ChatColor.YELLOW + "Hello world!");


        return true;
    }
}
