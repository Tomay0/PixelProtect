package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.exception.ProtectionExceptionReason;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class CommandUtil {

    /**
     * Get a protection expansion from arguments in a string.
     *
     * @param args          arguments
     * @param startIndex    Where the arguments begin
     * @param allowNegative Allow negative directions
     * @return null if valid size formatting is not found
     */
    public static Integer[] getSize(String[] args, int startIndex, boolean allowNegative) {
        if (startIndex >= args.length) return null;

        Integer[] size = new Integer[]{null, null, null, null};  // west, east, north, south

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i].toLowerCase();

            // check if only a number
            try {
                // valid, number specified
                int sizeParam = Integer.parseInt(arg);

                if (sizeParam < 0 && allowNegative) return null; // invalid size

                for (int j = 0; j < 4; j++) {
                    if (size[j] == null) size[j] = sizeParam;
                }

                continue;

            } catch (NumberFormatException e) {
            }

            // check if starting with S/N/E/W

            Set<Character> directions = new HashSet<>();
            int numStart = 0;
            for (char c : arg.toCharArray()) {
                if (c == 's' || c == 'n' || c == 'e' || c == 'w') {
                    numStart++;
                    directions.add(c);
                } else if (Character.isDigit(c) || c == '-') break;
                else return null; // character not a direction
            }

            if (numStart == 0) {
                return null; // must start with the direction
            }

            try {
                int sizeParam = Integer.parseInt(arg.substring(numStart));

                if (sizeParam < 0 && !allowNegative) return null; // invalid size

                // set a size for specific directions
                for (char c : directions) {
                    size["wens".indexOf(c)] = sizeParam;
                }

            } catch (NumberFormatException e) {
                return null; // begins with direction but still has invalid characters
            }
        }

        // don't allow null, check all
        for (int j = 0; j < 4; j++) {
            if (size[j] == null) size[j] = 0; // set nulls to 0
        }

        // no nulls found
        return size;
    }

    /**
     * Get an existing protection based on the arguments of a command. Use the player's name if none is specified.
     *
     * @param protections protection handler
     * @param sender      sender of command
     * @param args        command args
     * @return the protection. Null if invalid or unspecified.
     */
    public static Protection getExistingProtection(ProtectionHandler protections, CommandSender sender, String[] args) {
        if (args.length >= 2) {
            // test first arg
            Protection protection = protections.getProtection(args[1]);

            if (protection != null) return protection;
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        Protection protection = protections.getProtection(player.getName());

        if (protection == null) {
            if (args.length >= 2) {
                player.sendMessage(ChatColor.DARK_RED + "Unknown protection: " + args[1]);
            }
            return null;
        }

        return protection;
    }

    /**
     * Inform the sender what went wrong when updating their protection
     *
     * @param sender sender
     * @param e      exception
     */
    public static void handleUpdateException(CommandSender sender, InvalidProtectionException e) {
        sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
        switch (e.getReason()) {
            case PROTECTION_OVERLAPPING:
                sender.sendMessage(ChatColor.YELLOW + "Try making your expansion smaller.");
                break;
            case INVALID_BORDERS:
                sender.sendMessage(ChatColor.YELLOW + "The minimum diameter of a protection for either direction is " + ChatColor.RED + Protection.MIN_SIZE +
                        ChatColor.YELLOW + " blocks.");
                break;
        }

    }
}
