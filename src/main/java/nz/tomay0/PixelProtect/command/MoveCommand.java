package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.command.CommandSender;

/**
 * Move a protection
 */
public class MoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public MoveCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "move";
    }

    @Override
    public String getDescription() {
        return "Move your protection to where you are standing.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("move TODO");
    }
}
