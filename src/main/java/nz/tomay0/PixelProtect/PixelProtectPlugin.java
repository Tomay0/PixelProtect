package nz.tomay0.PixelProtect;

import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.command.CommandHandler;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.protection.SequentialProtectionHandler;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;

/**
 * Main plugin entry point for pixel protect
 */
public class PixelProtectPlugin extends JavaPlugin {
    /*

    TODO:
    - reduce lag on command to show borders
    - teleport timer
    - fix disabled worlds
    - command for admins to override permissions
    - dynmap integration
    - optimize offline player name recognition
    - help for players. eg: tell them "oh this is the wilderness you can't build here"
    - maybe some more bug fixing

    Maybe?
    - config default perms - some perm presets maybe
    - config dynmap colour?
    - disable pickup blocks on the ground
    - bonemeal grass over border?
    - seperate out entity interact? could allow people to interact with their own pets/vehicles storage minecarts can be considered chests
    - consider making the command formatting more lenient
    - consider making /pr perms have multiple pages.
    - auto sizes??
    - motd changer
    - colour changer

     */

    private static final boolean REQUIRES_VAULT = true;

    private ProtectionHandler protectionHandler;
    private PlayerStateHandler playerStateHandler;
    private Economy vaultEconomy;

    @Override
    public void onEnable() {
        // init vault
        if (!setupEconomy() && REQUIRES_VAULT) {
            getLogger().log(Level.SEVERE, "Error: Could not load Economy. Vault 1.7 and an economy plugin are required to run Pixel Protect.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // setup protection handler
        protectionHandler = new SequentialProtectionHandler(getProtectionDirectory());
        playerStateHandler = new PlayerStateHandler(protectionHandler);
        PluginConfig.loadConfig(getConfigFile());

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
     * Setup vault economy.
     *
     * @return if the setup was successful.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;

        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }


    /**
     * Return the claims/ directory
     *
     * @return claims directory
     */
    public File getProtectionDirectory() {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists())
            dataFolder.mkdir();

        File prDir = new File(dataFolder, "claims");
        if (!prDir.exists())
            prDir.mkdir();

        return prDir;
    }

    /**
     * Return the config.yml file. If it doesn't exist it will create it using the default one in the resources.
     *
     * @return file
     */
    private File getConfigFile() {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists())
            dataFolder.mkdir();

        File config = new File(dataFolder, "config.yml");
        if (!config.exists()) {
            try {
                // copy the file
                InputStream is = PixelProtectPlugin.class.getResourceAsStream("/config.yml");
                if (is == null) {
                    getLogger().log(Level.WARNING, "Could not load default config.yml. Resource not found in jar");
                    return null;
                }

                OutputStream os = new FileOutputStream(config);
                byte[] buffer = new byte[2048];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.flush();
                os.close();

            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Could not load default config.yml");
            }


        }

        return config;
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

    /**
     * Get economy
     */
    public Economy getEconomy() {
        return vaultEconomy;
    }
}
