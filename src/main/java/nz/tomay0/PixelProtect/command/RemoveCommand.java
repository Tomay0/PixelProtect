package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * Remove a protection
 */
public class RemoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param protections
     */
    public RemoveCommand(ProtectionHandler protections) {
        super(protections);
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
