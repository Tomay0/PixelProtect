package nz.tomay0.PixelProtect.playerstate;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles temporary values assigned to players.
 * For example, the protection the player is currently in
 */
public class PlayerState {
    private static final int MAX_PARTICLE_DISTANCE = 80;

    private ProtectionHandler protections;

    private Player player;
    private Protection currentProtection;

    private Set<Location> borderParticles = null;

    private Confirmation confirmation = null;

    private Location teleportLocation = null;

    private int teleportCountDown = 0;

    /**
     * Create player state
     *
     * @param protections protection handler
     * @param player      player
     */
    public PlayerState(ProtectionHandler protections, Player player) {
        this.protections = protections;
        this.player = player;
        move(null);
    }

    /**
     * Set the player confirmation
     *
     * @param confirmation
     */
    public void setConfirmation(Confirmation confirmation) {
        this.confirmation = confirmation;


        if (confirmation == null) {
            move(null);
            setShowingBorder(null);
        } else {
            setShowingBorder(confirmation.getProtection());
            currentProtection = null;
        }
    }

    public void requestTeleport(Location location) {
        teleportLocation = location;
        teleportCountDown = 3;
    }

    /**
     * Update the protection whose borders are being shown
     *
     * @param protection protection to show
     */
    public void setShowingBorder(Protection protection) {
        if (protection == null) {
            borderParticles = null;
            return;
        }

        borderParticles = new HashSet<>();

        World world = Bukkit.getWorld(protection.getWorld());

        // north side
        int x = protection.getWest();
        int y = world.getMaxHeight();
        int z = protection.getNorth();

        while (y > 0) {
            while (x < protection.getEast() + 1) {
                borderParticles.add(new Location(world, x, y, z));
                x += 2;
            }

            y -= 2;
            x = protection.getWest();
        }

        // east side
        x = protection.getEast() + 1;
        y = world.getMaxHeight();
        z = protection.getNorth();

        while (y > 0) {
            while (z < protection.getSouth() + 1) {
                borderParticles.add(new Location(world, x, y, z));
                z += 2;
            }

            y -= 2;
            z = protection.getNorth();
        }

        // south side
        x = protection.getEast() + 1;
        y = world.getMaxHeight();
        z = protection.getSouth() + 1;

        while (y > 0) {
            while (x > protection.getWest()) {
                borderParticles.add(new Location(world, x, y, z));
                x -= 2;
            }

            y -= 2;
            x = protection.getEast() + 1;
        }

        // west side
        x = protection.getWest();
        y = world.getMaxHeight();
        z = protection.getSouth() + 1;

        while (y > 0) {
            while (z > protection.getNorth()) {
                borderParticles.add(new Location(world, x, y, z));
                z -= 2;
            }

            y -= 2;
            z = protection.getSouth() + 1;
        }
    }

    /**
     * Update the protection to where you are standing
     *
     * @param e
     */
    public void move(PlayerMoveEvent e) {
        // teleport countdown
        if (teleportLocation != null && e != null) {
            double dx = e.getFrom().getX() - e.getTo().getX();
            double dy = e.getFrom().getY() - e.getTo().getY();
            double dz = e.getFrom().getZ() - e.getTo().getZ();

            if (dx != 0 || dy != 0 || dz != 0) {
                player.sendMessage(ChatColor.RED + "Teleportation cancelled.");
                teleportLocation = null;
                teleportCountDown = 0;
            }
        }

        // tell the user what protection they are in
        if (confirmation != null) return;

        Protection protection = protections.getMainProtectionAt(player.getLocation());

        if (protection != currentProtection) {
            if (protection == null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Entered the wilderness"));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(protection.getMotd()));
            }
        }
        currentProtection = protection;
    }

    /**
     * Get the confirmation if it exists
     *
     * @return confirmation
     */
    public Confirmation getConfirmation() {
        return confirmation;
    }

    /**
     * Teleport countdown
     */
    public void teleportCountDown() {
        if (teleportCountDown > 0) {
            teleportCountDown--;
        }

        if (teleportCountDown == 0 && teleportLocation != null) {
            player.teleport(teleportLocation);
            teleportLocation = null;
        }
    }

    /**
     * Display border particles
     */
    public void displayParticles() {
        // create all locations (this is done in this function so it does not occur in test cases.
        if (borderParticles == null) return;

        for (Location location : borderParticles) {
            double distance = player.getLocation().distance(location);
            if (distance < MAX_PARTICLE_DISTANCE)
                player.spawnParticle(Particle.CLOUD, location, 1, 0, 0, 0, 0);
        }
    }
}
