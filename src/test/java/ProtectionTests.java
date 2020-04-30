import nz.tomay0.PixelProtect.model.InvalidProtectionException;
import nz.tomay0.PixelProtect.model.Protection;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import sun.plugin.dom.exception.InvalidStateException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
        Protection protection = new Protection("Protection1", "world", -100, 100, -100, 100);

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
            new Protection("Protection with spaces", "world", -100, 100, -100, 100);
            fail();
        }catch(InvalidProtectionException e) { }

        // too small
        try {
            new Protection("Protection1", "world", -1, 1, -1, 1);
            fail();
        }catch(InvalidProtectionException e) { }

        // west > east
        try {
            new Protection("Protection1", "world", 100, -100, -100, 100);
            fail();
        }catch(InvalidProtectionException e) { }

        // north > south
        try {
            new Protection("Protection1", "world", -100, 100, 100, -100);
            fail();
        }catch(InvalidProtectionException e) { }

        // boundary valid
        try {
            new Protection("Protection1", "world", -2, 2, -2, 2);
            fail();
        }catch(InvalidProtectionException e) { }

        // boundary invalid
        try {
            new Protection("Protection1", "world", -2, 3, -2, 3);
            fail();
        }catch(InvalidProtectionException e) { }

    }
}
