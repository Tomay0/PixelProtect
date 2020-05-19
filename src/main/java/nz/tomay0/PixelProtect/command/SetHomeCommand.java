package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Set a protections home
 */
public class SetHomeCommand extends AbstractCommand {
    /**
     * Create new abstract command with a protection handler
     *
     * @param plugin plugin
     */
    public SetHomeCommand(PixelProtectPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "sethome";
    }

    @Override
    public String getDescription() {
        return "Set a protection's home.";
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
        if (protection.isAdminProtection()) {
            sender.sendMessage(ChatColor.DARK_RED + "This command is not avaliable for admin protections.");
            return;
        }

        // check if they have permission
        if (!getProtections().hasPermission(sender, protection, Perm.SETHOME)) {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to set this protection's home: " + protection.getName());
            return;
        }
        int homeArg = (args.length >= 2 && getProtections().isProtection(args[1], protection)) ? 2 : 1;

        String home = Protection.DEFAULT_HOME;

        if (args.length > homeArg) {
            home = args[homeArg];
        }

        if (PluginConfig.getInstance().getDisabledWorlds().contains(player.getLocation().getWorld().getName())) {
            player.sendMessage(ChatColor.DARK_RED + "You are not allowed to set protection homes in this world.");
            return;
        }

        // if a new home, check if allowed to set multiple
        if (protection.getHome(home) == null) {
            int homeCount = protection.getHomeCount();
            int totalMaxHomes = PluginConfig.getInstance().getMaxHomes();
            int blocksPerHome = PluginConfig.getInstance().getBlocksPerHome();

            if (totalMaxHomes != -1 && homeCount >= totalMaxHomes) {
                sender.sendMessage(ChatColor.RED + "You are not allowed to set more than " + PluginConfig.getInstance().getMaxHomes() + " homes.");
                return;
            } else if (blocksPerHome > 0) {
                int requiredArea = blocksPerHome * homeCount;
                if (protection.getArea() < requiredArea) {
                    sender.sendMessage(ChatColor.RED + "Your protection is not big enough to set another home. You need to claim another " + (requiredArea - protection.getArea()) + " blocks.");
                    return;
                }
            }
        }

        protection.setHome(home, player.getLocation());

        sender.sendMessage(ChatColor.YELLOW + "Set the home: " + ChatColor.GREEN + home + ChatColor.YELLOW + " of " + ChatColor.GREEN + protection.getName());
    }

    /**
     * Tell the sender how to use the command
     */
    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/pr sethome <name> <home name>");
        sender.sendMessage(ChatColor.YELLOW + "Leave out the home name to set to the default home.");
    }
}
