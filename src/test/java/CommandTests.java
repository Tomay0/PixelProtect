import nz.tomay0.PixelProtect.GriefListener;
import nz.tomay0.PixelProtect.PixelProtectPlugin;
import nz.tomay0.PixelProtect.command.*;
import nz.tomay0.PixelProtect.playerstate.PlayerStateHandler;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.perms.Perm;
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

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
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
        ownerPlayer = mockPlayer(new Location(overworld, 0, 100, 0), ownerUUID, "Owner1");
        adminPlayer = mockPlayer(new Location(overworld, -200, 100, 50), adminUUID, "Admin1");
        memberPlayer = mockPlayer(new Location(nether, 0, 100, 0), memberUUID, "Member1");
        noonePlayer = mockPlayer(new Location(overworld, 0, 100, 20), nooneUUID, "Noone1");

        // mock Bukkit.getWorld() call
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getWorld("world")).thenReturn(overworld);
        when(Bukkit.getWorld("world_nether")).thenReturn(nether);
        when(Bukkit.getConsoleSender()).thenReturn(console);

        // mock plugin
        plugin = mock(PixelProtectPlugin.class);
        protections = new ProtectionHandler();
        playerState = new PlayerStateHandler(protections);

        playerState.onPlayerJoin(new PlayerJoinEvent(ownerPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(adminPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(memberPlayer, null));
        playerState.onPlayerJoin(new PlayerJoinEvent(noonePlayer, null));

        when(plugin.getPlayerStateHandler()).thenReturn(playerState);
        when(plugin.getProtections()).thenReturn(protections);

    }

    /**
     * Create a mock player
     *
     * @param location
     * @param uuid
     * @param name
     * @return
     */
    private Player mockPlayer(Location location, UUID uuid, String name) {
        Player player = mock(Player.class);
        Player.Spigot spigot = mock(Player.Spigot.class);

        when(player.getName()).thenReturn(name);
        when(player.spigot()).thenReturn(spigot);
        when(player.getLocation()).thenReturn(location);
        when(player.getUniqueId()).thenReturn(uuid);

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

        // create some test protections
        protections.addNewProtection(new Protection("Owner1", "world", -100, -50, -100, 100, ownerUUID.toString(), new Location(overworld,0,80,0)));
        protections.addNewProtection(new Protection("Owner2", "world", -10, 10, -50, -20, ownerUUID.toString(), new Location(overworld,0,80,0)));

        // no size
        createCommand.onCommand(ownerPlayer, "create Owner3 yes".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // space between direction
        createCommand.onCommand(ownerPlayer, "create Owner3 s 20 30".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // negative size
        createCommand.onCommand(ownerPlayer, "create Owner3 -20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // negative size for one parameter
        createCommand.onCommand(ownerPlayer, "create Owner3 w-20 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // negative size for one parameter 2
        createCommand.onCommand(ownerPlayer, "create Owner3 w20 -20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // name with spaces
        createCommand.onCommand(ownerPlayer, "create Owner3 ok 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // ending with the direction
        createCommand.onCommand(ownerPlayer, "create Owner3 20s".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtection("Owner3"));

        // create with same name
        createCommand.onCommand(ownerPlayer, "create 10".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtectionAt(new Location(overworld, 0, 100, 0)));

        // create but overlapping
        createCommand.onCommand(ownerPlayer, "create Owner3 20".split(" "));
        assertFalse(plugin.getPlayerStateHandler().confirm(ownerPlayer));
        assertNull(protections.getProtectionAt(new Location(overworld, 0, 100, 0)));

        // create valid
        createCommand.onCommand(ownerPlayer, "create Owner3 19".split(" "));
        plugin.getPlayerStateHandler().confirm(ownerPlayer);
        assertNotNull(protections.getProtectionAt(new Location(overworld, 0, 100, 0)));

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
        assertTrue(playerState.confirm(ownerPlayer));

        assertEquals(-21, protections.getProtection("Owner1").getWest());
        assertEquals(-9, protections.getProtection("Owner1").getEast());
        assertEquals(-31, protections.getProtection("Owner1").getNorth());
        assertEquals(41, protections.getProtection("Owner1").getSouth());

        // another player create a protection
        createCommand.onCommand(adminPlayer, "create Test 8".split(" "));
        assertTrue(playerState.confirm(adminPlayer));

        // expand while another nearby, but still valid
        expandCommand.onCommand(ownerPlayer, "expand Owner1 w20".split(" "));
        assertTrue(playerState.confirm(ownerPlayer));

        assertEquals(-41, protections.getProtection("Owner1").getWest());
        assertEquals(-9, protections.getProtection("Owner1").getEast());
        assertEquals(-31, protections.getProtection("Owner1").getNorth());
        assertEquals(41, protections.getProtection("Owner1").getSouth());


        // some invalid
        expandCommand.onCommand(ownerPlayer, "expand Owner2 n1".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));

        expandCommand.onCommand(ownerPlayer, "expand Owner1 n 1".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));

        expandCommand.onCommand(adminPlayer, "expand Owner1 n1".split(" "));
        assertFalse(playerState.confirm(adminPlayer));
        assertFalse(playerState.confirm(ownerPlayer));

        expandCommand.onCommand(ownerPlayer, "expand Owner1 s1 w151".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));

        expandCommand.onCommand(ownerPlayer, "expand".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
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
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(ownerPlayer, "shift nsew20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(ownerPlayer, "shift ns20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(ownerPlayer, "shift ew20".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(ownerPlayer, "shift".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(ownerPlayer, "shift noone s30".split(" "));
        assertFalse(playerState.confirm(ownerPlayer));
        shiftCommand.onCommand(adminPlayer, "shift Owner1 s30".split(" "));
        assertFalse(playerState.confirm(adminPlayer));

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
        assertFalse(playerState.confirm(ownerPlayer));
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
        assertFalse(playerState.confirm(adminPlayer));
        removeCommand.onCommand(adminPlayer, "remove Owner1".split(" "));
        assertFalse(playerState.confirm(adminPlayer));
        assertNotNull(protections.getProtection("Owner1"));

        removeCommand.onCommand(ownerPlayer, "remove".split(" "));
        assertTrue(playerState.confirm(ownerPlayer));
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
        playerState.confirm(ownerPlayer);

        assertNotNull(protections.getProtection("Owner1"));

        // rename invalid
        renameCommand.onCommand(adminPlayer, "rename Owner1 test".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        renameCommand.onCommand(ownerPlayer, "rename test".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        renameCommand.onCommand(ownerPlayer, "rename Owner1".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        renameCommand.onCommand(ownerPlayer, "rename something test".split(" "));
        assertNotNull(protections.getProtection("Owner1"));
        assertNull(protections.getProtection("test"));

        // rename valid
        renameCommand.onCommand(ownerPlayer, "rename Owner1 test".split(" "));
        assertNull(protections.getProtection("Owner1"));
        assertNotNull(protections.getProtection("test"));

        renameCommand.onCommand(console, "rename test hello".split(" "));
        assertNull(protections.getProtection("test"));
        assertNotNull(protections.getProtection("hello"));
    }
}
