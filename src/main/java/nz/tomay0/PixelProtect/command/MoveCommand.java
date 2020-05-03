package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * Move a protection
 */
public class MoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param protections
     */
    public MoveCommand(ProtectionHandler protections) {
        super(protections);
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
