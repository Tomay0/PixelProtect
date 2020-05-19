import net.milkbowl.vault.economy.Economy;
import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.PluginConfig;
import nz.tomay0.PixelProtect.command.*;
import nz.tomay0.PixelProtect.protection.*;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, PluginConfig.class})
public class CommandTests {
    // TODO /pr confirm tests

    /**
     * Mock World objects, since they can't be determined without a server
     */
    @Mock
    private World overworld, nether;

    @Mock
    private Player ownerPlayer, noonePlayer, memberPlayer, adminPlayer;

    @Mock
    private PixelProtectPlugin plugin;

    @Mock
    private Economy economy;

    @Mock
    private PluginConfig config;

    private ProtectionHandler protections;
    private PlayerStateHandler playerState;

    @Mock
    ConsoleCommandSender console;

    private UUID ownerUUID = new UUID(100, 200);
    private UUID nooneUUID = new UUID(100, 201);
    private UUID memberUUID = new UUID(100, 202);
    private UUID adminUUID = new UUID(100, 203);

    /**
     * Create mock objects and method calls
     */
    @Before
    public void initMocks() {
        // mock world classes
        overworld = mock(World.class);
        when(overworld.getName()).thenReturn("world");

        nether = mock(World.class);
        when(nether.getName()).thenReturn("world_nether");

        // console
        console = mock(ConsoleCommandSender.class);

        // mock players
        ownerPlayer = mockPlayer(new Location(overworld, 0, 100, 0), ownerUUID, "Owner1", true);
        adminPlayer = mockPlayer(new Location(overworld, -200, 100, 50), adminUUID, "Admin1", true);
        memberPlayer = mockPlayer(new Location(nether, 0, 100, 0), memberUUID, "Member1", false);
        noonePlayer = mockPlayer(new Location(overworld, 0, 100, 20), nooneUUID, "Noone1", false);

        // mock Bukkit.getWorld() call
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getWorld("world")).thenReturn(overworld);
        when(Bukkit.getWorld("world_nether")).thenReturn(nether);
        when(Bukkit.getConsoleSender()).thenReturn(console);

        // mock economy, give everyone a balance of 10000
        economy = mock(Economy.class);
        when(economy.getBalance(ownerPlayer)).thenReturn(11000.0);
        when(economy.getBalance(adminPlayer)).thenReturn(11000.0);
        when(economy.getBalance(memberPlayer)).thenReturn(11000.0);
        when(economy.getBalance(noonePlayer)).thenReturn(11000.0);


        // mock config
        config = mock(PluginConfig.class);
        when(config.getMinDiameter()).thenReturn(5);
        when(config.getCostPerBlock()).thenReturn(1.0);
        when(config.getInitialCost()).thenReturn(5.0);
        when(config.getMaxArea()).thenReturn(15000);
        when(config.getDefaultRadius()).thenReturn(3);
        when(config.getBlocksPerHome()).thenReturn(100);
        when(config.getMaxProtections()).thenReturn(3);
        when(config.getMaxHomes()).thenReturn(3);
        when(config.getDisabledWorlds()).thenReturn(new HashSet<>());

        PowerMockito.mockStatic(PluginConfig.class);
        when(PluginConfig.getInstance()).thenReturn(config);

        // mock plugin
        plugin = mock(PixelProtectPlugin.class);
        protections = new HashedProtectionHandler();
        playerState = new PlayerStateHandler(protections);

