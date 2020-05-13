import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.Flag;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionBuilder;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.protection.perms.Perm;
import nz.tomay0.PixelProtect.protection.perms.PermLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class ProtectionTests {
    /**
     * Mock World objects, since they can't be determined without a server
     */
    @Mock
    private World overworld, nether;

    @Mock
    private Player ownerPlayer, noonePlayer, memberPlayer, adminPlayer;

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

        // mock players
        ownerPlayer = mock(Player.class);
        noonePlayer = mock(Player.class);
        memberPlayer = mock(Player.class);
        adminPlayer = mock(Player.class);
        when(ownerPlayer.getUniqueId()).thenReturn(ownerUUID);
        when(noonePlayer.getUniqueId()).thenReturn(nooneUUID);
        when(memberPlayer.getUniqueId()).thenReturn(memberUUID);
        when(adminPlayer.getUniqueId()).thenReturn(adminUUID);
        when(ownerPlayer.getLocation()).thenReturn(new Location(overworld, 0, 100, 0));
        when(ownerPlayer.getName()).thenReturn("Owner1");
        when(adminPlayer.getLocation()).thenReturn(new Location(overworld, -200, 100, 50));
        when(adminPlayer.getName()).thenReturn("Admin1");

        // mock Bukkit.getWorld() call
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getWorld("world")).thenReturn(overworld);
        when(Bukkit.getWorld("world_nether")).thenReturn(nether);
    }

    /**
     * Test overlap symmetrically
     *
     * @param pr1
     * @param pr2
     */
    private static void doubleOverlapTest(ProtectionHandler handler, boolean expected, Protection pr1, Protection pr2) {
        if (expected) {
            assertTrue(handler.isOverlapping(pr1, pr2));
            assertTrue(handler.isOverlapping(pr2, pr1));
        } else {
            assertFalse(handler.isOverlapping(pr1, pr2));
            assertFalse(handler.isOverlapping(pr2, pr1));
        }
    }

    /**
     * Clean temp/ directory and return it
     */
    private static File getTempDir() {
        File temp = new File("temp");
        if (!temp.exists()) {
            temp.mkdir();
        } else {
            for (File f : temp.listFiles()) {
                f.delete();
            }
        }

        return temp;
    }

    /**
     * Remove temp/ dir
     */
    private static void removeTempDir() {
        File temp = new File("temp");
        if (!temp.exists()) return;

        for (File f : temp.listFiles()) {
            f.delete();
        }

        temp.delete();

    }

    /**
     * Get a file from the test resources
     *
     * @param dir
     * @return
     */
    private static File getTestResource(String dir) {
        URL url = ProtectionTests.class.getResource(dir);

        return new File(url.getFile());
    }


    /**
     * Test the boundaries of a protection centred at 0,0 with boundaries at -100,-100 to 100,100
     */
    @Test
    public void testBoundaries() {
        // create protection
        Protection protection = new Protection("Protection1", "world", -100, 100, -100, 100, "owner", new Location(overworld, 0, 80, 0));

        assertTrue(protection.withinBounds(new Location(overworld, 0, 20, 0)));
        assertTrue(protection.withinBounds(new Location(overworld, 0, 10000, 0)));
        assertTrue(protection.withinBounds(new Location(overworld, -100, 20, 0)));
        assertFalse(protection.withinBounds(new Location(overworld, -101, 20, 0)));
        assertTrue(protection.withinBounds(new Location(overworld, -100, 20, -100)));
        assertFalse(protection.withinBounds(new Location(overworld, -100, 20, -101)));
        assertTrue(protection.withinBounds(new Location(overworld, 100, 20, 100)));
        assertFalse(protection.withinBounds(new Location(overworld, 101, 20, 100)));
        assertFalse(protection.withinBounds(new Location(overworld, 100, 20, 101)));
        assertFalse(protection.withinBounds(new Location(nether, 0, 20, 0)));
    }

    /**
     * Invalid protections
     */
    @Test
    public void testInvalidProtection() {
        // spaces in name
        try {
            new Protection("Protection with spaces", "world", -100, 100, -100, 100, "owner", new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // too small
        try {
            new Protection("Protection1", "world", -1, 1, -1, 1, "owner", new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // west > east
        try {
            new Protection("Protection1", "world", 100, -100, -100, 100, "owner", new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // north > south
        try {
            new Protection("Protection1", "world", -100, 100, 100, -100, "owner", new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // boundary invalid
        try {
            new Protection("Protection1", "world", -2, 1, -5, 5, "owner", new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // null owner
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2, null, new Location(overworld, 0, 80, 0));
            fail();
        } catch (InvalidProtectionException e) {
        }

        // null home
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2, "owner", null);
            fail();
        } catch (InvalidProtectionException e) {
        }

    }

    /**
     * Test default permissions
     */
    @Test
    public void testDefaultPermissions() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0));

        protection1.setPermissionLevel("admin", PermLevel.ADMIN);
        protection1.setPermissionLevel("member", PermLevel.MEMBER);

        // check building permissions
        assertTrue(protection1.hasPermission("owner", Perm.BUILD));
        assertTrue(protection1.hasPermission("admin", Perm.BUILD));
        assertTrue(protection1.hasPermission("member", Perm.BUILD));
        assertFalse(protection1.hasPermission("noone", Perm.BUILD));

        // check update permissions
        assertTrue(protection1.hasPermission("owner", Perm.UPDATE));
        assertTrue(protection1.hasPermission("admin", Perm.UPDATE));
        assertFalse(protection1.hasPermission("member", Perm.UPDATE));
        assertFalse(protection1.hasPermission("noone", Perm.UPDATE));

        // check remove permissions
        assertTrue(protection1.hasPermission("owner", Perm.REMOVE));
        assertFalse(protection1.hasPermission("admin", Perm.REMOVE));
        assertFalse(protection1.hasPermission("member", Perm.REMOVE));
        assertFalse(protection1.hasPermission("noone", Perm.REMOVE));
    }

    /**
     * Test player specific permissions
     */
    @Test
    public void testPlayerSpecificPermissions() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0));

        protection1.setPermissionLevel("member1", PermLevel.MEMBER);
        protection1.setPermissionLevel("member2", PermLevel.MEMBER);

        protection1.setSpecificPermission("noone1", Perm.INTERACT, true);
        protection1.setSpecificPermission("member2", Perm.CHEST, false);
        protection1.setSpecificPermission("member1", Perm.SETHOME, true);

        // check build permissions
        assertTrue(protection1.hasPermission("member1", Perm.BUILD));
        assertTrue(protection1.hasPermission("member2", Perm.BUILD));
        assertFalse(protection1.hasPermission("noone1", Perm.BUILD));
        assertFalse(protection1.hasPermission("noone2", Perm.BUILD));

        // check interact permissions
        assertTrue(protection1.hasPermission("member1", Perm.INTERACT));
        assertTrue(protection1.hasPermission("member2", Perm.INTERACT));
        assertTrue(protection1.hasPermission("noone1", Perm.INTERACT));
        assertFalse(protection1.hasPermission("noone2", Perm.INTERACT));

        // check chest permissions
        assertTrue(protection1.hasPermission("member1", Perm.CHEST));
        assertFalse(protection1.hasPermission("member2", Perm.CHEST));
        assertFalse(protection1.hasPermission("noone1", Perm.CHEST));
        assertFalse(protection1.hasPermission("noone2", Perm.CHEST));

        // check sethome permissions
        assertTrue(protection1.hasPermission("member1", Perm.SETHOME));
        assertFalse(protection1.hasPermission("member2", Perm.SETHOME));
        assertFalse(protection1.hasPermission("noone1", Perm.SETHOME));
        assertFalse(protection1.hasPermission("noone2", Perm.SETHOME));

        // check update permissions
        assertFalse(protection1.hasPermission("member1", Perm.UPDATE));
        assertFalse(protection1.hasPermission("member2", Perm.UPDATE));
        assertFalse(protection1.hasPermission("noone1", Perm.UPDATE));
        assertFalse(protection1.hasPermission("noone2", Perm.UPDATE));
        assertTrue(protection1.hasPermission("owner", Perm.UPDATE));

    }

    /**
     * Test changing default permissions of different levels
     */
    @Test
    public void testLevelPermissions() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0));

        protection1.setPermissionLevel("member", PermLevel.MEMBER);
        protection1.setPermissionLevel("admin", PermLevel.ADMIN);

        protection1.setDefaultPermissionLevel(Perm.INTERACT, PermLevel.NONE);
        protection1.setDefaultPermissionLevel(Perm.BUILD, PermLevel.ADMIN);
        protection1.setDefaultPermissionLevel(Perm.UPDATE, PermLevel.OWNER);

        // check interact permissions
        assertTrue(protection1.hasPermission("noone", Perm.INTERACT));
        assertTrue(protection1.hasPermission("member", Perm.INTERACT));
        assertTrue(protection1.hasPermission("admin", Perm.INTERACT));
        assertTrue(protection1.hasPermission("owner", Perm.INTERACT));

        // check build permissions
        assertFalse(protection1.hasPermission("noone", Perm.BUILD));
        assertFalse(protection1.hasPermission("member", Perm.BUILD));
        assertTrue(protection1.hasPermission("admin", Perm.BUILD));
        assertTrue(protection1.hasPermission("owner", Perm.BUILD));

        // check update permissions
        assertFalse(protection1.hasPermission("noone", Perm.UPDATE));
        assertFalse(protection1.hasPermission("member", Perm.UPDATE));
        assertFalse(protection1.hasPermission("admin", Perm.UPDATE));
        assertTrue(protection1.hasPermission("owner", Perm.UPDATE));
    }

    /**
     * Test changing default level permissions, but permissions should propagate to lower/higher levels
     */
    @Test
    public void testLevelPermissions2() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0));

        protection1.setPermissionLevel("member", PermLevel.MEMBER);
        protection1.setPermissionLevel("admin", PermLevel.ADMIN);

        protection1.setDefaultPermissionLevel(Perm.SETHOME, PermLevel.NONE);
        protection1.setDefaultPermissionLevel(Perm.BUILD, PermLevel.OWNER);

        // check sethome permissions
        assertTrue(protection1.hasPermission("noone", Perm.SETHOME));
        assertTrue(protection1.hasPermission("member", Perm.SETHOME));
        assertTrue(protection1.hasPermission("admin", Perm.SETHOME));
        assertTrue(protection1.hasPermission("owner", Perm.SETHOME));

        // check build permissions
        assertFalse(protection1.hasPermission("noone", Perm.BUILD));
        assertFalse(protection1.hasPermission("member", Perm.BUILD));
        assertFalse(protection1.hasPermission("admin", Perm.BUILD));
        assertTrue(protection1.hasPermission("owner", Perm.BUILD));

        // set this back to normal
        protection1.setDefaultPermissionLevel(Perm.SETHOME, PermLevel.MEMBER);

    }

    /**
     * There must be only 1 owner and the permissions of the owner cannot be updated.
     * This testcase covers this.
     */
    @Test
    public void testOwner() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "player1", new Location(overworld, 0, 80, 0));

        protection1.setPermissionLevel("player2", PermLevel.ADMIN);

        assertEquals(protection1.getPermissionLevel("player1"), PermLevel.OWNER);
        assertEquals(protection1.getPermissionLevel("player2"), PermLevel.ADMIN);

        // try disable chest access, owner should keep their access
        try {
            protection1.setSpecificPermission("player1", Perm.CHEST, false);
            fail();
        } catch (InvalidProtectionException e) {
        }
        protection1.setSpecificPermission("player2", Perm.CHEST, false);

        assertTrue(protection1.hasPermission("player1", Perm.CHEST));
        assertFalse(protection1.hasPermission("player2", Perm.CHEST));

        // now swap owners

        protection1.setPermissionLevel("player2", PermLevel.OWNER);

        assertEquals(protection1.getPermissionLevel("player1"), PermLevel.ADMIN);
        assertEquals(protection1.getPermissionLevel("player2"), PermLevel.OWNER);

        // both should have access to chests
        assertTrue(protection1.hasPermission("player1", Perm.CHEST));
        assertTrue(protection1.hasPermission("player2", Perm.CHEST));

        // swap back
        protection1.setPermissionLevel("player1", PermLevel.OWNER);

        // player2's chest deactivation should have been cleared, therefore both should still have access to chests
        assertTrue(protection1.hasPermission("player1", Perm.CHEST));
        assertTrue(protection1.hasPermission("player2", Perm.CHEST));

        // now disable chest access by level
        protection1.setDefaultPermissionLevel(Perm.CHEST, PermLevel.OWNER);

        assertTrue(protection1.hasPermission("player1", Perm.CHEST));
        assertFalse(protection1.hasPermission("player2", Perm.CHEST));
    }

    /**
     * Some tests to check that protections overlap
     */
    @Test
    public void testOverlapping() {
        ProtectionHandler handler = new ProtectionHandler();

        Protection pr = new Protection("pr1", "world", -10, 10, -10, 10, "owner", new Location(overworld, 0, 80, 0));

        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -5, 5, -5, 5, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world_nether", -5, 5, -5, 5, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world_nether", -50, -30, -5, 5, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -16, -10, -16, -10, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -16, -10, -17, -11, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -17, -11, -16, -10, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -16, -10, -17, -11, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -17, -11, -16, -10, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -10, 1000, -1000, 1000, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -10, 1000, -1000, -11, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -10, 1000, -1000, -10, "owner", new Location(overworld, 0, 80, 0)));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -50, -30, -5, 5, "owner", new Location(overworld, 0, 80, 0)));
    }

    /**
     * Test creating protections in an environment with other protections using the protection handler
     */
    @Test
    public void testCreation() {
        // 25 protections in a grid, no errors should occur
        ProtectionHandler handler = new ProtectionHandler();

        // check valid creation

        handler.addNewProtection(new Protection("pr1", "world", -20, -1, 0, 100, "owner", new Location(overworld, 0, 80, 0)));

        // check creation with same name - ignore case
        try {
            handler.addNewProtection(new Protection("PR1", "world", -40, -21, 0, 100, "owner", new Location(overworld, 0, 80, 0)));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection already exists with the name: pr1", e.getMessage());
        }
        // overlap
        try {
            handler.addNewProtection(new Protection("pr2", "world", -40, -20, 0, 100, "owner", new Location(overworld, 0, 80, 0)));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection cannot overlap another protection", e.getMessage());
        }
        // valid
        handler.addNewProtection(new Protection("pr2", "world", -40, -21, 0, 100, "owner", new Location(overworld, 0, 80, 0)));

        // same name with extra characters
        try {
            handler.addNewProtection(new Protection("[pr1]", "world", -400, -200, 0, 100, "owner", new Location(overworld, 0, 80, 0)));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection already exists with the name: pr1", e.getMessage());
        }
        // valid, smallest possible
        handler.addNewProtection(new Protection("pr3", "world", -104, -100, -104, -100, "owner", new Location(overworld, 0, 80, 0)));
    }

    /**
     * Test creation from yml file
     */
    @Test
    public void testYaml() {
        ProtectionHandler handler = new ProtectionHandler(getTestResource("claims1"));

        // check that the protection is added

        Location l1 = new Location(overworld, -10, 20, -10);
        Location l2 = new Location(overworld, -11, 20, -10);

        Protection protection = handler.getProtectionAt(l1);

        assertEquals("BigProtection", protection.getName());

        // check build permissions
        assertTrue(handler.hasPermission(ownerPlayer, l1, Perm.BUILD));
        assertTrue(handler.hasPermission(ownerPlayer, l2, Perm.BUILD));
        assertFalse(handler.hasPermission(noonePlayer, l1, Perm.BUILD));
        assertTrue(handler.hasPermission(noonePlayer, l2, Perm.BUILD));
        assertTrue(handler.hasPermission(noonePlayer, l1, Perm.INTERACT));
        assertTrue(handler.hasPermission(noonePlayer, l1, Perm.CHEST));
        assertFalse(protection.hasPermission(memberUUID.toString(), Perm.BUILD));
        assertTrue(protection.hasPermission(adminUUID.toString(), Perm.BUILD));
        assertFalse(protection.hasPermission(adminUUID.toString(), Perm.UPDATE));
        assertTrue(protection.hasPermission(ownerUUID.toString(), Perm.UPDATE));
        assertTrue(protection.getFlag(Flag.PVP));
        assertTrue(protection.getFlag(Flag.PRESSURE_PLATE_PROTECTION));

        assertEquals(new Location(overworld, 0, 80, 1), protection.getHome(Protection.DEFAULT_HOME));
    }

    /**
     * Test various invalid yml files as contained within the invalidclaims/ dir
     */
    @Test
    public void testInvalidYaml() {
        File dir = getTestResource("invalidclaims");

        for (File yml : dir.listFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(yml);

            try {
                ProtectionBuilder.fromYaml(config, dir);
                fail();
            } catch (InvalidProtectionException e) {
            }
        }
    }

    /**
     * Save a yml to a file, then load it.
     * Also try renaming.
     */
    @Test
    public void testYmlSave() {
        File dir = getTempDir();

        Location home = new Location(overworld, -5, 80, 2);

        Protection protection = new Protection("protection-1", "world", -10, 10, -10, 10, ownerUUID.toString(), home);
        protection.setDir(dir);

        // check file was created
        assertTrue(new File(dir, "protection1.yml").exists());


        // load the protection by loading the entire dir
        ProtectionHandler handler = new ProtectionHandler(dir);

        Protection loaded = handler.getProtection("protection-1");

        assertEquals(protection.getName(), loaded.getName());
        assertEquals(loaded.getPermissionLevel(ownerUUID.toString()), PermLevel.OWNER);

        assertFalse(handler.hasPermission(noonePlayer, new Location(overworld, -5, 50, -5), Perm.BUILD));

        assertEquals(home, loaded.getHome(Protection.DEFAULT_HOME));

        removeTempDir();
    }

    /**
     * Test creating a yml and editing it
     */
    @Test
    public void testYmlEdit() {
        File dir = getTempDir();

        ProtectionHandler handler = new ProtectionHandler(dir);

        // add some protections
        handler.addNewProtection(new Protection("pr1", "world", -10, 10, -10, 10, ownerUUID.toString(), new Location(overworld, 0, 80, 0)));
        handler.addNewProtection(new Protection("pr2", "world", -21, -11, -10, 10, ownerUUID.toString(), new Location(overworld, 0, 80, 0)));

        assertTrue(new File(dir, "pr1.yml").exists());
        assertTrue(new File(dir, "pr2.yml").exists());

        // rename
        handler.renameProtection("pr2", "p_r_3");

        assertTrue(new File(dir, "pr1.yml").exists());
        assertTrue(new File(dir, "pr3.yml").exists());
        assertFalse(new File(dir, "pr2.yml").exists());


        // remove
        handler.removeProtection("pr3");

        assertTrue(new File(dir, "pr1.yml").exists());
        assertFalse(new File(dir, "pr3.yml").exists());
        assertFalse(new File(dir, "pr2.yml").exists());

        // change some perms in the protection
        handler.getProtection("pr1").setDefaultPermissionLevel(Perm.SETHOME, PermLevel.NONE);
        handler = new ProtectionHandler(dir);
        assertTrue(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.SETHOME));
        assertFalse(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.BUILD));

        handler.getProtection("pr1").setSpecificPermission(nooneUUID.toString(), Perm.BUILD, true);
        handler = new ProtectionHandler(dir);
        assertTrue(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.BUILD));


        removeTempDir();

    }
}
