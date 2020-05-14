package nz.tomay0.PixelProtect;

public class PluginConfig {

    /**
     * Plugin config from a yml
     *
     * @param plugin
     */
    public PluginConfig(PixelProtectPlugin plugin) {
        // TODO make the plugin configurable
    }

    /**
     * Default plugin config
     */
    public PluginConfig() {

    }

    public double getProtectionSetupCost() {
        return 50;
    }

    public double getProtectionBlockCost() {
        return 0.2;
    }


}
