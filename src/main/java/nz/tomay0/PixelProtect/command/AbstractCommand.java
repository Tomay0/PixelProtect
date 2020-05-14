package nz.tomay0.PixelProtect.command;

import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.command.CommandSender;

/**
 * An abstract protection command
 */
public abstract class AbstractCommand {
    private PixelProtectPlugin plugin;

    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public AbstractCommand(PixelProtectPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the protection handler
     *
     * @return
     */
    protected ProtectionHandler getProtections() {
        return plugin.getProtections();
    }

    /**
     * Get the confirmation handler
     *
     * @return
     */
    protected PlayerStateHandler getPlayerStateHandler() {
        return plugin.getPlayerStateHandler();
    }


    /**
     * Get the economy
     */
    protected Economy getEconomy() {
        return plugin.getEconomy();
    }

    /**
     * Get command label.
     *
     * @return string
     */
    public abstract String getCommand();

    /**
     * Get a short description of the command, used for the /pr help menu
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * When you type the command
     *
     * @param sender sender, either a player or the console
     * @param args   arguments, first should always be the command label
     */
    public abstract void onCommand(CommandSender sender, String[] args);
}
