package nz.tomay0.PixelProtect;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PixelProtectPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Pixel Protect initialised");

        // setup command handler
        getCommand("protect").setExecutor(new CommandHandler());
    }
}
