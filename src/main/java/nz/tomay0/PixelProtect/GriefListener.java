package nz.tomay0.PixelProtect;

import java.util.*;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;


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
            EntityType.MINECART_MOB_SPAWNER, EntityType.MINECART_TNT, EntityType.LEASH_HITCH));

    // items you can't right click while holding
    private static final Set<Material> bannedHolding = getBannedHoldingRightClick();

    private static Set<Material> getBannedHoldingRightClick() {
        Set<Material> materials = new HashSet<>(Arrays.asList(Material.FLINT_AND_STEEL, Material.END_CRYSTAL, Material.ITEM_FRAME,
                Material.PAINTING, Material.LEAD, Material.SHEARS));

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
    private PixelProtectPlugin plugin;


    /**
     * Init grief listener
     *
     * @param plugin
     */
    public GriefListener(PixelProtectPlugin plugin) {
        this.protections = plugin.getProtections();
        this.plugin = plugin;
    }


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
     * Multi place block (eg bed)
     *
     * @param e
     */
    @EventHandler
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        Player player = e.getPlayer();

        for (BlockState b : e.getReplacedBlockStates()) {
            if (!protections.hasPermission(player, b.getLocation(), Perm.BUILD)) {
                sendPlayerMessage(player, Perm.BUILD);
                e.setCancelled(true);
                return;
            }
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
     * Place hanging
     *
     * @param e
     */
    @EventHandler
    public void onPlaceHanging(HangingPlaceEvent e) {
        Player player = e.getPlayer();
        Location location = e.getEntity().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    /**
     * Place water
     *
     * @param e
     */
    @EventHandler
    public void onWaterPlace(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (!protections.hasPermission(player, location, Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
        }
    }

    /**
     * Pickup water
     *
     * @param e
     */
    @EventHandler
    public void onWaterPickup(PlayerBucketFillEvent e) {
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
     * is considered a chest
     *
     * @param e
     */
    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Location location = e.getRightClicked().getLocation();

        if (!protections.hasPermission(player, location, Perm.CHEST)) {
            sendPlayerMessage(player, Perm.CHEST);
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

            // when you "step on" blocks
            if (block.getType().equals(Material.TURTLE_EGG) || (protections.getFlagAt(block.getLocation(), Flag.PRESSURE_PLATE_PROTECTION) &&
                    (block.getType().toString().endsWith("PRESSURE_PLATE") || block.getType().equals(Material.REDSTONE_ORE) || block.getType().equals(Material.TRIPWIRE)))) {
                if (!protections.hasPermission(player, block.getLocation(), Perm.INTERACT)) {
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
                if (!protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                    sendPlayerMessage(player, Perm.BUILD);
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
     * Lightning strike, add some metadata so the cause cam be determined
     *
     * @param e
     */
    /*@EventHandler
    public void onLightningStrike(LightningStrikeEvent e) {
        e.getLightning().setMetadata("LIGHTNING_CAUSE", new FixedMetadataValue(plugin, e.getCause()));
    }*/

    /**
     * When projectiles are launched from dispensers, notate the source in the metadata
     *
     * @param e
     */
    @EventHandler
    public void onDispense(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();

        ProjectileSource source = projectile.getShooter();

        if (!(source instanceof Entity)) {
            Location location = projectile.getLocation();

            Vector velocity = projectile.getVelocity();

            location = location.subtract(velocity);


            projectile.setMetadata("PROJECTILE_SOURCE", new FixedMetadataValue(plugin, location));
        }
    }

    /**
     * Check the location of a projectile source. (Eg: dispenser)
     * <p>
     * If hitLocation is inside, but the source is outside. return true to indicate that the source is outside
     *
     * @param projectile
     * @return
     */
    private boolean checkProjectileOutsideProtection(Projectile projectile, Location hitLocation) {
        if (!projectile.hasMetadata("PROJECTILE_SOURCE")) return false;

        Object metadata = projectile.getMetadata("PROJECTILE_SOURCE").get(0).value();

        if (!(metadata instanceof Location)) return false;

        Location sourceLoc = (Location) metadata;

        Protection p1 = protections.getProtectionAt(hitLocation);

        if (p1 == null || !p1.getFlag(Flag.ENTITY_DAMAGE_ENTITY)) return false; // ignore if hit an unprotected area

        Protection p2 = protections.getProtectionAt(sourceLoc);

        return p1 != p2;
    }


    /**
     * Break hanging entities like paintings and itemframes
     *
     * @param e
     */
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Entity remover = e.getRemover();
        Entity entity = e.getEntity();

        if (remover instanceof Projectile) {
            ProjectileSource source = ((Projectile) remover).getShooter();

            if (source instanceof Entity) remover = (Entity) source;
            else if (checkProjectileOutsideProtection(((Projectile) remover), entity.getLocation())) {
                e.setCancelled(true);
                return;
            } else return;
        }

        if (remover instanceof Player) {
            Player player = (Player) remover;

            if (!protections.hasPermission(player, entity.getLocation(), Perm.BUILD)) {
                sendPlayerMessage(player, Perm.BUILD);
                e.setCancelled(true);
            }
        } else {
            if (!protections.getFlagAt(entity.getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Break minecarts/boats
     *
     * @param e
     */
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        Entity remover = e.getAttacker();
        Entity entity = e.getVehicle();

        if (remover instanceof Projectile) {
            ProjectileSource source = ((Projectile) remover).getShooter();

            if (source instanceof Entity) remover = (Entity) source;
            else if (checkProjectileOutsideProtection(((Projectile) remover), entity.getLocation())) {
                e.setCancelled(true);
                return;
            } else return;
        }

        if (remover instanceof Player) {
            Player player = (Player) remover;

            if (!protections.hasPermission(player, entity.getLocation(), Perm.BUILD)) {
                sendPlayerMessage(player, Perm.BUILD);
                e.setCancelled(true);
            }
        } else {
            if (!protections.getFlagAt(entity.getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
                e.setCancelled(true);
            }
        }

    }

    /**
     * Potion splash
     *
     * @param e
     */
    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getAffectedEntities().size() == 0) return; // ignore for 0 entities.
        // potion splash: witch, player, dispenser.
        // similar to checking entity damage

        ThrownPotion potion = e.getPotion();

        ProjectileSource source = potion.getShooter();

        if (!(source instanceof Entity)) {
            // dispenser or something
            if (!potion.hasMetadata("PROJECTILE_SOURCE")) return;

            Object metadata = potion.getMetadata("PROJECTILE_SOURCE").get(0).value();

            if (!(metadata instanceof Location)) return;

            Protection sourcePr = protections.getProtectionAt((Location) metadata);

            for (LivingEntity entity : e.getAffectedEntities()) {
                if (sourcePr != null && sourcePr.withinBounds(entity.getLocation())) continue; // same protection

                // different protections
                Protection destPr = protections.getProtectionAt(entity.getLocation());

                if (destPr != null)
                    e.setIntensity(entity, 0); // potion came from another protection or outside
            }

            return;
        }

        Entity shooter = (Entity) source;

        // shooter is a player, consider if the player has perms
        if (shooter instanceof Player) {
            Player player = (Player) shooter;

            for (LivingEntity entity : e.getAffectedEntities()) {
                EntityType type = entity.getType();

                Perm perm;
                if (type == EntityType.PLAYER) {
                    if (!protections.getFlagAt(entity.getLocation(), Flag.PVP)) {
                        e.setIntensity(entity, 0);
                    }
                    return;
                } else if (friendlyMobs.contains(type)) {
                    perm = Perm.KILL_FRIENDLY;
                } else if (hostileMobs.contains(type)) {
                    perm = Perm.KILL_HOSTILE;
                } else return;

                if (!protections.hasPermission(player, entity.getLocation(), perm)) {
                    e.setIntensity(entity, 0);
                }
            }


            return;
        }

        // shooter is another entity - eg witch, cancel if the enemy is not a player

        for (LivingEntity entity : e.getAffectedEntities()) {
            if (entity instanceof Player) continue;

            if (!protections.getFlagAt(entity.getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
                e.setIntensity(entity, 0);
            }
        }
    }

    /**
     * Block form - eg: frost walker, snowman snow
     *
     * @param e
     */
    @EventHandler
    public void onBlockForm(EntityBlockFormEvent e) {
        Entity entity = e.getEntity();
        Block b = e.getBlock();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (!protections.hasPermission(player, b.getLocation(), Perm.BUILD)) {
                e.setCancelled(true);
            }
        } else {

            if (!protections.getFlagAt(entity.getLocation(), Flag.MOB_GRIEFING)) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Blocks that break when you hit them with projectiles. (Chorus flower)
     *
     * @param e
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getHitBlock() == null || e.getHitBlock().getType() != Material.CHORUS_FLOWER) return;

        Projectile projectile = e.getEntity();
        ProjectileSource source = projectile.getShooter();
        Location location = e.getHitBlock().getLocation();
        if (source instanceof Player) {
            Player player = (Player) source;
            if (protections.hasPermission(player, location, Perm.BUILD)) return;

            sendPlayerMessage(player, Perm.BUILD);
        } else if (source instanceof Entity) {
            if (!protections.getFlagAt(location, Flag.MOB_GRIEFING)) return;
        } else if (!checkProjectileOutsideProtection(projectile, location)) return;

        e.getHitBlock().setType(Material.AIR);

        projectile.remove();

        // cancel the event
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            e.getHitBlock().setType(Material.CHORUS_FLOWER);
        });

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

        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();

            if (source instanceof Entity) damager = (Entity) source;
            else if (checkProjectileOutsideProtection(((Projectile) damager), entity.getLocation())) {
                e.setCancelled(true);
                return;
            } else return;
        }

        if (damager instanceof Player) {
            Player player = (Player) damager;

            EntityType type = entity.getType();

            Perm perm = null;
            if (type == EntityType.PLAYER) {
                // PVP, cancel if disabled in this protection
                if (!protections.getFlagAt(entity.getLocation(), Flag.PVP)) {
                    e.setCancelled(true);

                }
                return;
            } else if (friendlyMobs.contains(type)) {
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
        }
        // cancel damage from lightning
        else if (damager instanceof LightningStrike) {
            if (!protections.getFlagAt(entity.getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
                e.setCancelled(true);
            }
        } else {
            if (friendlyMobs.contains(entity.getType()) && hostileMobs.contains(damager.getType())) {
                if (!protections.getFlagAt(entity.getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
                    e.setCancelled(true);
                }
            }

        }
    }


    /**
     * Entity explosions
     *
     * @param e
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            Location location = block.getLocation();

            if (!protections.getFlagAt(location, Flag.EXPLOSION_DAMAGE)) {
                e.blockList().remove(block);
            }
        }

    }


    /**
     * Block explosions
     *
     * @param e
     */
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            Location location = block.getLocation();

            if (!protections.getFlagAt(location, Flag.EXPLOSION_DAMAGE)) {
                e.blockList().remove(block);
            }
        }

    }

    /**
     * Entity change block. Eg: enderman
     *
     * @param e
     */
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (!protections.getFlagAt(e.getBlock().getLocation(), Flag.MOB_GRIEFING)) {
            e.setCancelled(true);
        }
    }

    /**
     * Fire spreading across entities
     *
     * @param e
     */
    @EventHandler
    public void onEntityIgnite(EntityCombustByEntityEvent e) {
        if (!protections.getFlagAt(e.getEntity().getLocation(), Flag.FIRE_SPREAD)) {
            e.setCancelled(true);
        }
    }

    /**
     * Disable pigs from changing to pig zombies
     */
    @EventHandler
    public void onPigZap(PigZapEvent e) {
        if (!protections.getFlagAt(e.getEntity().getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
            e.setCancelled(true);
        }
    }

    /**
     * Disable creepers from changing to charged creepers
     */
    @EventHandler
    public void onCreeperZap(CreeperPowerEvent e) {
        if (!protections.getFlagAt(e.getEntity().getLocation(), Flag.ENTITY_DAMAGE_ENTITY)) {
            e.setCancelled(true);
        }
    }

    /**
     * Disable fire spread. Fire should only come from flint and steel
     *
     * @param e
     */
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            if (!protections.getFlagAt(e.getBlock().getLocation(), Flag.FIRE_SPREAD)) {
                e.setCancelled(true);
            }

        }
    }

    /**
     * Disable fire damage
     */
    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (!protections.getFlagAt(e.getBlock().getLocation(), Flag.FIRE_SPREAD)) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevent bonemeal feritilization
     *
     * @param e
     */
    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent e) {
        if (e.getPlayer() == null) return;
        Block b = e.getBlock();

        Player player = e.getPlayer();

        if (!protections.hasPermission(player, b.getLocation(), Perm.BUILD)) {
            sendPlayerMessage(player, Perm.BUILD);
            e.setCancelled(true);
            return;
        }

        if (b.getType() == Material.OAK_SAPLING ||
                b.getType() == Material.SPRUCE_SAPLING ||
                b.getType() == Material.BIRCH_SAPLING ||
                b.getType() == Material.JUNGLE_SAPLING ||
                b.getType() == Material.ACACIA_SAPLING ||
                b.getType() == Material.DARK_OAK_SAPLING) {
            // tag saplings for if they grow
            b.setMetadata("BONEMEAL_PLAYER", new FixedMetadataValue(plugin, player.getName()));
        }
    }

    /**
     * Disable trees growing from outside a pr into one
     */
    @EventHandler
    public void onStructureGrow(StructureGrowEvent e) {
        Location source = e.getLocation();
        Player player = null;
        if (e.isFromBonemeal()) {
            Block b = e.getLocation().getBlock();

            if (b.hasMetadata("BONEMEAL_PLAYER")) {
                Object metadata = b.getMetadata("BONEMEAL_PLAYER").get(0).value();

                player = Bukkit.getPlayer(metadata.toString());
            }
        }

        Protection sourcePr = protections.getProtectionAt(source);

        for (BlockState block : new ArrayList<>(e.getBlocks())) {
            Protection pr = protections.getProtectionAt(block.getLocation());
            if (pr != null && pr.getFlag(Flag.BORDER_TREE_PROTECTION) && pr != sourcePr) {
                if (player != null && pr.hasPermission(player.getUniqueId().toString(), Perm.BUILD)) continue;

                e.getBlocks().remove(block);
            }
        }
    }

    /**
     * Piston extend. Can't extend from outside inside a protection
     *
     * @param e
     */
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        Block piston = e.getBlock();

        Protection sourcePr = protections.getProtectionAt(piston.getLocation());

        Set<Location> locations = new HashSet<>();

        for (Block block : e.getBlocks()) {
            locations.add(block.getLocation().add(0, 255 - block.getLocation().getY(), 0));
            locations.add(block.getLocation().add(e.getDirection().getDirection()).add(0, 255 - block.getLocation().getY(), 0));
        }

        for (Location location : locations) {
            Protection pr = protections.getProtectionAt(location);
            if (pr != null && pr.getFlag(Flag.BORDER_PISTON_PROTECTION) && pr != sourcePr) {
                e.setCancelled(true);
                return;
            }
        }

    }

    /**
     * Piston retract. Can't retract from outside inside
     *
     * @param e
     */
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        Block piston = e.getBlock();

        Protection sourcePr = protections.getProtectionAt(piston.getLocation());

        Set<Location> locations = new HashSet<>();

        for (Block block : e.getBlocks()) {
            locations.add(block.getLocation().add(0, 255 - block.getLocation().getY(), 0));
            locations.add(block.getLocation().subtract(e.getDirection().getDirection()).add(0, 255 - block.getLocation().getY(), 0));
        }

        for (Location location : locations) {
            Protection pr = protections.getProtectionAt(location);
            if (pr != null && pr.getFlag(Flag.BORDER_PISTON_PROTECTION) && pr != sourcePr) {
                e.setCancelled(true);
                return;
            }
        }

    }

    /**
     * Stop fluids going over borders
     *
     * @param e
     */
    @EventHandler
    public void onFluidFlow(BlockFromToEvent e) {
        Protection pr = protections.getProtectionAt(e.getToBlock().getLocation());
        if (pr == null) return;

        if (pr.getFlag(Flag.BORDER_FLUID_PROTECTION) && !pr.withinBounds(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /**
     * Stop pumpkins and melons from spawning over a border
     */
    @EventHandler
    public void onBlockGrow(BlockGrowEvent e) {
        if (e.getNewState().getType() != Material.PUMPKIN && e.getNewState().getType() != Material.MELON) return;


        Location l = e.getBlock().getLocation();
        Protection protection = protections.getProtectionAt(l);

        if (protection == null || !protection.getFlag(Flag.BORDER_TREE_PROTECTION)) return;

        // work out where growth was from
        Set<Location> locations = new HashSet<>();

        for (Location lRel : Arrays.asList(l.clone().add(1, 0, 0), l.clone().add(-1, 0, 0), l.clone().add(0, 0, 1), l.clone().add(0, 0, -1))) {
            if (lRel.getBlock().getType() == Material.MELON_STEM && e.getNewState().getType() == Material.MELON)
                locations.add(lRel);
            else if (lRel.getBlock().getType() == Material.PUMPKIN_STEM && e.getNewState().getType() == Material.PUMPKIN)
                locations.add(lRel);
        }

        for (Location location : locations) {
            if (!protection.withinBounds(location)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Stop portals from being created in protections a player does not have access to (generation when trying to change world)
     *
     * @param e
     */
    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        // get blocks created, see if theyre in the pr, cancel if player
        if (e.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) return; // fire should not be cancelled

        Entity entity = e.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            for (BlockState block : new ArrayList<>(e.getBlocks())) {
                if (protections.getFlagAt(block.getLocation(), Flag.NETHER_PORTAL_PROTECTION) && !protections.hasPermission(player, block.getLocation(), Perm.BUILD)) {
                    e.setCancelled(true);
                    return;
                }
            }

        } else {
            for (BlockState block : new ArrayList<>(e.getBlocks())) {
                if (protections.getFlagAt(block.getLocation(), Flag.NETHER_PORTAL_PROTECTION)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }


    }
}
