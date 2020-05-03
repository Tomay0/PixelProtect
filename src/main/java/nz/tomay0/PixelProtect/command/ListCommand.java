package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.command.CommandSender;

/**
 * List protections you have permissions
 */
public class ListCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public ListCommand(PixelProtectPlugin plugin) {
        super(plugin);
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
