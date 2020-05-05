package nz.tomay0.PixelProtect.confirm;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
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
    private Confirmation consoleConfirmation = null;

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
     * Put a confirmation into the map, set as console confirmation if player is null
     *
     * @param player       player
     * @param confirmation confirmation
     */
    private void putConfirmation(Player player, Confirmation confirmation) {
        if (player != null) confirmations.put(player, confirmation);
        else consoleConfirmation = confirmation;
    }

    /**
     * Remove a confirmation from the hashmap by the player.
     *
     * @param player player
     */
    private void removeConfirmation(Player player) {
        if (player == null) consoleConfirmation = null;
        else confirmations.remove(player);
    }

    /**
     * Get current confirmation to confirm
     *
     * @param player player
     * @return confirmation
     */
    private Confirmation getConfirmation(Player player) {
        if (player == null)
            return consoleConfirmation;

        if (confirmations.containsKey(player))
            return confirmations.get(player);

        return null;
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

        putConfirmation(player, confirmation);
    }

    /**
     * Request to update a protection
     *
     * @param player    player making the request (null for console)
     * @param newBounds new bounds of the protection
     */
    public void requestUpdate(Player player, Protection newBounds) {
        Confirmation confirmation = new Confirmation(player, newBounds, true);

        putConfirmation(player, confirmation);
    }

    /**
     * When a player types /pr confirm
     *
     * @param player
     */
    public boolean confirm(Player player) {
        Confirmation confirmation = getConfirmation(player);
        if (confirmation == null) return false;

        confirmation.confirm(protections);
        removeConfirmation(player);

        return true;
    }

    /**
     * Cancel a confirmation
     *
     * @param player player
     * @return if confirmation existed
     */
    public boolean cancel(Player player) {
        if (getConfirmation(player) == null) return false;

        removeConfirmation(player);
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

        removeConfirmation(player);
    }

    /**
     * Runs every second, reminds players to use /pr confirm and shows border particles
     */
    public void run() {
        for (Player player : confirmations.keySet()) {

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/pr confirm" + ChatColor.YELLOW + " to confirm."));

            confirmations.get(player).displayParticles();
        }

    }
}
