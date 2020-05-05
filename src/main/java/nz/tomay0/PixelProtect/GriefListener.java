package nz.tomay0.PixelProtect;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.perms.Perm;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener that checks for all types of griefs within a protection
 */
public class GriefListener implements Listener {
    private ProtectionHandler protections;

    private static final Set<Material> bannedHolding = getBannedHoldingRightClick();
    private static final Set<Material> bannedInteract = getBannedInteract();
    private static final Set<Material> bannedChests = getBannedChests();

    private static Set<Material> getBannedHoldingRightClick() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.FLINT_AND_STEEL, Material.END_CRYSTAL, Material.ITEM_FRAME,
                Material.PAINTING, Material.BONE_MEAL, Material.LEAD, Material.SHEARS));

        for (Material material : Material.values()) {
            if (material.toString().endsWith("BOAT") || material.toString().endsWith("MINECART") || material.toString().endsWith("SPAWN_EGG") ||
                    material.toString().endsWith("SHOVEL") || material.toString().endsWith("AXE")) {
                materials.add(material);
            }
        }

        return materials;
    }

    private static Set<Material> getBannedInteract() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.ENCHANTING_TABLE, Material.LOOM, Material.JUKEBOX, Material.CRAFTING_TABLE, Material.STONECUTTER,
                Material.END_PORTAL_FRAME, Material.BEEHIVE, Material.BEE_NEST, Material.BELL, Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.REPEATER, Material.COMPARATOR,
                Material.LEVER, Material.DAYLIGHT_DETECTOR, Material.CAULDRON, Material.SWEET_BERRY_BUSH, Material.COMPOSTER, Material.DRAGON_EGG, Material.BEACON, Material.LECTERN));


        //anvil bed door button gate trapdoor
        for (Material material : Material.values()) {
            if (material.toString().endsWith("ANVIL") || material.toString().endsWith("BED") || material.toString().endsWith("DOOR") || material.toString().endsWith("BUTTON")
                    || material.toString().endsWith("GATE") || material.toString().endsWith("TRAPDOOR")) {
                materials.add(material);
            }
        }

        return materials;
    }

    private static Set<Material> getBannedChests() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.CHEST, Material.ENDER_CHEST, Material.BARREL, Material.HOPPER, Material.FURNACE,
                Material.BLAST_FURNACE, Material.CAMPFIRE, Material.BREWING_STAND, Material.DISPENSER, Material.DROPPER, Material.TRAPPED_CHEST, Material.SMOKER));

        for(Material material : Material.values()) {
            if(material.toString().endsWith("SHULKER_BOX")) {
                materials.add(material);
            }
        }
        return materials;

    }


    public GriefListener(ProtectionHandler protections) {
        this.protections = protections;
    }


    /*

    TODO firespread

    interact - detect place/break before?

    pistons

    sand cannons?

    enderman

    silverfish

    crops

    lightning strike trident

    zombie break door

    block explode? Beds?

    pickup blocks on the ground

    Entity minecarts can be destroyed? itemframes too

    withers destroying blocks


     */

    /**
     * Send different messages depending on what the permission type is
     *
     * @param player
     * @param perm
     */
    private void sendPlayerMessage(Player player, Perm perm) {
        String message = null;

        switch (perm) {
            case BUILD:
                message = "You do not have permission to build here.";
                break;
            case INTERACT:
                message = "You do not have permission to interact here.";
                break;
            case CHEST:
                message = "You do not have permission to open chests here.";
                break;
            case KILL_FRIENDLY:
                message = "You do not have permission to hurt friendly mobs here.";
                break;
            case KILL_HOSTILE:
                message = "You do not have permission to hurt hostile mobs here.";
                break;
        }

        if (message != null)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + message));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Location location = e.getRightClicked().getLocation();

        if (!protections.hasPermission(player, location, Perm.INTERACT)) {
            sendPlayerMessage(player, Perm.INTERACT);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Location location = e.getRightClicked().getLocation();

        if (!protections.hasPermission(player, location, Perm.INTERACT)) {
            sendPlayerMessage(player, Perm.INTERACT);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) return;

        Block block = e.getClickedBlock();

        if (e.getAction() == Action.PHYSICAL) {
            // TODO allow for more strict access that doesn't allow pressure plates, redstone ore or tripwires.

            // when you "step on" blocks
            if (block.getType().equals(Material.TURTLE_EGG) || block.getType().equals(Material.FARMLAND)
                /* || block.getType().toString().endsWith("PRESSURE_PLATE") || block.getType().equals(Material.REDSTONE_ORE) || block.getType().equals(Material.TRIPWIRE)*/) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.INTERACT)) {
                    sendPlayerMessage(player, Perm.INTERACT);
                    e.setCancelled(true);
                }
            }
        }
        //TODO FIRE DOES NOT WORK

        else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            // putting out fire counts

            Block relative = block.getRelative(e.getBlockFace());
            if (relative != null && relative.getType() == Material.FIRE) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                    sendPlayerMessage(player, Perm.BUILD);
                    e.setCancelled(true);
                }
            }

        } else {

            // items you can place without causing block place
            ItemStack mainItem = player.getInventory().getItemInMainHand();
            ItemStack sideItem = player.getInventory().getItemInOffHand();

            if (mainItem != null) {
                if (bannedHolding.contains(mainItem.getType())) {
                    if (!protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                        sendPlayerMessage(player, Perm.BUILD);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            if (sideItem != null) {
                if (bannedHolding.contains(sideItem.getType())) {
                    if (!protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                        sendPlayerMessage(player, Perm.BUILD);
                        e.setCancelled(true);
                        return;
                    }
                }
            }

            // items you can't interact with
            if(bannedChests.contains(block.getType())) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.CHEST)) {
                    sendPlayerMessage(player, Perm.CHEST);
                    e.setCancelled(true);
                    return;
                }
            }
            if(bannedInteract.contains(block.getType())) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.INTERACT)) {
                    sendPlayerMessage(player, Perm.INTERACT);
                    e.setCancelled(true);
                    return;
                }
            }

        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Protection protection = protections.getProtectionAt(e.getLocation());

        for (Block block : new ArrayList<>(e.blockList())) {
            Location location = block.getLocation();

            if (protection != null && protection.withinBounds(location)) {
                e.blockList().remove(block);
            } else {
                protection = protections.getProtectionAt(block.getLocation());
                if (protection != null) {
                    e.blockList().remove(block);
                }
            }
        }
    }

    //TODO armor stand, item frame picture frame

    // TODO wither block damage, enderman block damage

    // TODO farmland

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        if (damager instanceof Player) {
            Player player = (Player) damager;

            // TODO distinguish between hostile and friendly

            if (!protections.hasPermission(player, entity.getLocation(), Perm.KILL_HOSTILE)) {
                sendPlayerMessage(player, Perm.KILL_HOSTILE);
                e.setCancelled(true);
            }
        } else {
            //TODO what entities should be able to kill other types of entities?

            //TODO hostile shouldnt kill friendly IMO
            if (protections.getProtectionAt(entity.getLocation()) != null) {
                e.setCancelled(true);
            }
        }
    }
}
