import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test performance related to algorithms with finding protections.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class PerformanceTests {
    @Mock
    private World overworld, nether;

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

        // mock Bukkit.getWorld() call
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getWorld("world")).thenReturn(overworld);
        when(Bukkit.getWorld("world_nether")).thenReturn(nether);
    }

    /**
     * Create a grid of protections
     *
     * @param west    west
     * @param north   north
     * @param size    size
     * @param numRoot num root
     * @return
     */
    private List<Protection> getProtectionGrid(int west, int north, int size, int numRoot) {
        List<Protection> prs = new ArrayList<>();
        for (int i = 0; i < numRoot; i++) {
            for (int j = 0; j < numRoot; j++) {
                int w = west + i * size;
                int e = w + size - 1;
                int n = north + j * size;
                int s = n + size - 1;

                prs.add(new Protection("pr" + i + "b" + j, "world", w, e, n, s, "owner", new Location(overworld, (w + e) / 2.0, 80, (n + s) / 2)));
            }
        }

        return prs;
    }


    /**
     * Test on average how long it takes to find a protection at a location when there is 1000 protections.
     */
    @Test
    public void test1000ProtectionsNormal() {
        List<Protection> protections = getProtectionGrid(0,0,10, 32);

        // normal protection creation
        ProtectionHandler handler = new ProtectionHandler(protections);

        long time = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            new ProtectionHandler(protections);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to initialize: " + (time/10.0) + "ms");

        // getProtectionAt()
        Location location = new Location(overworld, -10, 80, -10);

        time = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            handler.getProtectionAt(location);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to get a protection: " + (time/10.0) + "ms");
    }
}
