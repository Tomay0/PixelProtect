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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener that checks for all types of griefs within a protection
 */
public class GriefListener implements Listener {

    /*

    Distinguish different types of blocks and entities

     */


    // hostile mobs
    private static final Set<EntityType> hostileMobs = new HashSet<>(Arrays.asList(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
            EntityType.DROWNED, EntityType.ENDERMAN, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMITE, EntityType.SILVERFISH,
            EntityType.EVOKER, EntityType.GHAST, EntityType.GIANT, EntityType.GUARDIAN, EntityType.HUSK, EntityType.ILLUSIONER, EntityType.MAGMA_CUBE, EntityType.PHANTOM,
            EntityType.PIG_ZOMBIE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SHULKER, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER,
            EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER));

    // friendly mobs
    private static final Set<EntityType> friendlyMobs = new HashSet<>(Arrays.asList(EntityType.BAT, EntityType.BEE, EntityType.CAT, EntityType.CHICKEN,
            EntityType.COD, EntityType.COW, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.FOX, EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.LLAMA,
            EntityType.MULE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA, EntityType.PARROT, EntityType.PIG, EntityType.PUFFERFISH, EntityType.RABBIT,
            EntityType.SALMON, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.SNOWMAN, EntityType.SQUID, EntityType.TRADER_LLAMA, EntityType.TROPICAL_FISH,
            EntityType.TURTLE, EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.WOLF, EntityType.ZOMBIE_HORSE, EntityType.POLAR_BEAR));

    // entities that are like blocks
    private static final Set<EntityType> buildEntities = new HashSet<>(Arrays.asList(EntityType.ARMOR_STAND, EntityType.BOAT, EntityType.ENDER_CRYSTAL, EntityType.MINECART,
            EntityType.MINECART_CHEST, EntityType.MINECART_COMMAND, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER, EntityType.MINECART_TNT, EntityType.PAINTING, EntityType.LEASH_HITCH, EntityType.ITEM_FRAME));

    // items you can't right click while holding
    private static final Set<Material> bannedHolding = getBannedHoldingRightClick();

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

    // blocks you can't right click
    private static final Set<Material> bannedInteract = getBannedInteract();

