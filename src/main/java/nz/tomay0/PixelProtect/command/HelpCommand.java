package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
     * @param plugin plugin
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
    public String getDescription() {
        return "Show list of all protection commands.";
    }

    /**
     * Get number of pages
     *
     * @return
     */
    private int numPages() {
        return (int) (Math.ceil(commands.size() / (double) NUM_TO_SHOW));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        // work out page number and show help for the particular page
        if (args.length < 2) {
            showHelp(sender, 1);
        } else {
            String pageNumString = args[1];
            try {
                int pageNum = Integer.parseInt(pageNumString);

                if (pageNum < 1) showHelp(sender, 1);
                else if (pageNum > numPages()) showHelp(sender, numPages());
                else showHelp(sender, pageNum);

            } catch (NumberFormatException e) {
                showHelp(sender, 1);
            }
        }
    }

    /**
     * Show help for a particular page
     *
     * @param sender user to send to
     * @param page   page number
     */
    private void showHelp(CommandSender sender, int page) {
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "PixelProtect" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Help " + page + "/" + numPages());

        int i = (page - 1) * NUM_TO_SHOW;

        for (AbstractCommand command : commands) {
            i--;
            if (i >= 0) continue;
            if (i < -4) break;

            sender.sendMessage(ChatColor.RED + "/pr " + command.getCommand() + ChatColor.WHITE + ": " + ChatColor.LIGHT_PURPLE + command.getDescription());
        }

        if (page < numPages()) {
            sender.sendMessage(ChatColor.YELLOW + "Next page: " + ChatColor.RED + "/pr help " + (page + 1));
        }
    }
}
