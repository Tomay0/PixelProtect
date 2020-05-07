package nz.tomay0.PixelProtect;

import nz.tomay0.PixelProtect.command.CommandHandler;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.plugin.java.JavaPlugin;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin entry point for pixel protect
 */
public class PixelProtectPlugin extends JavaPlugin {
    private ProtectionHandler protectionHandler;
    private PlayerStateHandler playerStateHandler;

    @Override
    public void onEnable() {

        // setup protection handler
        protectionHandler = new ProtectionHandler(getProtectionDirectory());
        playerStateHandler = new PlayerStateHandler(protectionHandler);

        GriefListener griefListener = new GriefListener(this);

        getServer().getPluginManager().registerEvents(playerStateHandler, this);
        getServer().getPluginManager().registerEvents(griefListener, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, playerStateHandler, 20, 20);

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
     * Get player state handler
     *
     * @return
     */
    public PlayerStateHandler getPlayerStateHandler() {
        return playerStateHandler;
    }
}
