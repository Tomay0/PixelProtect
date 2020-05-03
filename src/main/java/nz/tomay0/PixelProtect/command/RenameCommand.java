package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.command.CommandSender;

/**
 * Rename a protection
 */
public class RenameCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public RenameCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Change the name of your protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("rename TODO");
    }
}
