package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.model.InvalidProtectionException;
import nz.tomay0.PixelProtect.model.Protection;
import nz.tomay0.PixelProtect.model.ProtectionBuilder;
import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Creation of a new protection
 */
public class CreateCommand extends AbstractCommand {
    private static final int DEFAULT_SIZE = 3;

    /**
     * Create new abstract command with a protection handler
     *
     * @param protections
     */
    public CreateCommand(ProtectionHandler protections) {
        super(protections);
    }

    @Override
    public String getCommand() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new protection.";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot create protections from the console.");
            return;
        }

        Player player = (Player) sender;
        String protectionName;
        Integer[] size;
        boolean nameSpecified = false;

        if (args.length < 2) {
            // no arguments - protection with a set size and your name
            protectionName = player.getName();
            size = new Integer[]{
                    DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE
            };
        } else {
            // first assume the name is not specified
            size = CommandUtil.getSize(args, 1, false, false);

            if (size == null) {
                // the first argument is not a size, therefore it shall be considered the size
                nameSpecified = true;
                protectionName = args[1];
                if(args.length > 2) {
                    // get the size from a larger offset
                    size = CommandUtil.getSize(args, 2, false, false);

                    if(size == null) {
                        incorrectFormatting(player);
                        return;
                    }
                }
                else {
                    // use default size
                    size = new Integer[]{
                            DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE
                    };
                }
            }
            else {
                // the name is not specified as the first argument, therefore use the player's name
                protectionName = player.getName();
            }
        }

        // create the protection IF POSSIBLE
        try {
            // try create the protection
            Protection protection = ProtectionBuilder.fromCommand(protectionName, player, size);

            getProtections().addNewProtection(protection);

            player.sendMessage(ChatColor.GREEN + "Protection created");
            // TODO confirmation, show the borders, show additional features
        } catch (InvalidProtectionException e) {
            // invalid protection
            player.sendMessage(ChatColor.DARK_RED + e.getMessage());
        }

    }

    /**
     * Show help when the formatting is incorrect
     */
    private void incorrectFormatting(Player player) {
        // TODO explain formatting
        player.sendMessage(ChatColor.DARK_RED + "Invalid formatting.");
    }
}
