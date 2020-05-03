package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * List protections you have permissions
 */
public class ListCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param protections
     */
    public ListCommand(ProtectionHandler protections) {
        super(protections);
    }

    @Override
    public String getCommand() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all protections you have permissions for.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("list TODO");
    }
}
