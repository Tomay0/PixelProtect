import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.exception.ProtectionExceptionReason;
import nz.tomay0.PixelProtect.protection.*;
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
import java.util.*;

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
        Protection protection = new Protection("Protection1", "world", -100, 100, -100, 100, "owner", new Location(overworld, 0, 80, 0), false);

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
            new Protection("Protection with spaces", "world", -100, 100, -100, 100, "owner", new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // too small
        try {
            new Protection("Protection1", "world", -1, 1, -1, 1, "owner", new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // west > east
        try {
            new Protection("Protection1", "world", 100, -100, -100, 100, "owner", new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // north > south
        try {
            new Protection("Protection1", "world", -100, 100, 100, -100, "owner", new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // boundary invalid
        try {
            new Protection("Protection1", "world", -2, 1, -5, 5, "owner", new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // null owner
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2, null, new Location(overworld, 0, 80, 0), false);
            fail();
        } catch (InvalidProtectionException e) {
        }

        // null home
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2, "owner", null, false);
            fail();
        } catch (InvalidProtectionException e) {
        }

    }

    /**
     * Test default permissions
     */
    @Test
    public void testDefaultPermissions() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0), false);

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0), false);

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0), false);

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner", new Location(overworld, 0, 80, 0), false);

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "player1", new Location(overworld, 0, 80, 0), false);

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
        ProtectionHandler handler = new SequentialProtectionHandler();

        Protection pr = new Protection("pr1", "world", -10, 10, -10, 10, "owner", new Location(overworld, 0, 80, 0), false);

        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -5, 5, -5, 5, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world_nether", -5, 5, -5, 5, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world_nether", -50, -30, -5, 5, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -16, -10, -16, -10, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -16, -10, -17, -11, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -17, -11, -16, -10, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -16, -10, -17, -11, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -17, -11, -16, -10, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -10, 1000, -1000, 1000, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -10, 1000, -1000, -11, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, true, pr,
                new Protection("pr2", "world", -10, 1000, -1000, -10, "owner", new Location(overworld, 0, 80, 0), false));
        doubleOverlapTest(handler, false, pr,
                new Protection("pr2", "world", -50, -30, -5, 5, "owner", new Location(overworld, 0, 80, 0), false));
    }

    /**
     * Test creating protections in an environment with other protections using the protection handler
     */
    @Test
    public void testCreation() {
        // 25 protections in a grid, no errors should occur
        ProtectionHandler handler = new SequentialProtectionHandler();

        // check valid creation

        handler.addNewProtection(new Protection("pr1", "world", -20, -1, 0, 100, "owner", new Location(overworld, 0, 80, 0), false));

        // check creation with same name - ignore case
        try {
            handler.addNewProtection(new Protection("PR1", "world", -40, -21, 0, 100, "owner", new Location(overworld, 0, 80, 0), false));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection already exists with the name: pr1", e.getMessage());
        }
        // overlap
        try {
            handler.addNewProtection(new Protection("pr2", "world", -40, -20, 0, 100, "owner", new Location(overworld, 0, 80, 0), false));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection cannot overlap another protection", e.getMessage());
        }
        // valid
        handler.addNewProtection(new Protection("pr2", "world", -40, -21, 0, 100, "owner", new Location(overworld, 0, 80, 0), false));

        // same name with extra characters
        try {
            handler.addNewProtection(new Protection("[pr1]", "world", -400, -200, 0, 100, "owner", new Location(overworld, 0, 80, 0), false));
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals("A protection already exists with the name: pr1", e.getMessage());
        }
        // valid, smallest possible
        handler.addNewProtection(new Protection("pr3", "world", -104, -100, -104, -100, "owner", new Location(overworld, 0, 80, 0), false));
    }

    /**
     * Test creation from yml file
     */
    @Test
    public void testYaml() {
        ProtectionHandler handler = new SequentialProtectionHandler(getTestResource("claims1"));

        // check that the protection is added

        Location l1 = new Location(overworld, -10, 20, -10);
        Location l2 = new Location(overworld, -11, 20, -10);

        Protection protection = handler.getProtection("BigProtection");

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

        Protection protection = new Protection("protection-1", "world", -10, 10, -10, 10, ownerUUID.toString(), home, false);
        protection.setDir(dir);

        // check file was created
        assertTrue(new File(dir, "protection1.yml").exists());


        // load the protection by loading the entire dir
        ProtectionHandler handler = new SequentialProtectionHandler(dir);

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

        ProtectionHandler handler = new SequentialProtectionHandler(dir);

        // add some protections
        handler.addNewProtection(new Protection("pr1", "world", -10, 10, -10, 10, ownerUUID.toString(), new Location(overworld, 0, 80, 0), false));
        handler.addNewProtection(new Protection("pr2", "world", -21, -11, -10, 10, ownerUUID.toString(), new Location(overworld, 0, 80, 0), false));

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
        handler = new SequentialProtectionHandler(dir);
        assertTrue(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.SETHOME));
        assertFalse(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.BUILD));

        handler.getProtection("pr1").setSpecificPermission(nooneUUID.toString(), Perm.BUILD, true);
        handler = new SequentialProtectionHandler(dir);
        assertTrue(handler.hasPermission(noonePlayer, new Location(overworld, 0, 10, 0), Perm.BUILD));


        removeTempDir();

    }


    /**
     * Test implementation of the hashed protection handler basic
     */
    @Test
    public void hashedProtectionHandler() {
        ProtectionHandler protections = new HashedProtectionHandler();

        protections.addNewProtection(new Protection("Pr1", "world", -1000, 422, -1000, 0,
                "owner", new Location(overworld, 0, 80, 0), false));
        protections.addNewProtection(new Protection("Pr2", "world", 423, 962, -1000, 0,
                "owner", new Location(overworld, 0, 80, 0), false));

        // test lots of locations within Pr1

        for (int x = -1000; x <= 422; x += 18) {
            for (int z = -1000; z <= 0; z += 11) {
                assertEquals("Pr1", protections.getProtectionsAt(new Location(overworld, x, 80, z)).iterator().next().getName());
            }
        }
        for (int x = 423; x <= 962; x += 18) {
            for (int z = -1000; z <= 0; z += 11) {
                assertEquals("Pr2", protections.getProtectionsAt(new Location(overworld, x, 80, z)).iterator().next().getName());
            }
        }

    }

    /**
     * Test hashed protection handler move
     */
    @Test
    public void hashedProtectionMove() {
        HashedProtectionHandler protections = new HashedProtectionHandler();

        Protection protection = new Protection("Pr1", "world", -1000, 422, -1000, 0,
                "owner", new Location(overworld, 0, 80, 0), false);

        protections.addNewProtection(protection);

        Set<Integer> hashes = protections.getHashes(protection);

        protections.updateBounds(new Protection("Pr1", "world", 1000, 1047, 0, 47));

        // all old hashes should be cleared

        Map<Integer, Set<Protection>> newHashes = protections.getProtectionsByLocationHash();

        for (int hash : hashes) {
            assertFalse(newHashes.containsKey(hash));
        }
        for (int hash : protections.getHashes(protection)) {
            assertTrue(newHashes.containsKey(hash));
        }


    }

    /**
     * Test hashed protection handler overlap
     */
    @Test
    public void hashedProtectionOverlap() {
        HashedProtectionHandler protections = new HashedProtectionHandler();

        Protection protection = new Protection("Pr1", "world", -200, 200, -200, 200,
                "owner", new Location(overworld, 0, 80, 0), false);

        protections.addNewProtection(protection);
        try {
            protections.addNewProtection(new Protection("Pr2", "world", 200, 400, 0, 47));
            fail();
        } catch (InvalidProtectionException e) {
        }

        try {
            protections.addNewProtection(new Protection("Pr2", "world", 0, 400, 200, 270));
            fail();
        } catch (InvalidProtectionException e) {
        }


    }

    /**
     * Test remove. All these protections are added to the same cell hash
     */
    @Test
    public void hashedProtectionRemove() {
        HashedProtectionHandler protections = new HashedProtectionHandler();

        Protection protection1 = new Protection("Pr1", "world", 1, 19, 1, 19,
                "owner", new Location(overworld, 0, 80, 0), false);
        Protection protection2 = new Protection("Pr2", "world", 20, 40, 1, 19,
                "owner", new Location(overworld, 0, 80, 0), false);
        Protection protection3 = new Protection("Pr3", "world", 1, 19, 20, 40,
                "owner", new Location(overworld, 0, 80, 0), false);
        Protection protection4 = new Protection("Pr4", "world", 20, 40, 20, 40,
                "owner", new Location(overworld, 0, 80, 0), false);

        protections.addNewProtection(protection1);
        protections.addNewProtection(protection2);
        protections.addNewProtection(protection3);
        protections.addNewProtection(protection4);

        assertEquals(1, protections.getProtectionsByLocationHash().size());

        assertEquals("Pr1", protections.getProtectionsAt(new Location(overworld, 5, 80, 5)).iterator().next().getName());
        assertEquals("Pr2", protections.getProtectionsAt(new Location(overworld, 25, 80, 5)).iterator().next().getName());
        assertEquals("Pr3", protections.getProtectionsAt(new Location(overworld, 5, 80, 25)).iterator().next().getName());
        assertEquals("Pr4", protections.getProtectionsAt(new Location(overworld, 25, 80, 25)).iterator().next().getName());

        protections.removeProtection("Pr4");
        assertEquals(1, protections.getProtectionsByLocationHash().size());

        assertEquals("Pr1", protections.getProtectionsAt(new Location(overworld, 5, 80, 5)).iterator().next().getName());
        assertEquals("Pr2", protections.getProtectionsAt(new Location(overworld, 25, 80, 5)).iterator().next().getName());
        assertEquals("Pr3", protections.getProtectionsAt(new Location(overworld, 5, 80, 25)).iterator().next().getName());
        assertTrue(protections.getProtectionsAt(new Location(overworld, 25, 80, 25)).isEmpty());

        protections.removeProtection("Pr1");
        protections.removeProtection("Pr2");
        protections.removeProtection("Pr3");

        assertEquals(0, protections.getProtectionsByLocationHash().size());

        assertTrue(protections.getProtectionsAt(new Location(overworld, 5, 80, 5)).isEmpty());
        assertTrue(protections.getProtectionsAt(new Location(overworld, 25, 80, 5)).isEmpty());
        assertTrue(protections.getProtectionsAt(new Location(overworld, 5, 80, 25)).isEmpty());
        assertTrue(protections.getProtectionsAt(new Location(overworld, 25, 80, 25)).isEmpty());
    }


    /**
     * Test that admin protections are allowed to overlap, but not with normal protections
     */
    @Test
    public void testAdminProtections() {
        HashedProtectionHandler handler = new HashedProtectionHandler();

        Protection spawn = new Protection("Spawn", "world", -10, 10, -10, 10,
                "owner", new Location(overworld, 0, 80, 0), true);
        Protection unclaimable = new Protection("Unclaimable", "world", -100, 100, -100, 100,
                "owner", new Location(overworld, 0, 80, 0), true);
        handler.addNewProtection(spawn);
        handler.addNewProtection(unclaimable);

        assertEquals("Spawn", handler.getMainProtectionAt(new Location(overworld, 0, 80, 0)).getName());
        assertEquals("Unclaimable", handler.getMainProtectionAt(new Location(overworld, 11, 80, 0)).getName());

        // test that other protections can't overlap
        Protection protection = new Protection("test", "world", 50, 100, -50, 50, "owner", new Location(overworld, 0, 80, 0), false);

        try {
            handler.addNewProtection(protection);
            fail();
        } catch (InvalidProtectionException e) {
            assertEquals(ProtectionExceptionReason.PROTECTION_OVERLAPPING, e.getReason());
        }
    }

    /**
     * Test griefing from outside to inside. Eg: water flow, piston pushing.
     */
    @Test
    public void testInsideOutsideGrief() {
        HashedProtectionHandler handler = new HashedProtectionHandler();

        Protection bigAdmin = new Protection("BigAdmin", "world", -100, 100, -100, 100,
                "owner", new Location(overworld, 0, 80, 0), true);
        Protection smallAdmin1 = new Protection("small1", "world", -10, 10, -20, 0,
                "owner", new Location(overworld, 0, 80, 0), true);
        Protection smallAdmin2 = new Protection("small2", "world", -10, 10, 1, 20,
                "owner", new Location(overworld, 0, 80, 0), true);
        Protection otherProtection = new Protection("test", "world", 101, 200, -100, 100,
                "owner", new Location(overworld, 0, 80, 0), false);

        smallAdmin1.setFlag(Flag.BORDER_FLUID_PROTECTION, true);
        smallAdmin2.setFlag(Flag.BORDER_FLUID_PROTECTION, true);
        otherProtection.setFlag(Flag.BORDER_FLUID_PROTECTION, true);
        bigAdmin.setFlag(Flag.BORDER_FLUID_PROTECTION, false);

        smallAdmin1.setFlag(Flag.BORDER_PISTON_PROTECTION, true);
        smallAdmin2.setFlag(Flag.BORDER_PISTON_PROTECTION, true);
        otherProtection.setFlag(Flag.BORDER_PISTON_PROTECTION, true);
        bigAdmin.setFlag(Flag.BORDER_PISTON_PROTECTION, true);

        smallAdmin1.setFlag(Flag.BORDER_TREE_PROTECTION, false);
        smallAdmin2.setFlag(Flag.BORDER_TREE_PROTECTION, true);
        otherProtection.setFlag(Flag.BORDER_TREE_PROTECTION, false);
        bigAdmin.setFlag(Flag.BORDER_TREE_PROTECTION, true);

        handler.addNewProtection(bigAdmin);
        handler.addNewProtection(smallAdmin1);
        handler.addNewProtection(smallAdmin2);
        handler.addNewProtection(otherProtection);

        Location small1 = new Location(overworld, 0, 80, -10);
        Location small2 = new Location(overworld, 0, 80, 10);
        Location big = new Location(overworld, -25, 80, 0);
        Location outside = new Location(overworld, -250, 80, 0);
        Location other = new Location(overworld, 150, 80, 0);

        // doublecheck the locations are the main protections
        assertEquals("small1", handler.getMainProtectionAt(small1).getName());
        assertEquals("small2", handler.getMainProtectionAt(small2).getName());
        assertEquals("BigAdmin", handler.getMainProtectionAt(big).getName());
        assertEquals("test", handler.getMainProtectionAt(other).getName());
        assertNull(handler.getMainProtectionAt(outside));

        // test fluid protection
        assertTrue(handler.testInsideOutsideProtected(small1, small2, Flag.BORDER_FLUID_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(small2, small1, Flag.BORDER_FLUID_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(small1, big, Flag.BORDER_FLUID_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(big, small1, Flag.BORDER_FLUID_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(small1, outside, Flag.BORDER_FLUID_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(outside, small1, Flag.BORDER_FLUID_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(other, big, Flag.BORDER_FLUID_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(big, other, Flag.BORDER_FLUID_PROTECTION));

        // test piston protection
        assertTrue(handler.testInsideOutsideProtected(small1, small2, Flag.BORDER_PISTON_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(small2, small1, Flag.BORDER_PISTON_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(small1, big, Flag.BORDER_PISTON_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(big, small1, Flag.BORDER_PISTON_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(big, outside, Flag.BORDER_PISTON_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(outside, big, Flag.BORDER_PISTON_PROTECTION));

        // test tree protection
        assertTrue(handler.testInsideOutsideProtected(small1, small2, Flag.BORDER_TREE_PROTECTION));
        assertFalse(handler.testInsideOutsideProtected(small2, small1, Flag.BORDER_TREE_PROTECTION));

        assertTrue(handler.testInsideOutsideProtected(small1, small2, Flag.BORDER_TREE_PROTECTION));
        assertFalse(handler.testInsideOutsideProtected(small2, small1, Flag.BORDER_TREE_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(small2, big, Flag.BORDER_TREE_PROTECTION));
        assertTrue(handler.testInsideOutsideProtected(big, small2, Flag.BORDER_TREE_PROTECTION));

        assertFalse(handler.testInsideOutsideProtected(small1, big, Flag.BORDER_TREE_PROTECTION));
        assertFalse(handler.testInsideOutsideProtected(big, small1, Flag.BORDER_TREE_PROTECTION));
    }
}
