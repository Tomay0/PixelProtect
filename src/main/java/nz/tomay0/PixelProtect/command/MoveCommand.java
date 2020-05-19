package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.AdminProtection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Move a protection
 */
public class MoveCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public MoveCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "move";
    }

    @Override
    public String getDescription() {
        return "Move your protection to where you are standing.";
    }

    @Override
    public boolean getConsole() {
        return false;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is a player only command.");
            return;
        }

        Player player = (Player) sender;

        Protection protection = CommandUtil.getExistingProtection(getProtections(), player, args, 3);
        if (protection == null) {
            commandHelp(player);
            return;
        }
        if (!getProtections().hasPermission(sender, protection, Perm.UPDATE)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to update to this protection: " + protection.getName());

            return;
        }
        int locationArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        if (args.length <= locationArg) {
            commandHelp(player);
            return;
        }

        String location = args[locationArg].toLowerCase();

        Location playerLocation = player.getLocation();
        int px = playerLocation.getBlockX();
        int pz = playerLocation.getBlockZ();

        World world = playerLocation.getWorld();
        int xlen = protection.getEast() - protection.getWest();
        int zlen = protection.getSouth() - protection.getNorth();
        int xmid = xlen / 2;
        int zmid = zlen / 2;

        int west, north;

        if (location.equals("centre") || location.equals("center") || location.equals("c")) {
            west = px - xmid;
            north = pz - zmid;
        } else if (location.equals("south") || location.equals("s")) {
            west = px - xmid;
            north = pz - zlen;
        } else if (location.equals("north") || location.equals("n")) {
            west = px - xmid;
            north = pz;
        } else if (location.equals("east") || location.equals("e")) {
            west = px - xlen;
            north = pz - zmid;
        } else if (location.equals("west") || location.equals("w")) {
            west = px;
            north = pz - zmid;
        } else if (location.equals("northwest") || location.equals("westnorth") || location.equals("nw") || location.equals("wn")) {
            west = px;
            north = pz;
        } else if (location.equals("southwest") || location.equals("westsouth") || location.equals("sw") || location.equals("ws")) {
            west = px;
            north = pz - zlen;
        } else if (location.equals("northeast") || location.equals("eastnorth") || location.equals("ne") || location.equals("en")) {
            west = px - xlen;
            north = pz;
        } else if (location.equals("southeast") || location.equals("eastsouth") || location.equals("se") || location.equals("es")) {
            west = px - xlen;
            north = pz - zlen;
        } else if (location.equals("relhome") || location.equals("home")) {

            if (protection.isAdminProtection()) {
                sender.sendMessage(ChatColor.DARK_RED + "Relhome is not available for admin protections.");
                return;
            }
            Location home = protection.getHome(Protection.DEFAULT_HOME);
            if (!player.getLocation().getWorld().getName().equals(home.getWorld().getName())) {
                player.sendMessage(ChatColor.DARK_RED + "You must be in the same world as the home to use /pr move relhome");
                return;
            }

            int dx = home.getBlockX() - protection.getWest();
            int dz = home.getBlockZ() - protection.getNorth();

            west = px - dx;
            north = pz - dz;
        } else {
            commandHelp(sender);
            return;
        }


        // new bounds are determined
        try {
            Protection newBounds = ProtectionBuilder.move(protection, north, west, world.getName(), getProtections());

            getPlayerStateHandler().requestUpdate(player, newBounds, 0);

            sender.sendMessage(ChatColor.YELLOW + "Updating the borders of " + ChatColor.GREEN + newBounds.getName());
            sender.sendMessage(ChatColor.YELLOW + "Confirm by typing " + ChatColor.AQUA + "/pr confirm");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr cancel" + ChatColor.LIGHT_PURPLE + " to cancel.");
        } catch (InvalidProtectionException e) {
            CommandUtil.handleUpdateException(sender, e);
        }
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr move <name> <location to move>");
        sender.sendMessage(ChatColor.YELLOW + "Location to move should be a corner, side of centre of the protection. Options:");
        sender.sendMessage("centre, north, south, east, west, ne, se, nw, ne, relhome");
    }
}
