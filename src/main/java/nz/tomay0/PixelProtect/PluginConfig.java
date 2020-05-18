package nz.tomay0.PixelProtect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Plugin config file
 */
public class PluginConfig {
    private static PluginConfig config = null;

    /**
     * Load the plugin config from a file
     *
     * @param file
     */
    public static void loadConfig(File file) {
        config = new PluginConfig(file);
    }

    /**
     * Get the plugin config
     *
     * @return
     */
    public static PluginConfig getInstance() {
        if (config == null) {
            config = new PluginConfig();
        }
        return config;
    }


    private int maxProtections = -1;
    private int maxArea = -1;
    private int defaultRadius = 3;
    private int minDiameter = 5;
    private int maxHomes = -1;
    private int blocksPerHome = 2000;
    private double costPerBlock = 0.5;
    private double initialCost = 25.5;
    private List<String> disabledWorlds = new ArrayList<>();

    /**
     * Plugin config from a yml
     *
     * @param file file
     */
    private PluginConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        maxProtections = getValue(config, "max-protections", maxProtections);
        maxArea = getValue(config, "max-area", maxArea);
        defaultRadius = getValue(config, "default-radius", defaultRadius);
        minDiameter = getValue(config, "min-diameter", minDiameter);
        maxHomes = getValue(config, "max-homes", maxHomes);
        blocksPerHome = getValue(config, "blocks-per-home", blocksPerHome);
        costPerBlock = getValue(config, "cost-per-block", costPerBlock);
        initialCost = getValue(config, "initial-cost", initialCost);
        disabledWorlds = getValue(config, "disabled-worlds", new ArrayList<>());

        // check for invalid values
        if (maxProtections < -1) maxProtections = -1;

        if (minDiameter < 1) minDiameter = 1;

        if (defaultRadius <= -1) defaultRadius = -1;
        else if (defaultRadius * 2 + 1 < minDiameter) defaultRadius = (int) Math.ceil((minDiameter - 1.0) / 2.0);

        if (maxHomes < -1) maxHomes = -1;
        if (maxHomes == 0) maxHomes = 1;

        if (blocksPerHome <= 0) blocksPerHome = -1;

        if (costPerBlock < 0) costPerBlock = 0;

        if (initialCost < 0) initialCost = 0;

        if (maxArea <= -1) maxArea = -1;
        else if (maxArea < 1) maxArea = 1;
    }


    /**
     * Get a value from the config. Set this value if it does not exist in the config
     *
     * @param config       config
     * @param key          key to check
     * @param defaultValue default value
     * @param <T>          type of value
     * @return
     */
    private <T> T getValue(YamlConfiguration config, String key, T defaultValue) {
        if (!config.contains(key)) {
            Bukkit.getLogger().log(Level.WARNING, key + " was not found in the config.");
            return defaultValue;
        }
        Object value = config.get(key);

        if (!(defaultValue.getClass().isInstance(value))) {
            Bukkit.getLogger().log(Level.WARNING, key + " within the config is not the right data type.");
            return defaultValue;
        }

        return (T) value;
    }


    /**
     * Default plugin config
     */
    public PluginConfig() {

    }


    /**
     * Maximum area
     *
     * @return
     */
    public int getMaxArea() {
        return maxArea;
    }

    /**
     * Maximum protections
     *
     * @return
     */
    public int getMaxProtections() {
        return maxProtections;
    }

    /**
     * Minimum protection diameter
     *
     * @return
     */
    public int getMinDiameter() {
        return minDiameter;
    }

    /**
     * Maximum homes
     *
     * @return
     */
    public int getMaxHomes() {
        return maxHomes;
    }

    /**
     * Get blocks per home
     *
     * @return
     */
    public int getBlocksPerHome() {
        return blocksPerHome;
    }

    /**
     * Get cost of protection per block
     *
     * @return
     */
    public double getCostPerBlock() {
        return costPerBlock;
    }

    /**
     * Get initial cost of protection
     *
     * @return
     */
    public double getInitialCost() {
        return initialCost;
    }

    /**
     * Get default radius
     *
     * @return
     */
    public int getDefaultRadius() {
        return defaultRadius;
    }

    /**
     * Get disabled worlds
     *
     * @return
     */
    public Set<String> getDisabledWorlds() {
        return new HashSet<>(disabledWorlds);
    }
}
