package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.command.CommandSender;

/**
 * Shift a protection
 */
public class ShiftCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ShiftCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "shift";
    }

    @Override
    public String getDescription() {
        return "Move your protection a set number of blocks in a chosen direction.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("shift TODO");
    }
}