        playerState.onPlayerJoin(new PlayerJoinEvent(ownerPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(adminPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(memberPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(noonePlayer, null));

        when(plugin.getPlayerStateHandler()).thenReturn(playerState);
        when(plugin.getProtections()).thenReturn(protections);
        when(plugin.getEconomy()).thenReturn(economy);
        when(plugin.getOfflinePlayer("Owner1")).thenReturn(ownerPlayer);
        when(plugin.getOfflinePlayer("Admin1")).thenReturn(adminPlayer);
        when(plugin.getOfflinePlayer("Member1")).thenReturn(memberPlayer);
        when(plugin.getOfflinePlayer("Noone1")).thenReturn(noonePlayer);

    }

    /**
     * Create a mock player
     *
     * @param location
     * @param uuid
     * @param name
     * @return
     */
    private Player mockPlayer(Location location, UUID uuid, String name, boolean admin) {
        Player player = mock(Player.class);
        Player.Spigot spigot = mock(Player.Spigot.class);

        when(player.getName()).thenReturn(name);
        when(player.spigot()).thenReturn(spigot);
        when(player.getLocation()).thenReturn(location);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPlayedBefore()).thenReturn(true);
        when(player.hasPermission("pixelprotect.admin")).thenReturn(admin);

        return player;
    }

    /**
     * Test creating a protection from a command
     */
    @Test
    public void createFromCommand() {
        // create a protection using /pr create <name> <size>
        CreateCommand createCommand = new CreateCommand(plugin);
        ConfirmCommand confirmCommand = new ConfirmCommand(plugin);

        createCommand.onCommand(ownerPlayer, "create bigprotection 50".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));

        assertNotNull(protections.getProtection("bigprotection"));

        createCommand.onCommand(adminPlayer, "create smallprotection 5".split(" ")); // -200, 50
        confirmCommand.onCommand(adminPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("smallprotection"));


        // check the bounds of the big protection
        assertTrue(protections.hasPermission(adminPlayer, new Location(overworld, -51, 100, 0), Perm.BUILD));
        assertFalse(protections.hasPermission(adminPlayer, new Location(overworld, -50, 100, -50), Perm.BUILD));
        assertFalse(protections.hasPermission(adminPlayer, new Location(overworld, -50, 100, 50), Perm.BUILD));
        assertFalse(protections.hasPermission(adminPlayer, new Location(overworld, 50, 100, -50), Perm.BUILD));
        assertFalse(protections.hasPermission(adminPlayer, new Location(overworld, 50, 100, 50), Perm.BUILD));
        assertTrue(protections.hasPermission(adminPlayer, new Location(overworld, 50, 100, 51), Perm.BUILD));

        // check the bounds of the small protection
        assertTrue(protections.hasPermission(ownerPlayer, new Location(overworld, -206, 100, 50), Perm.BUILD));
        assertFalse(protections.hasPermission(ownerPlayer, new Location(overworld, -205, 100, 50), Perm.BUILD));
        assertTrue(protections.hasPermission(ownerPlayer, new Location(overworld, -194, 100, 50), Perm.BUILD));
        assertFalse(protections.hasPermission(ownerPlayer, new Location(overworld, -195, 100, 50), Perm.BUILD));
        assertTrue(protections.hasPermission(ownerPlayer, new Location(overworld, -200, 100, 56), Perm.BUILD));
        assertFalse(protections.hasPermission(ownerPlayer, new Location(overworld, -200, 100, 55), Perm.BUILD));
        assertFalse(protections.hasPermission(ownerPlayer, new Location(overworld, -200, 100, 45), Perm.BUILD));
        assertTrue(protections.hasPermission(ownerPlayer, new Location(overworld, -200, 100, 44), Perm.BUILD));

    }

