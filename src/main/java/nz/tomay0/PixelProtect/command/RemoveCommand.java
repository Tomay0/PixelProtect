package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.command.CommandSender;

/**
 * Remove a protection
 */
public class RemoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public RemoveCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("remove TODO");
    }
}
