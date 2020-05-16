import nz.tomay0.PixelProtect.protection.HashedProtectionHandler;
import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import nz.tomay0.PixelProtect.protection.SequentialProtectionHandler;
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
import java.util.Arrays;
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


    private List<Location> locationsToTest;

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

        locationsToTest = Arrays.asList(new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400),
                new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400),
                new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400),
                new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400),
                new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400),
                new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, -10, 80, -10), new Location(overworld, -10, 80, -10),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 0, 0, 0), new Location(overworld, 19, 80, 19),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 150, 80, 150), new Location(overworld, 150, 80, 150),
                new Location(overworld, 400, 80, 400), new Location(overworld, 400, 80, 400));
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

                prs.add(new Protection("pr" + i + "b" + j, "world", w, e, n, s, "owner", new Location(overworld, (w + e) / 2.0, 80, (n + s) / 2), false));
            }
        }

        return prs;
    }

    private void doTest(List<Protection> protections, List<Location> locationsToTest) {
        // normal protection creation
        ProtectionHandler sequential = new SequentialProtectionHandler(protections);
        ProtectionHandler hashed = new HashedProtectionHandler(protections);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            new SequentialProtectionHandler(protections);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to initialize sequential: " + (time / 10.0) + "ms");

        time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            new HashedProtectionHandler(protections);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to initialize hashed: " + (time / 10.0) + "ms");

        // getProtectionAt()

        time = System.currentTimeMillis();
        for (Location location : locationsToTest) {
            sequential.getProtectionsAt(location);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to get a protection Sequential: " + (time / (double) locationsToTest.size()) + "ms");

        time = System.currentTimeMillis();
        for (Location location : locationsToTest) {
            hashed.getProtectionsAt(location);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("Time to get a protection Hashed: " + (time / (double) locationsToTest.size()) + "ms");
    }


    /**
     * Test on average how long it takes to find a protection at a location when there is 1000 protections.
     */
    @Test
    public void test1000Protections() {
        List<Protection> protections = getProtectionGrid(0, 0, 20, 32);

        doTest(protections, locationsToTest);
    }

    /**
     * 100 Protections
     */
    @Test
    public void test100Protections() {
        List<Protection> protections = getProtectionGrid(0, 0, 50, 10);
        doTest(protections, locationsToTest);
    }

    /**
     * 25 Protections
     */
    @Test
    public void test25Protections() {
        List<Protection> protections = getProtectionGrid(0, 0, 200, 5);
        doTest(protections, locationsToTest);
    }
    /**
     * Giant list of protections - this takes over 4 minutes to run which is why its commented out
     */
    /*@Test
    public void test10000Protections() {
        List<Protection> protections = getProtectionGrid(-1000, -1000, 50, 100);

        doTest(protections, locationsToTest);
    }*/
}