    /**
     * Test creating a protection from a command with different formatting
     */
    @Test
    public void createValidFormatting() {
        CreateCommand createCommand = new CreateCommand(plugin);
        ConfirmCommand confirmCommand = new ConfirmCommand(plugin);

        // creation with no parameters.
        createCommand.onCommand(ownerPlayer, "create".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        protections.removeProtection("Owner1");
        assertNull(protections.getProtection("Owner1"));

        // creation with only a size
        createCommand.onCommand(ownerPlayer, "create 40".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertEquals(40, protections.getProtection("Owner1").getEast());
        protections.removeProtection("Owner1");
        assertNull(protections.getProtection("Owner1"));

        // creation with multiple size parameters
        createCommand.onCommand(ownerPlayer, "create w5 e3 n2 s10".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertEquals(-5, protections.getProtection("Owner1").getWest());
        assertEquals(3, protections.getProtection("Owner1").getEast());
        assertEquals(-2, protections.getProtection("Owner1").getNorth());
        assertEquals(10, protections.getProtection("Owner1").getSouth());
        protections.removeProtection("Owner1");
        assertNull(protections.getProtection("Owner1"));

        // creation with size parameters, but some are the same
        createCommand.onCommand(ownerPlayer, "create we10 ns20".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertEquals(-10, protections.getProtection("Owner1").getWest());
        assertEquals(10, protections.getProtection("Owner1").getEast());
        assertEquals(-20, protections.getProtection("Owner1").getNorth());
        assertEquals(20, protections.getProtection("Owner1").getSouth());
        protections.removeProtection("Owner1");
        assertNull(protections.getProtection("Owner1"));

        // creation with a name AND size parameters
        createCommand.onCommand(ownerPlayer, "create west200 ns20 10".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("west200"));
        assertEquals(-10, protections.getProtection("west200").getWest());
        assertEquals(10, protections.getProtection("west200").getEast());
        assertEquals(-20, protections.getProtection("west200").getNorth());
        assertEquals(20, protections.getProtection("west200").getSouth());
        protections.removeProtection("west200");
        assertNull(protections.getProtection("west200"));

        // creation with only the name (default size)
        createCommand.onCommand(ownerPlayer, "create west200".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertNotNull(protections.getProtection("west200"));
    }

    /**
     * Test creating a protection from a command.
     * Includes invalid formatting and
     */
    @Test
    public void createInvalidCreateCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);

        // not enough funds
        createCommand.onCommand(ownerPlayer, "create 1000".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertTrue(protections.getProtectionsAt(new Location(overworld, 0, 100, 0)).isEmpty());

        // create some test protections
        protections.addNewProtection(new Protection("Owner1", "world", -100, -50, -100, 100,
                ownerUUID.toString(), new Location(overworld, 0, 80, 0)));
        protections.addNewProtection(new Protection("Owner2", "world", -10, 10, -50, -20,
                ownerUUID.toString(), new Location(overworld, 0, 80, 0)));

        // no size
        createCommand.onCommand(ownerPlayer, "create Owner3 yes".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // space between direction
        createCommand.onCommand(ownerPlayer, "create Owner3 s 20 30".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // negative size
        createCommand.onCommand(ownerPlayer, "create Owner3 -20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // negative size for one parameter
        createCommand.onCommand(ownerPlayer, "create Owner3 w-20 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // negative size for one parameter 2
        createCommand.onCommand(ownerPlayer, "create Owner3 w20 -20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // name with spaces
        createCommand.onCommand(ownerPlayer, "create Owner3 ok 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // ending with the direction
        createCommand.onCommand(ownerPlayer, "create Owner3 20s".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner3"));

        // create with same name
        createCommand.onCommand(ownerPlayer, "create 10".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertTrue(protections.getProtectionsAt(new Location(overworld, 0, 100, 0)).isEmpty());

        // create but overlapping
        createCommand.onCommand(ownerPlayer, "create Owner3 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer, economy));
        assertTrue(protections.getProtectionsAt(new Location(overworld, 0, 100, 0)).isEmpty());

        // create valid
        createCommand.onCommand(ownerPlayer, "create Owner3 19".split(" "));
        plugin.getPlayerStateHandler().confirm(ownerPlayer, economy);
        assertFalse(protections.getProtectionsAt(new Location(overworld, 0, 100, 0)).isEmpty());

    }

    /**
     * Expand command. Valid + invalid tests included
     */
    @Test
    public void testExpandCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);
        ExpandCommand expandCommand = new ExpandCommand(plugin);
        ConfirmCommand confirmCommand = new ConfirmCommand(plugin);

        // create
        createCommand.onCommand(ownerPlayer, "create 20".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));

        assertEquals(-20, protections.getProtection("Owner1").getWest());
        assertEquals(20, protections.getProtection("Owner1").getEast());
        assertEquals(-20, protections.getProtection("Owner1").getNorth());
        assertEquals(20, protections.getProtection("Owner1").getSouth());

        // expand by 1 in all directions
        expandCommand.onCommand(ownerPlayer, "expand 1".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));

        assertEquals(-21, protections.getProtection("Owner1").getWest());
        assertEquals(21, protections.getProtection("Owner1").getEast());
        assertEquals(-21, protections.getProtection("Owner1").getNorth());
        assertEquals(21, protections.getProtection("Owner1").getSouth());

        // expand north/south by 20
        expandCommand.onCommand(console, "expand owner_1 ns20".split(" "));
        confirmCommand.onCommand(console, "confirm".split(" "));

        assertEquals(-21, protections.getProtection("Owner1").getWest());
        assertEquals(21, protections.getProtection("Owner1").getEast());
        assertEquals(-41, protections.getProtection("Owner1").getNorth());
        assertEquals(41, protections.getProtection("Owner1").getSouth());

        // expand east by -30, north by -10
        expandCommand.onCommand(ownerPlayer, "expand Owner1 n-10 e-30".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        assertEquals(-21, protections.getProtection("Owner1").getWest());
        assertEquals(-9, protections.getProtection("Owner1").getEast());
        assertEquals(-31, protections.getProtection("Owner1").getNorth());
        assertEquals(41, protections.getProtection("Owner1").getSouth());

        // another player create a protection
        createCommand.onCommand(adminPlayer, "create Test 8".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));

        // expand while another nearby, but still valid
        expandCommand.onCommand(ownerPlayer, "expand Owner1 w20".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        assertEquals(-41, protections.getProtection("Owner1").getWest());
        assertEquals(-9, protections.getProtection("Owner1").getEast());
        assertEquals(-31, protections.getProtection("Owner1").getNorth());
        assertEquals(41, protections.getProtection("Owner1").getSouth());


        // some invalid
        expandCommand.onCommand(ownerPlayer, "expand Owner2 n1".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        expandCommand.onCommand(ownerPlayer, "expand Owner1 n 1".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        expandCommand.onCommand(adminPlayer, "expand Owner1 n1".split(" "));
        assertFalse(playerState.confirm(adminPlayer, economy));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        expandCommand.onCommand(ownerPlayer, "expand Owner1 s1 w151".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        expandCommand.onCommand(ownerPlayer, "expand".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
    }


    /**
     * Test the shift command
     */
    @Test
    public void testShiftCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);
        ShiftCommand shiftCommand = new ShiftCommand(plugin);
        ConfirmCommand confirmCommand = new ConfirmCommand(plugin);

        // create
        createCommand.onCommand(ownerPlayer, "create 20".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));

        assertEquals(-20, protections.getProtection("Owner1").getWest());
        assertEquals(20, protections.getProtection("Owner1").getEast());
        assertEquals(-20, protections.getProtection("Owner1").getNorth());
        assertEquals(20, protections.getProtection("Owner1").getSouth());

        createCommand.onCommand(adminPlayer, "create 20".split(" "));
        confirmCommand.onCommand(adminPlayer, "confirm".split(" "));


        // invalid
        shiftCommand.onCommand(ownerPlayer, "shift 20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(ownerPlayer, "shift nsew20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(ownerPlayer, "shift ns20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(ownerPlayer, "shift ew20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(ownerPlayer, "shift".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(ownerPlayer, "shift noone s30".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
        shiftCommand.onCommand(adminPlayer, "shift Owner1 s30".split(" "));
        assertFalse(playerState.confirm(adminPlayer, economy));

        // valid
        shiftCommand.onCommand(ownerPlayer, "shift Owner1 s30".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertEquals(-20, protections.getProtection("Owner1").getWest());
        assertEquals(20, protections.getProtection("Owner1").getEast());
        assertEquals(10, protections.getProtection("Owner1").getNorth());
        assertEquals(50, protections.getProtection("Owner1").getSouth());

        // valid
        shiftCommand.onCommand(ownerPlayer, "shift Owner1 e20".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));
        assertEquals(0, protections.getProtection("Owner1").getWest());
        assertEquals(40, protections.getProtection("Owner1").getEast());
        assertEquals(10, protections.getProtection("Owner1").getNorth());
        assertEquals(50, protections.getProtection("Owner1").getSouth());

        // overlap
        shiftCommand.onCommand(ownerPlayer, "shift Owner1 w180".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));
    }

    /**
     * Test the remove command
     */
    @Test
    public void testRemoveCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);
        RemoveCommand removeCommand = new RemoveCommand(plugin);
        ConfirmCommand confirmCommand = new ConfirmCommand(plugin);

        createCommand.onCommand(ownerPlayer, "create 20".split(" "));
        confirmCommand.onCommand(ownerPlayer, "confirm".split(" "));

        assertNotNull(protections.getProtection("Owner1"));

        removeCommand.onCommand(adminPlayer, "remove".split(" "));
        assertFalse(playerState.confirm(adminPlayer, economy));
        removeCommand.onCommand(adminPlayer, "remove Owner1".split(" "));
        assertFalse(playerState.confirm(adminPlayer, economy));
        assertNotNull(protections.getProtection("Owner1"));

        removeCommand.onCommand(ownerPlayer, "remove".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertNull(protections.getProtection("Owner1"));


    }

    /**
     * Test the rename command
     */
    @Test
    public void testRenameCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);
        RenameCommand renameCommand = new RenameCommand(plugin);

        // create
        createCommand.onCommand(ownerPlayer, "create 20".split(" "));
        playerState.confirm(ownerPlayer, economy);

        assertNotNull(protections.getProtection("Owner1"));

        // rename invalid
        renameCommand.onCommand(adminPlayer, "rename Owner1 test".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        renameCommand.onCommand(ownerPlayer, "rename Owner1".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        renameCommand.onCommand(ownerPlayer, "rename something test".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        // rename valid
        renameCommand.onCommand(ownerPlayer, "rename ok".split(" "));
        assertNull(protections.getProtection("Owner1"));
        assertNotNull(protections.getProtection("ok"));

        renameCommand.onCommand(ownerPlayer, "rename ok test".split(" "));
        assertNull(protections.getProtection("ok"));
        assertNotNull(protections.getProtection("test"));

        renameCommand.onCommand(console, "rename test hello".split(" "));
        assertNull(protections.getProtection("test"));
        assertNotNull(protections.getProtection("hello"));
    }

    /**
     * Test the move command
     */
    @Test
    public void testMoveCommand() {
        CreateCommand createCommand = new CreateCommand(plugin);
        MoveCommand moveCommand = new MoveCommand(plugin);

        // create
        createCommand.onCommand(ownerPlayer, "create 3".split(" "));
        playerState.confirm(ownerPlayer, economy);

        // get the protection
        Protection protection = protections.getProtection("Owner1");
        assertNotNull(protection);

        protection.setPermissionLevel(adminUUID.toString(), PermLevel.ADMIN);

        // valid update tests

        // admin location -200, 50.
        // protection size from -3 to 3 = 6 difference

        // North West
        moveCommand.onCommand(adminPlayer, "move Owner1 nw".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));
        assertEquals(-200, protection.getWest());
        assertEquals(-194, protection.getEast());
        assertEquals(50, protection.getNorth());
        assertEquals(56, protection.getSouth());

        // North East
        moveCommand.onCommand(adminPlayer, "move Owner1 ne".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));
        assertEquals(-206, protection.getWest());
        assertEquals(-200, protection.getEast());
        assertEquals(50, protection.getNorth());
        assertEquals(56, protection.getSouth());

        // South East
        moveCommand.onCommand(adminPlayer, "move Owner1 se".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));
        assertEquals(-206, protection.getWest());
        assertEquals(-200, protection.getEast());
        assertEquals(44, protection.getNorth());
        assertEquals(50, protection.getSouth());

        // South West
        moveCommand.onCommand(adminPlayer, "move Owner1 sw".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));
        assertEquals(-200, protection.getWest());
        assertEquals(-194, protection.getEast());
        assertEquals(44, protection.getNorth());
        assertEquals(50, protection.getSouth());

        // Centre
        moveCommand.onCommand(ownerPlayer, "move c".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-3, protection.getWest());
        assertEquals(3, protection.getEast());
        assertEquals(-3, protection.getNorth());
        assertEquals(3, protection.getSouth());

        // North
        moveCommand.onCommand(ownerPlayer, "move north".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-3, protection.getWest());
        assertEquals(3, protection.getEast());
        assertEquals(0, protection.getNorth());
        assertEquals(6, protection.getSouth());

        // South
        moveCommand.onCommand(ownerPlayer, "move south".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-3, protection.getWest());
        assertEquals(3, protection.getEast());
        assertEquals(-6, protection.getNorth());
        assertEquals(0, protection.getSouth());

        // West
        moveCommand.onCommand(ownerPlayer, "move west".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(0, protection.getWest());
        assertEquals(6, protection.getEast());
        assertEquals(-3, protection.getNorth());
        assertEquals(3, protection.getSouth());

        // East
        moveCommand.onCommand(ownerPlayer, "move east".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-6, protection.getWest());
        assertEquals(0, protection.getEast());
        assertEquals(-3, protection.getNorth());
        assertEquals(3, protection.getSouth());

        // Test relhome
        moveCommand.onCommand(ownerPlayer, "move c".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        moveCommand.onCommand(ownerPlayer, "move relhome".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-3, protection.getWest());
        assertEquals(3, protection.getEast());
        assertEquals(-3, protection.getNorth());
        assertEquals(3, protection.getSouth());

        moveCommand.onCommand(adminPlayer, "move Owner1 c".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));
        assertEquals(-203, protection.getWest());
        assertEquals(-197, protection.getEast());
        assertEquals(47, protection.getNorth());
        assertEquals(53, protection.getSouth());

        protection.setHome("home", new Location(overworld, -1, 80, -2));

        moveCommand.onCommand(ownerPlayer, "move relhome".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        assertEquals(-202, protection.getWest());
        assertEquals(-196, protection.getEast());
        assertEquals(49, protection.getNorth());
        assertEquals(55, protection.getSouth());
    }


    /**
     * Test the /pr setperm command with levels
     */
    @Test
    public void testSetPermLevel() {
        SetPermCommand setPerm = new SetPermCommand(plugin);
        CreateCommand create = new CreateCommand(plugin);

        create.onCommand(ownerPlayer, "create 20".split(" "));
        playerState.confirm(ownerPlayer, economy);

        Protection protection = protections.getProtection("Owner1");

        // test owner setting all perm levels
        setPerm.onCommand(ownerPlayer, "setperm Admin1 admin".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(adminUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Member1 MeMbEr asdjioasodji".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(memberUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Owner1 Member1 none".split(" "));
        assertEquals(PermLevel.NONE, protection.getPermissionLevel(memberUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Member1 owner".split(" "));
        assertEquals(PermLevel.OWNER, protection.getPermissionLevel(memberUUID.toString()));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(ownerUUID.toString()));

        // test update perms as an admin
        setPerm.onCommand(ownerPlayer, "setperm Member1 admin".split(" "));
        assertEquals(PermLevel.OWNER, protection.getPermissionLevel(memberUUID.toString()));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(ownerUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Admin1 owner".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(adminUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Admin1 member".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(adminUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Noone1 member".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(nooneUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Noone1 admin".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(nooneUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Noone1 none".split(" "));
        assertEquals(PermLevel.NONE, protection.getPermissionLevel(nooneUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Owner1 member".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(ownerUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Owner1 owner".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(ownerUUID.toString()));

        setPerm.onCommand(memberPlayer, "setperm Owner1 Owner1 owner".split(" "));
        assertEquals(PermLevel.OWNER, protection.getPermissionLevel(ownerUUID.toString()));
        setPerm.onCommand(ownerPlayer, "setperm Owner1 Member1 member".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(memberUUID.toString()));

        // test update permissions as member - should not be allowed
        setPerm.onCommand(memberPlayer, "setperm Noone1 member".split(" "));
        assertEquals(PermLevel.NONE, protection.getPermissionLevel(nooneUUID.toString()));

        setPerm.onCommand(memberPlayer, "setperm Admin1 member".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(adminUUID.toString()));
    }

    /**
     * Test the /pr setperm command with specific permissions
     */
    @Test
    public void testSetPermSpecific() {
        SetPermCommand setPerm = new SetPermCommand(plugin);
        CreateCommand create = new CreateCommand(plugin);

        create.onCommand(ownerPlayer, "create 20".split(" "));
        playerState.confirm(ownerPlayer, economy);

        Protection protection = protections.getProtection("Owner1");

        // test owner setting all perm levels
        setPerm.onCommand(ownerPlayer, "setperm Admin1 admin".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(adminUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Member1 member".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(memberUUID.toString()));

        setPerm.onCommand(ownerPlayer, "setperm Member1 setperms true".split(" "));
        assertTrue(protection.hasPermission(memberUUID.toString(), Perm.SETPERMS));

        // owner set noone

        setPerm.onCommand(ownerPlayer, "setperm Noone1 remove true".split(" "));
        assertTrue(protection.hasPermission(nooneUUID.toString(), Perm.REMOVE));
        setPerm.onCommand(ownerPlayer, "setperm Noone1 remove false".split(" "));
        assertFalse(protection.hasPermission(nooneUUID.toString(), Perm.REMOVE));

        // admin set noone

        setPerm.onCommand(adminPlayer, "setperm Owner1 Noone1 remove true".split(" "));
        assertFalse(protection.hasPermission(nooneUUID.toString(), Perm.REMOVE));
        setPerm.onCommand(adminPlayer, "setperm Owner1 Noone1 update true".split(" "));
        assertTrue(protection.hasPermission(nooneUUID.toString(), Perm.UPDATE));
        setPerm.onCommand(adminPlayer, "setperm Owner1 Noone1 update false".split(" "));
        assertFalse(protection.hasPermission(nooneUUID.toString(), Perm.UPDATE));

        // member set noone

        setPerm.onCommand(memberPlayer, "setperm Owner1 Noone1 update true".split(" "));
        assertFalse(protection.hasPermission(nooneUUID.toString(), Perm.UPDATE));
        setPerm.onCommand(memberPlayer, "setperm Owner1 Noone1 build true".split(" "));
        assertTrue(protection.hasPermission(nooneUUID.toString(), Perm.BUILD));
        setPerm.onCommand(memberPlayer, "setperm Owner1 Noone1 build false".split(" "));
        assertFalse(protection.hasPermission(nooneUUID.toString(), Perm.BUILD));

        // admin cannot set perms of other admin
        setPerm.onCommand(ownerPlayer, "setperm Owner1 Member1 admin".split(" "));
        assertEquals(PermLevel.ADMIN, protection.getPermissionLevel(memberUUID.toString()));

        setPerm.onCommand(memberPlayer, "setperm Owner1 Admin1 update false".split(" "));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.UPDATE));

        setPerm.onCommand(ownerPlayer, "setperm Owner1 Member1 member".split(" "));
        assertEquals(PermLevel.MEMBER, protection.getPermissionLevel(memberUUID.toString()));

        setPerm.onCommand(memberPlayer, "setperm Owner1 Admin1 update false".split(" "));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.UPDATE));

        // test no setperms perm

        setPerm.onCommand(memberPlayer, "setperm Owner1 Noone1 build true".split(" "));
        assertTrue(protection.hasPermission(nooneUUID.toString(), Perm.BUILD));

        setPerm.onCommand(ownerPlayer, "setperm Owner1 Member1 setperms false".split(" "));

        setPerm.onCommand(memberPlayer, "setperm Owner1 Noone1 build false".split(" "));
        assertTrue(protection.hasPermission(nooneUUID.toString(), Perm.BUILD));

    }

    /**
     * Test the flag command
     */
    @Test
    public void testFlagCommand() {
        FlagCommand config = new FlagCommand(plugin);
        CreateCommand create = new CreateCommand(plugin);

        create.onCommand(ownerPlayer, "create 20".split(" "));
        playerState.confirm(ownerPlayer, economy);

        Protection protection = protections.getProtection("Owner1");

        // test invalid
        config.onCommand(memberPlayer, "flag Owner1 pvp true".split(" "));
        assertFalse(protection.getFlag(Flag.PVP));

        config.onCommand(ownerPlayer, "flag pvp".split(" "));
        assertFalse(protection.getFlag(Flag.PVP));
        config.onCommand(ownerPlayer, "flag pvp whoop".split(" "));
        assertFalse(protection.getFlag(Flag.PVP));

        // valid
        config.onCommand(ownerPlayer, "flag pvp true".split(" "));
        assertTrue(protection.getFlag(Flag.PVP));

    }

    /**
     * Test the setminlevel command
     */
    @Test
    public void testSetMinLevelCommand() {
        SetMinLevelCommand setMinLevel = new SetMinLevelCommand(plugin);
        CreateCommand create = new CreateCommand(plugin);

        create.onCommand(ownerPlayer, "create 20".split(" "));
        playerState.confirm(ownerPlayer, economy);

        Protection protection = protections.getProtection("Owner1");

        // test invalid
        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 something something".split(" "));
        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 something owner".split(" "));
        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 home something".split(" "));

        setMinLevel.onCommand(adminPlayer, "setminlevel Owner1 home none".split(" "));
        assertFalse(protection.hasPermission(adminUUID.toString(), Perm.HOME));


        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 home none".split(" "));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.HOME));
        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 home member".split(" "));
        assertFalse(protection.hasPermission(adminUUID.toString(), Perm.HOME));

        protection.setPermissionLevel(adminUUID.toString(), PermLevel.ADMIN);

        setMinLevel.onCommand(adminPlayer, "setminlevel Owner1 home none".split(" "));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.HOME));

        setMinLevel.onCommand(adminPlayer, "setminlevel Owner1 home owner".split(" "));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.HOME));

        setMinLevel.onCommand(ownerPlayer, "setminlevel Owner1 home owner".split(" "));
        assertFalse(protection.hasPermission(adminUUID.toString(), Perm.HOME));
    }

    /**
     * Test max protections, max area
     */
    @Test
    public void testMaxProtections() {
        CreateCommand create = new CreateCommand(plugin);
        ExpandCommand expand = new ExpandCommand(plugin);
        ShiftCommand shift = new ShiftCommand(plugin);
        SetPermCommand setPerm = new SetPermCommand(plugin);

        // test paying too much
        create.onCommand(ownerPlayer, "create pr1 52".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        create.onCommand(ownerPlayer, "create pr1 51".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        // note that in the test cases the money is not subtracted

        // area 10609. 103x103. Area to go over = 4391. expand 43 blocks.
        expand.onCommand(ownerPlayer, "expand pr1 w43".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        expand.onCommand(ownerPlayer, "expand pr1 w42".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        shift.onCommand(ownerPlayer, "shift pr1 w10000".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));


        // try to create 3 more protections. Max is 3.
        create.onCommand(ownerPlayer, "create pr2 10".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        shift.onCommand(ownerPlayer, "shift pr2 n10000".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        create.onCommand(ownerPlayer, "create pr3 10".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));
        shift.onCommand(ownerPlayer, "shift pr3 s10000".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        create.onCommand(ownerPlayer, "create pr4 10".split(" "));
        assertFalse(playerState.confirm(ownerPlayer, economy));

        // other player create and try to give ownership away
        create.onCommand(adminPlayer, "create pr4 10".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));

        setPerm.onCommand(adminPlayer, "setperm pr4 Owner1 member".split(" "));
        setPerm.onCommand(adminPlayer, "setperm pr4 Owner1 owner".split(" "));
        assertEquals(PermLevel.MEMBER, protections.getProtection("pr4").getPermissionLevel(ownerUUID.toString()));
        assertEquals(PermLevel.OWNER, protections.getProtection("pr4").getPermissionLevel(adminUUID.toString()));

    }

    /**
     * Test sethome limit
     */
    @Test
    public void testSetHome() {
        CreateCommand create = new CreateCommand(plugin);
        ExpandCommand expand = new ExpandCommand(plugin);
        SetHomeCommand sethome = new SetHomeCommand(plugin);

        // size 5 (121 - can set 2 homes)
        create.onCommand(ownerPlayer, "create 5".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));


        Protection protection = protections.getProtection("Owner1");

        // set 1 home
        sethome.onCommand(ownerPlayer, "sethome home1".split(" "));
        assertNotNull(protection.getHome("home1"));

        // set a 2nd home (too many per protection)
        sethome.onCommand(ownerPlayer, "sethome home2".split(" "));
        assertNull(protection.getHome("home2"));

        // expand 10
        expand.onCommand(ownerPlayer, "expand w10".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        sethome.onCommand(ownerPlayer, "sethome home2".split(" "));
        assertNotNull(protection.getHome("home2"));

        sethome.onCommand(ownerPlayer, "sethome home3".split(" "));
        assertNull(protection.getHome("home3"));

        expand.onCommand(ownerPlayer, "expand w100".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));

        sethome.onCommand(ownerPlayer, "sethome home3".split(" "));
        assertNull(protection.getHome("home3"));
    }

    /**
     * Test createadmin and config preset
     */
    @Test
    public void testCreateAdmin() {
        CreateAdminCommand create = new CreateAdminCommand(plugin);
        ExpandCommand expand = new ExpandCommand(plugin);
        ConfigPresetCommand cmd = new ConfigPresetCommand(plugin);

        // size 5 (121 - can set 2 homes)
        create.onCommand(ownerPlayer, "createadmin Spawn 5".split(" "));
        assertTrue(playerState.confirm(ownerPlayer, economy));


        Protection protection = protections.getProtection("Spawn");

        assertTrue(protections.hasPermission(adminPlayer, new Location(overworld, 0, 80, 0), Perm.BUILD));
        assertFalse(protections.hasPermission(memberPlayer, new Location(overworld, 0, 80, 0), Perm.BUILD));

        expand.onCommand(adminPlayer, "expand Spawn w5".split(" "));
        assertTrue(playerState.confirm(adminPlayer, economy));

        assertTrue(protections.hasPermission(ownerPlayer, new Location(overworld, -6, 80, 0), Perm.BUILD));
        assertFalse(protections.hasPermission(memberPlayer, new Location(overworld, -6, 80, 0), Perm.BUILD));

        cmd.onCommand(adminPlayer, "configpreset Spawn wilderness".split(" "));

        assertTrue(protections.hasPermission(memberPlayer, new Location(overworld, -6, 80, 0), Perm.BUILD));

        cmd.onCommand(adminPlayer, "configpreset Spawn default".split(" "));

        assertFalse(protections.hasPermission(memberPlayer, new Location(overworld, -6, 80, 0), Perm.BUILD));
    }
}
