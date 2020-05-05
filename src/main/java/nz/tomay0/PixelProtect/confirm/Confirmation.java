package nz.tomay0.PixelProtect.confirm;

import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * A confirmation to creating or updating the bounds of a protection
 */
public class Confirmation {
    /**
     * Player updating/creating
     */
    private Player player;

    /**
     * Temporary bounds of the protection
     */
    private Protection tempProtection;

    /**
     * If this protection already exists.
     */
    private boolean existing;

    /**
     * Locations to display particles
     */
    private Set<Location> particleLocations;

    /**
     * Create protection
     *
     * @param player         player
     * @param tempProtection temporary protection which only comes into affect once creating
     * @param exists         exists
     */
    public Confirmation(Player player, Protection tempProtection, boolean exists) {
        this.player = player;
        this.tempProtection = tempProtection;
        this.existing = exists;
    }

    /**
     * Confirm this confirmation
     */
    public void confirm(ProtectionHandler protections) {
        CommandSender sender = player == null ? Bukkit.getConsoleSender() : player;

        if (!existing) {
            // create new protection
            protections.addNewProtection(tempProtection);

            sender.sendMessage(ChatColor.YELLOW + "Created " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.RED + "/pr setperm " + tempProtection.getIdSafeName() + " <name> member" + ChatColor.LIGHT_PURPLE + " to add new members to your protection.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "You can see more commands you can use to manage your protection by typing " + ChatColor.RED + "/pr help");
        } else {
            // update bounds
            protections.updateBounds(tempProtection);
            sender.sendMessage(ChatColor.YELLOW + "Updated " + ChatColor.GREEN + tempProtection.getName() + ChatColor.YELLOW + " successfully!");
        }
    }


    /**
     * Display particles of the border to the player
     */
    public void displayParticles() {
        // create all locations (this is done in this function so it does not occur in test cases.
        if (particleLocations == null) {
            particleLocations = new HashSet<>();

            World world = Bukkit.getWorld(tempProtection.getWorld());

            // north side
            int x = tempProtection.getWest();
            int y = world.getMaxHeight();
            int z = tempProtection.getNorth();

            while (y > 0) {
                while (x < tempProtection.getEast() + 1) {
                    particleLocations.add(new Location(world, x, y, z));
                    x += 2;
                }

                y -= 2;
                x = tempProtection.getWest();
            }

            // east side
            x = tempProtection.getEast() + 1;
            y = world.getMaxHeight();
            z = tempProtection.getNorth();

            while (y > 0) {
                while (z < tempProtection.getSouth() + 1) {
                    particleLocations.add(new Location(world, x, y, z));
                    z += 2;
                }

                y -= 2;
                z = tempProtection.getNorth();
            }

            // south side
            x = tempProtection.getEast() + 1;
            y = world.getMaxHeight();
            z = tempProtection.getSouth() + 1;

            while (y > 0) {
                while (x > tempProtection.getWest()) {
                    particleLocations.add(new Location(world, x, y, z));
                    x -= 2;
                }

                y -= 2;
                x = tempProtection.getEast() + 1;
            }

            // west side
            x = tempProtection.getWest();
            y = world.getMaxHeight();
            z = tempProtection.getSouth() + 1;

            while (y > 0) {
                while (z > tempProtection.getNorth()) {
                    particleLocations.add(new Location(world, x, y, z));
                    z -= 2;
                }

                y -= 2;
                z = tempProtection.getSouth() + 1;
            }
        }

        for (Location location : particleLocations) {
            player.spawnParticle(Particle.CLOUD, location, 1, 0, 0, 0, 0);
        }
    }
}