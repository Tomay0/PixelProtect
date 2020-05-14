package nz.tomay0.PixelProtect;

import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.command.CommandHandler;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.protection.SequentialProtectionHandler;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin entry point for pixel protect
 */
public class PixelProtectPlugin extends JavaPlugin {
    /*

    TODO overall:


    commands
    - /pr config
    - /pr preset
    - consider making the command formatting more lenient
    - consider making /pr perms have multiple pages.
    - command for admins to override permissions
    - administrative protections (eg spawn)
    - auto sizes??
    - motd
    - colour

    optimization/performance
    - optimize offline player name recognition

    integration
    - grief prevention importer
    - economy integration
    - dynmap integration

    config
    - default perms - some perm presets maybe
    - home limit
    - protection cost
    - max protection size
    - dynmap colour?

    misc
    - help for players. eg: tell them "oh this is the wilderness you can't build here"

    grief

    -pickup blocks on the ground
    - bonemeal grass over border?
    - seperate out entity interact? could allow people to interact with their own pets/vehicles storage minecarts can be considered chests

     */


    /*

    TODO config option ideas

    PVP
    Dispenser projectiles doing damage
    Pickup blocks/items
    Pistons
    Nether portal generation
    Fluid flow
    dispenser projectiles
    block form
    Mob griefing in general
    fire spread
    Hostile mobs doing damage to other entities
    Pressure plate = interaction sorta thing

     */
    private static final boolean REQUIRES_VAULT = true;

    private ProtectionHandler protectionHandler;
    private PlayerStateHandler playerStateHandler;
    private Economy vaultEconomy;
    private PluginConfig config;

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
        config = new PluginConfig(this);

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

    /**
     * Get economy
     */
    public Economy getEconomy() {
        return vaultEconomy;
    }

    /**
     * Get the plugin configuration
     *
     * @return
     */
    public PluginConfig getPluginConfig() {
        return config;
    }
}
