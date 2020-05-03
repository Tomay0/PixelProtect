package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * An abstract protection command
 */
public abstract class AbstractCommand {
    private ProtectionHandler protections;

    /**
     * Create new abstract command with a protection handler
     * @param protections
     */
    public AbstractCommand(ProtectionHandler protections) {
        this.protections = protections;
    }

    /**
     * Get the protection handler
     * @return
     */
    protected ProtectionHandler getProtections() {
        return protections;
    }

    /**
     * Get command label.
     * @return string
     */
    public abstract String getCommand();

    /**
     * Get a short description of the command, used for the /pr help menu
     * @return
     */
    public abstract String getDescription();

    /**
     * When you type the command
     * @param sender sender, either a player or the console
     * @param args arguments, first should always be the command label
     */
    public abstract void onCommand(CommandSender sender, String[] args);
}
