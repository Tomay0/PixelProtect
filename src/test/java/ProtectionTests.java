import nz.tomay0.PixelProtect.model.InvalidProtectionException;
import nz.tomay0.PixelProtect.model.Protection;
import nz.tomay0.PixelProtect.model.perms.Perm;
import nz.tomay0.PixelProtect.model.perms.PermLevel;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import sun.plugin.dom.exception.InvalidStateException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtectionTests {

    /**
     * Mock World objects, since they can't be determined without a server
     */
    @Mock
    private World overworld, nether;

    @Before
    public void create() {
        overworld = mock(World.class);
        when(overworld.getName()).thenReturn("world");

        nether = mock(World.class);
        when(nether.getName()).thenReturn("world_nether");
    }

    /**
     * Test the boundaries of a protection centred at 0,0 with boundaries at -100,-100 to 100,100
     */
    @Test
    public void testBoundaries() {
        // create protection
        Protection protection = new Protection("Protection1", "world", -100, 100, -100, 100, "owner");

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
            new Protection("Protection with spaces", "world", -100, 100, -100, 100, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // too small
        try {
            new Protection("Protection1", "world", -1, 1, -1, 1, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // west > east
        try {
            new Protection("Protection1", "world", 100, -100, -100, 100, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // north > south
        try {
            new Protection("Protection1", "world", -100, 100, 100, -100, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // boundary valid
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // boundary invalid
        try {
            new Protection("Protection1", "world", -2, 3, -2, 3, "owner");
            fail();
        } catch (InvalidProtectionException e) {
        }

        // null owner
        try {
            new Protection("Protection1", "world", -2, 3, -2, 2, null);
            fail();
        } catch (InvalidProtectionException e) {
        }

    }

    /**
     * Test default permissions
     */
    @Test
    public void testDefaultPermissions() {
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner");

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner");

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner");

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "owner");

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
        Protection protection1 = new Protection("Protection1", "world", -50, 50, -50, 50, "player1");

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

}
