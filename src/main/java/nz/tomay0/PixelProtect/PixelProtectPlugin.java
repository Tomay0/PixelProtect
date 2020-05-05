package nz.tomay0.PixelProtect;

import nz.tomay0.PixelProtect.command.CommandHandler;
import nz.tomay0.PixelProtect.confirm.ConfirmationHandler;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin entry point for pixel protect
 */
public class PixelProtectPlugin extends JavaPlugin {
    private ProtectionHandler protectionHandler;
    private ConfirmationHandler confirmationHandler;

    @Override
    public void onEnable() {

        // setup protection handler
        protectionHandler = new ProtectionHandler(getProtectionDirectory());
        confirmationHandler = new ConfirmationHandler(protectionHandler);

        getServer().getPluginManager().registerEvents(confirmationHandler, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, confirmationHandler, 20, 20);

        getServer().getPluginManager().registerEvents(new GriefListener(protectionHandler), this);

        // setup command handler
        getCommand("protect").setExecutor(new CommandHandler(this));

        // log that the plugin has loaded successfully
        getLogger().log(Level.INFO, "Initialized PixelProtect successfully");
    }


    /**
     * Return the claims/ directory
     *
     * @return claims directory
     */
    private File getProtectionDirectory() {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists())
            dataFolder.mkdir();

        File prDir = new File(dataFolder, "claims");
        if (!prDir.exists())
            prDir.mkdir();

        return prDir;
    }

    /**
     * Get protections
     *
     * @return
     */
    public ProtectionHandler getProtections() {
        return protectionHandler;
    }

    /**
     * Get confirmation handler
     *
     * @return
     */
    public ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }
}
