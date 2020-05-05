package nz.tomay0.PixelProtect.command;

import nz.tomay0.PixelProtect.PixelProtectPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Handles the implementation of the /protect /pr command
 */
public class CommandHandler implements CommandExecutor {
    private List<AbstractCommand> commandsList;
    private Map<String, AbstractCommand> commandMap;
    private PixelProtectPlugin plugin;

    /**
     * Initialize the command list
     */
    public CommandHandler(PixelProtectPlugin plugin) {
        this.plugin = plugin;

        // create list - for help menu
        commandsList = new ArrayList<>();
        commandsList.add(new CreateCommand(plugin));
        commandsList.add(new ExpandCommand(plugin));
        commandsList.add(new ShiftCommand(plugin));
        commandsList.add(new MoveCommand(plugin));
        commandsList.add(new RemoveCommand(plugin));
        commandsList.add(new RenameCommand(plugin));
        commandsList.add(new ListCommand(plugin));

        commandsList.add(new HelpCommand(plugin, new ArrayList<>(commandsList))); // all commands after here are not contained in /pr help
        commandsList.add(new ConfirmCommand(plugin));
        commandsList.add(new CancelCommand(plugin));

        // create map - for usage
        commandMap = new HashMap<>();
        for (AbstractCommand command : commandsList) {
            commandMap.put(command.getCommand(), command);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("pr") && !label.equalsIgnoreCase("protect"))
            return false;

        // no arguments - show very basic help
        if (args.length == 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr create <name> <size>" + ChatColor.LIGHT_PURPLE + " to create a protection.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr help" + ChatColor.LIGHT_PURPLE + " for more commands.");
            return true;
        }

        String prCommand = args[0].toLowerCase();

        if (commandMap.containsKey(prCommand)) {
            commandMap.get(prCommand).onCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unknown command. Use " + ChatColor.RED + "/pr help " + ChatColor.LIGHT_PURPLE + "for a list of commands.");
        }
        return true;
    }
}