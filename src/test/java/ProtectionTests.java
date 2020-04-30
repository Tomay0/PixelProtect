import nz.tomay0.PixelProtect.model.Protection;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtectionTests {

    /**
     * Mock World objects, since they can't be determined from
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
}
