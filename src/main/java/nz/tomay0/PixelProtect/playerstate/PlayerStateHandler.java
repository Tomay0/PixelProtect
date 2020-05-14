package nz.tomay0.PixelProtect.playerstate;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

import static nz.tomay0.PixelProtect.playerstate.Confirmation.ConfirmationType.CREATE;
import static nz.tomay0.PixelProtect.playerstate.Confirmation.ConfirmationType.REMOVE;
import static nz.tomay0.PixelProtect.playerstate.Confirmation.ConfirmationType.UPDATE;

/**
 * Handle player state, confirmations and border displaying
 */
public class PlayerStateHandler implements Listener, Runnable {
    private ProtectionHandler protections;

    private Map<Player, PlayerState> playerStates = new HashMap<>();

    // console doesn't have playerstate, instead it has its own confirmation
    private Confirmation consoleConfirmation = null;

    /**
     * Handle player state
     *
     * @param protections
     */
    public PlayerStateHandler(ProtectionHandler protections) {
        this.protections = protections;

        // add all players currently on the server to the set (for reloading)
        for (Player player : Bukkit.getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    /**
     * Put a confirmation into the map, set as console confirmation if player is null
     *
     * @param player       player
     * @param confirmation confirmation
     */
    private void putConfirmation(Player player, Confirmation confirmation) {
        if (player != null) playerStates.get(player).setConfirmation(confirmation);
        else consoleConfirmation = confirmation;
    }

    /**
     * Remove a confirmation from the hashmap by the player.
     *
     * @param player player
     */
    private void removeConfirmation(Player player) {
        if (player == null) consoleConfirmation = null;
        else playerStates.get(player).setConfirmation(null);
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

        return playerStates.get(player).getConfirmation();
    }

    /**
     * Request to create a protection
     *
     * @param player     player requesting
     * @param protection protection
     * @param cost       cost
     */
    public void requestCreate(Player player, Protection protection, double cost) {
        Confirmation confirmation = new Confirmation(player, protection, CREATE, cost);

        putConfirmation(player, confirmation);
    }

    /**
     * Request to update a protection
     *
     * @param player    player making the request (null for console)
     * @param newBounds new bounds of the protection
     * @param cost      cost to update
     */
    public void requestUpdate(Player player, Protection newBounds, double cost) {
        Confirmation confirmation = new Confirmation(player, newBounds, UPDATE, cost);

        putConfirmation(player, confirmation);
    }

    /**
     * Request the removal of a protection
     *
     * @param player     player making the request (null for console)
     * @param protection protection
     * @param cost       cost to remove (should be negative)
     */
    public void requestRemove(Player player, Protection protection, double cost) {
        Confirmation confirmation = new Confirmation(player, protection, REMOVE, cost);

        putConfirmation(player, confirmation);
    }

    /**
     * When a player types /pr confirm
     *
     * @param player
     */
    public boolean confirm(Player player, Economy economy) {
        Confirmation confirmation = getConfirmation(player);
        if (confirmation == null) return false;

        confirmation.confirm(protections, economy);
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
     * Add player
     *
     * @param e
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        playerStates.put(player, new PlayerState(protections, player));
    }

    /**
     * Remove player
     *
     * @param e
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        playerStates.remove(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        playerStates.get(player).move();
    }

    /**
     * Runs every second, reminds players to use /pr confirm and shows border particles
     */
    public void run() {
        for (Player player : playerStates.keySet()) {
            PlayerState state = playerStates.get(player);

            if (state.getConfirmation() != null)
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/pr confirm" + ChatColor.YELLOW + " to confirm."));

            state.displayParticles();
        }

    }

    /**
     * Show borders of a protection
     *
     * @param player     player
     * @param protection protection
     */
    public void showBorders(Player player, Protection protection) {
        playerStates.get(player).setShowingBorder(protection);

        if (protection != null) {
            player.sendMessage(ChatColor.GREEN + "Showing the borders of " + ChatColor.GREEN + protection.getName());
            player.sendMessage(ChatColor.YELLOW + "Hide them by typing " + ChatColor.RED + "/pr hide");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Protection borders are now hidden.");
        }
    }
}
