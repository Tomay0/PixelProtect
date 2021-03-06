package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Creation of a new admin protection
 */
public class CreateAdminCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public CreateAdminCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "createadmin";
    }

    @Override
    public boolean getConsole() {
        return false;
    }

    @Override
    public String getPermission() {
        return "pixelprotect.admin";
    }

    @Override
    public String getDescription() {
        return "Create a new admin protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        String protectionName;
        Integer[] size;
        boolean nameSpecified = false;

        int defaultSize = PluginConfig.getInstance().getDefaultRadius();


        if (args.length < 2) {
            if (defaultSize == -1) {
                incorrectFormatting(player);
                return;
            }
            // no arguments - protection with a set size and your name
            protectionName = player.getName();
            size = new Integer[]{
                    defaultSize, defaultSize, defaultSize, defaultSize
            };
        } else {
            // first assume the name is not specified
            size = CommandUtil.getSize(args, 1, false);

            if (size == null) {
                // the first argument is not a size, therefore it shall be considered the size
                nameSpecified = true;
                protectionName = args[1];
                if (args.length > 2) {
                    // get the size from a larger offset
                    size = CommandUtil.getSize(args, 2, false);

                    if (size == null) {
                        incorrectFormatting(player);
                        return;
                    }
                } else {
                    if (defaultSize == -1) {
                        incorrectFormatting(player);
                        return;
                    }
                    // use default size
                    size = new Integer[]{
                            defaultSize, defaultSize, defaultSize, defaultSize
                    };
                }
            } else {
                // the name is not specified as the first argument, therefore use the player's name
                protectionName = player.getName();
            }
        }

        // create the protection IF POSSIBLE
        try {
            Protection protection = ProtectionBuilder.fromAdminCommand(protectionName, player, size, getProtections());
            getPlayerStateHandler().requestCreate(player, protection, 0);

            player.sendMessage(ChatColor.YELLOW + "Creating a new admin protection named " + ChatColor.GREEN + protectionName);
            player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.RED + "/pr cancel" + ChatColor.YELLOW + " to cancel.");
            player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/pr confirm" + ChatColor.YELLOW + " to confirm.");
        } catch (InvalidProtectionException e) {
            // invalid protection
            player.sendMessage(ChatColor.DARK_RED + e.getMessage());
            switch (e.getReason()) {
                case PROTECTION_OVERLAPPING:
                    player.sendMessage(ChatColor.YELLOW + "Try move somewhere else and type the command again.");
                    break;
                case PROTECTION_ALREADY_EXISTS:
                    player.sendMessage(ChatColor.RED + "/pr createadmin <name> <size> " + ChatColor.LIGHT_PURPLE + " is the command to create a protection. Replace <name> with a different name that doesn't exist already.");
                    break;
                case INVALID_BORDERS:
                    int minRadius = (PluginConfig.getInstance().getMinDiameter() - 1) / 2;
                    player.sendMessage(ChatColor.YELLOW + "The minimum diameter of a protection for either direction is " + ChatColor.RED + PluginConfig.getInstance().getMinDiameter() +
                            ChatColor.YELLOW + " blocks. (radius of " + ChatColor.RED + minRadius + ChatColor.YELLOW + ")");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "For the smallest radius protection, type: " + ChatColor.RED + "/pr create <name>");
                    break;
            }
        }

    }

    /**
     * Show help when the formatting is incorrect
     */
    private void incorrectFormatting(Player player) {
        player.sendMessage(ChatColor.DARK_RED + "Invalid formatting. Consider these example commands as a guide:");
        player.sendMessage(ChatColor.RED + "/pr createadmin ExampleProtection1 20" + ChatColor.WHITE + " - " + ChatColor.LIGHT_PURPLE + "Create an admin protection called 'ExampleProtection1' with a radius of 20 from where you are standing.");
        player.sendMessage(ChatColor.RED + "/pr createadmin ExampleProtection1 n5 s10 ew4" + ChatColor.WHITE + " - " + ChatColor.LIGHT_PURPLE + "Create an admin protection called 'ExampleProtection1' that expands 5 blocks north, 10 blocks south and 4 blocks east/west.");
        player.sendMessage(ChatColor.YELLOW + "Note that the name must not have spaces.");
    }
}
