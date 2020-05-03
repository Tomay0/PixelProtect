package nz.tomay0.PixelProtect.confirm;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nz.tomay0.PixelProtect.model.ProtectionBuilder;
import nz.tomay0.PixelProtect.model.ProtectionHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles protection creating/updating confirmations.
 * <p>
 * A confirmation will be deleted if a player types /pr reject or when they leave the server
 * <p>
 * Retyping a command will update the confirmation.
 * <p>
 * Typing /pr confirm will confirm the confirmation and update/create the protection
 */
public class ConfirmationHandler implements Listener, Runnable {

    /**
     * Player confirmations
     */
    private Map<Player, Confirmation> confirmations = new HashMap<>();

    private ProtectionHandler protections;

    /**
     * Create new confirmation handler
     *
     * @param protections protections
     */
    public ConfirmationHandler(ProtectionHandler protections) {
        this.protections = protections;
    }


    /**
     * Request to create a protection
     *
     * @param player player requesting
     * @param name   name of the protection
     * @param size   size of the protection
     */
    public void requestCreate(Player player, String name, Integer[] size) {
        Confirmation confirmation = new Confirmation(player, ProtectionBuilder.fromCommand(name, player, size, protections), false);

        confirmations.put(player, confirmation);
    }

    /**
     * When a player types /pr confirm
     *
     * @param player
     */
    public boolean confirm(Player player) {
        if (!confirmations.containsKey(player)) return false;

        confirmations.get(player).confirm(protections);
        confirmations.remove(player);

        return true;
    }

    /**
     * Cancel a confirmation
     *
     * @param player player
     * @return if confirmation existed
     */
    public boolean cancel(Player player) {
        if (!confirmations.containsKey(player)) return false;

        confirmations.remove(player);

        return true;
    }

    /**
     * Remove confirmations when players quit
     *
     * @param e event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (confirmations.containsKey(player)) {
            confirmations.remove(player);
        }
    }

    /**
     * Runs every second, reminds players to use /pr confirm and shows border particles
     */
    public void run() {
        for (Player player : confirmations.keySet()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/pr confirm" + ChatColor.YELLOW + " to confirm."));
        }

    }
}
