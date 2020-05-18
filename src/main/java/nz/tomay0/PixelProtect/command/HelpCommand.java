package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Help command
 */
public class HelpCommand extends AbstractCommand {
    private static final int NUM_TO_SHOW = 4;

    private List<AbstractCommand> commands;

    /**
     * Help command
     *
     * @param plugin   plugin
     * @param commands list of commands to show in help
     */
    public HelpCommand(PixelProtectPlugin plugin, List<AbstractCommand> commands) {
        super(plugin);
        this.commands = commands;
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public boolean getConsole() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Show list of all protection commands.";
    }


    private List<AbstractCommand> getCommands(CommandSender sender) {

        List<AbstractCommand> commands = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;

            for (AbstractCommand command : this.commands) {
                if (command.getPermission() == null || player.hasPermission(command.getPermission())) {
                    commands.add(command);
                }
            }
        } else {
            for (AbstractCommand command : this.commands) {
                if (command.getConsole()) {
                    commands.add(command);
                }
            }
        }


        return commands;
    }

    /**
     * Get number of pages
     *
     * @return
     */
    private int numPages(int nCommands) {
        return (int) (Math.ceil(nCommands / (double) NUM_TO_SHOW));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        List<AbstractCommand> commands = getCommands(sender);

        // work out page number and show help for the particular page
        if (args.length < 2) {
            showHelp(sender, commands, 1);
        } else {
            String pageNumString = args[1];
            try {
                int pageNum = Integer.parseInt(pageNumString);

                if (pageNum < 1) showHelp(sender, commands, 1);
                else if (pageNum > numPages(commands.size())) showHelp(sender, commands, numPages(commands.size()));
                else showHelp(sender, commands, pageNum);

            } catch (NumberFormatException e) {
                showHelp(sender, commands, 1);
            }
        }
    }

    /**
     * Show help for a particular page
     *
     * @param sender user to send to
     * @param page   page number
     */
    private void showHelp(CommandSender sender, List<AbstractCommand> commands, int page) {
        sender.sendMessage(ChatColor.YELLOW + "For a more in-depth guide go to: " + ChatColor.AQUA + PluginConfig.getInstance().getHelpLink());

        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "PixelProtect" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Help " + page + "/" + numPages(commands.size()));

        int i = (page - 1) * NUM_TO_SHOW;

        for (AbstractCommand command : commands) {
            i--;
            if (i >= 0) continue;
            if (i < -4) break;

            sender.sendMessage(ChatColor.RED + "/pr " + command.getCommand() + ChatColor.WHITE + ": " + ChatColor.LIGHT_PURPLE + command.getDescription());
        }

        if (page < numPages(commands.size())) {
            sender.sendMessage(ChatColor.YELLOW + "Next page: " + ChatColor.RED + "/pr help " + (page + 1));
        }
    }
}