    private static Set<Material> getBannedInteract() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.ENCHANTING_TABLE, Material.LOOM, Material.JUKEBOX, Material.CRAFTING_TABLE, Material.STONECUTTER,
                Material.END_PORTAL_FRAME, Material.BEEHIVE, Material.BEE_NEST, Material.BELL, Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.REPEATER, Material.COMPARATOR,
                Material.LEVER, Material.DAYLIGHT_DETECTOR, Material.CAULDRON, Material.SWEET_BERRY_BUSH, Material.COMPOSTER, Material.DRAGON_EGG,
                Material.BEACON, Material.LECTERN, Material.NOTE_BLOCK, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.REPEATING_COMMAND_BLOCK,
                Material.CAKE));


        //anvil bed door button gate trapdoor
        for (Material material : Material.values()) {
            if (material.toString().endsWith("ANVIL") || material.toString().endsWith("BED") || material.toString().endsWith("DOOR") || material.toString().endsWith("BUTTON")
                    || material.toString().endsWith("GATE") || material.toString().endsWith("TRAPDOOR")) {
                materials.add(material);
            }
        }

        return materials;
    }

    // blocks that can store items, therefore you can't access without chest perms
    private static final Set<Material> bannedChests = getBannedChests();

    private static Set<Material> getBannedChests() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.CHEST, Material.ENDER_CHEST, Material.BARREL, Material.HOPPER, Material.FURNACE,
                Material.BLAST_FURNACE, Material.CAMPFIRE, Material.BREWING_STAND, Material.DISPENSER, Material.DROPPER, Material.TRAPPED_CHEST, Material.SMOKER));

        for (Material material : Material.values()) {
            if (material.toString().endsWith("SHULKER_BOX")) {
                materials.add(material);
            }
        }
        return materials;

    }

    // blocks that "build" when you right click - flower pots
    private static final Set<Material> bannedBuildInteract = getBannedBuildInteract();

    private static Set<Material> getBannedBuildInteract() {
        Set<Material> materials = new HashSet<>();
        materials.add(Material.FLOWER_POT);

        for (Material m : Material.values()) {
            if (m.toString().startsWith("POTTED")) {
                materials.add(m);
            }
        }
        return materials;
    }

    /**
     * All protections
     */
    private ProtectionHandler protections;


    /**
     * Init grief listener
     *
     * @param protections
     */
    public GriefListener(ProtectionHandler protections) {
        this.protections = protections;
    }


    /*

    TODO

    firespread

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

    //TODO armor stand, item frame picture frame

    // TODO wither block damage, enderman block damage

    // TODO farmland

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

    /**
     * Place block
     *
     * @param e
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    /**
     * Break block
     *
     * @param e
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    /**
     * Right click entity
     *
     * @param e
     */
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Location location = e.getRightClicked().getLocation();

        if (!protections.hasPermission(player, location, Perm.INTERACT)) {
            sendPlayerMessage(player, Perm.INTERACT);
            e.setCancelled(true);
        }
    }

    /**
     * Right click an entity at a specific place (armour stands)
     *
     * @param e
     */
    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Location location = e.getRightClicked().getLocation();

        if (!protections.hasPermission(player, location, Perm.INTERACT)) {
            sendPlayerMessage(player, Perm.INTERACT);
            e.setCancelled(true);
        }
    }

    /**
     * Interact
     *
     * @param e
     */
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
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            // avoid interacting with noteblocks
            if (block.getType().equals(Material.NOTE_BLOCK)) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.INTERACT)) {
                    sendPlayerMessage(player, Perm.INTERACT);
                    e.setCancelled(true);
                }

            }
            // avoid interacting with the dragon egg
            else if (block.getType().equals(Material.DRAGON_EGG)) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                    sendPlayerMessage(player, Perm.BUILD);
                    e.setCancelled(true);
                }
            }
            // detect fire
            else {
                BlockFace face = e.getBlockFace();
                Block b = block.getRelative(face);

                if (b.getType() == Material.FIRE) {
                    if (!protections.hasPermission(player, b.getLocation(), Perm.BUILD)) {
                        sendPlayerMessage(player, Perm.BUILD);
                        e.setCancelled(true);
                    }

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

            // flower pots mostly
            if (bannedBuildInteract.contains(block.getType())) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.CHEST)) {
                    sendPlayerMessage(player, Perm.CHEST);
                    e.setCancelled(true);
                    return;
                }

            }

            // items you can't interact with
            else if (bannedChests.contains(block.getType())) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.CHEST)) {
                    sendPlayerMessage(player, Perm.CHEST);
                    e.setCancelled(true);
                    return;
                }
            } else if (bannedInteract.contains(block.getType())) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.INTERACT)) {
                    sendPlayerMessage(player, Perm.INTERACT);
                    e.setCancelled(true);
                    return;
                }
            }

        }
    }

    /**
     * Break hanging entities like paintings and itemframes
     * @param e
     */
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Entity remover = e.getRemover();
        Entity entity = e.getEntity();

        if (remover instanceof Player) {
            Player player = (Player) remover;

            if (!protections.hasPermission(player, entity.getLocation(), Perm.BUILD)) {
                sendPlayerMessage(player, Perm.BUILD);
                e.setCancelled(true);
            }
        }
        else{
            // TODO allow hanging items to get killed by other entities?
            if (protections.getProtectionAt(entity.getLocation()) != null) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Break minecarts/boats
     * @param e
     */
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        Entity remover = e.getAttacker();
        Entity entity = e.getVehicle();

        if (remover instanceof Player) {
            Player player = (Player) remover;

            if (!protections.hasPermission(player, entity.getLocation(), Perm.BUILD)) {
                sendPlayerMessage(player, Perm.BUILD);
                e.setCancelled(true);
            }
        }
        else{
            // TODO allow vehicles items to get killed by other entities?
            if (protections.getProtectionAt(entity.getLocation()) != null) {
                e.setCancelled(true);
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

    /**
     * When an entity damages another entity
     *
     * @param e
     */
    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        if (damager instanceof Player) {
            Player player = (Player) damager;

            EntityType type = entity.getType();

            Perm perm = null;
            if (friendlyMobs.contains(type)) {
                perm = Perm.KILL_FRIENDLY;
            } else if (hostileMobs.contains(type)) {
                perm = Perm.KILL_HOSTILE;
            } else if (buildEntities.contains(type)) {
                perm = Perm.BUILD;
            }

            if (perm == null) return;  // hurting projectile weapons, other players doesn't matter

            if (!protections.hasPermission(player, entity.getLocation(), perm)) {
                sendPlayerMessage(player, perm);
                e.setCancelled(true);
            }
        } else {
            // TODO make this configurable? hostile mobs can't damage friendly mobs
            if (friendlyMobs.contains(entity.getType()) && hostileMobs.contains(damager.getType())) {
                if (protections.getProtectionAt(entity.getLocation()) != null) {
                    e.setCancelled(true);
                }
            }

        }
    }
}
