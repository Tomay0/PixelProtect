package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * Expansion of a protection
 */
public class ExpandCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ExpandCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "expand";
    }

    @Override
    public String getDescription() {
        return "Expand/Contract the size of your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("expand TODO");
    }
}
