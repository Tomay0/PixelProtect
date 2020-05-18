package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

/**
 * Import command
 */
public class ImportCommand extends AbstractCommand {
    /**
     * Import Command
     */
    public ImportCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "import";
    }

    @Override
    public String getDescription() {
        return "Import claim data from another plugin.";
    }

    @Override
    public boolean getConsole() {
        return true;
    }
    @Override
    public String getPermission() {
        return "pixelprotect.import";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 1 || !args[1].equalsIgnoreCase("griefprevention")) {
            sender.sendMessage(ChatColor.YELLOW + "Put all griefprevention claim data into a directory named import/ within the pixel protect plugin data folder and type:");
            sender.sendMessage(ChatColor.RED + "/pr import griefprevention");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr import griefprevention -reset" + ChatColor.LIGHT_PURPLE + " to also remove all currently created protections");
            return;
        }

        File dir = new File(getPlugin().getDataFolder(), "import");

        if (!dir.exists() || !dir.isDirectory()) {
            sender.sendMessage(ChatColor.RED + "Import directory not found. Create it within the pixel protect data folder.");
            return;
        }

        if (args.length > 2 && args[2].equalsIgnoreCase("-reset")) {
            getProtections().removeAll();
        }

        int created = 0;
        int admin = 0;

        for (File file : Arrays.asList(dir.listFiles())) {
            if (file.isDirectory()) continue;

            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            try {
                Protection protection = ProtectionBuilder.fromGriefPreventionYaml(yml, getPlugin().getProtectionDirectory(), getProtections());
                getProtections().addNewProtection(protection);

                created++;
                if (protection.isAdminProtection()) admin++;
                file.delete();
            } catch (InvalidProtectionException e) {
                sender.sendMessage(ChatColor.RED + "Error in: " + file.getName());
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        }

        // delete import folder if empty
        if (dir.listFiles().length == 0) {
            dir.delete();
        }

        sender.sendMessage(ChatColor.GREEN + "" + created + " protections created.");
        if (admin > 0) {
            sender.sendMessage(ChatColor.YELLOW + "" + admin + " admin protections were created. You should consider renaming these appropriately (eg: admin1 could be spawn, etc)");
        }

    }
}
