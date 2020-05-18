package nz.tomay0.PixelProtect.dynmap;

import nz.tomay0.PixelProtect.exception.InvalidProtectionException;
import nz.tomay0.PixelProtect.protection.Protection;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.Objects;

/**
 * An area on the dynmap. This has an equals method to check that the state of a dynmap area has not changed
 */
public class DynmapArea {
    private static final double FILL_OPACITY = 0.35;
    private static final double EDGE_OPACITY = 0.8;
    private static final int EDGE_THICKNESS = 3;

    private double west, east, north, south;
    private int colour;
    private String name, id, world;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynmapArea that = (DynmapArea) o;
        return Double.compare(that.west, west) == 0 &&
                Double.compare(that.east, east) == 0 &&
                Double.compare(that.north, north) == 0 &&
                Double.compare(that.south, south) == 0 &&
                colour == that.colour &&
                name.equals(that.name) &&
                world.equals(that.world) &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(west, east, north, south, colour, name, world, id);
    }

    /**
     * Expected dynmap implementation from a protection
     *
     * @param protection protection
     */
    public DynmapArea(Protection protection) {
        west = protection.getWest();
        east = protection.getEast() + 1;
        north = protection.getNorth();
        south = protection.getSouth() + 1;
        colour = protection.getColour();
        name = protection.getName();
        world = protection.getWorld();
        id = protection.getIdSafeName();
    }

    /**
     * Dynmap area from dynmap API implementation
     *
     * @param area area
     */
    public DynmapArea(AreaMarker area) {
        if (area.getCornerCount() != 4) return;

        west = area.getCornerX(0);
        east = area.getCornerX(2);
        north = area.getCornerX(0);
        south = area.getCornerX(2);
        colour = area.getFillColor();
        name = area.getLabel();
        world = area.getWorld();
        id = area.getMarkerID();
    }

    /**
     * Create a new area
     */
    public void create(MarkerSet set) {
        double x[] = new double[]{west, west, east, east};
        double z[] = new double[]{north, south, south, north};

        AreaMarker area = set.createAreaMarker(id, name, false, world, x, z, true);

        area.setFillStyle(FILL_OPACITY, colour);
        area.setLineStyle(EDGE_THICKNESS, EDGE_OPACITY, colour);
    }
}
