package nz.tomay0.PixelProtect;

import nz.tomay0.PixelProtect.command.CommandHandler;
import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin entry point for pixel protect
 */
public class PixelProtectPlugin extends JavaPlugin {
    //
    private ProtectionHandler protectionHandler;

    @Override
    public void onEnable() {

        // setup protection handler
        protectionHandler = new ProtectionHandler(getProtectionDirectory());

        // setup command handler
        getCommand("protect").setExecutor(new CommandHandler(protectionHandler));

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
}
