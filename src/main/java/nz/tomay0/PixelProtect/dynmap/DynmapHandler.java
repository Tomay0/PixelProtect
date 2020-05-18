package nz.tomay0.PixelProtect.dynmap;

import nz.tomay0.PixelProtect.protection.Protection;
import nz.tomay0.PixelProtect.protection.ProtectionHandler;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles the creating and removing of protections on the dynmap
 */
public class DynmapHandler {
    private static DynmapHandler instance;

    /**
     * Get the dynmap handler instance
     *
     * @return
     */
    public static DynmapHandler getInstance() {
        if (instance == null)
            instance = new DynmapHandler(null);

        return instance;
    }

    /**
     * Create a new instance of the dynmap handler with an API
     *
     * @param api
     */
    public static void createInstance(DynmapAPI api) {
        instance = new DynmapHandler(api);
    }

    private DynmapAPI api;

    /**
     * Create dynmap handler
     *
     * @param api
     */
    private DynmapHandler(DynmapAPI api) {
        this.api = api;
    }


    /**
     * Gets the pixel protect marker set. If this does not exist it will create it
     *
     * @return null if no api defined, otherwise the marker set
     */
    private MarkerSet getMarkerSet() {
        if (api == null) return null;
        MarkerAPI markerAPI = api.getMarkerAPI();

        // check if there already is a marker set
        MarkerSet set = markerAPI.getMarkerSet("pixelprotect.protections");

        if (set == null) {
            // create new marker set
            set = markerAPI.createMarkerSet("pixelprotect.protections", "Protections", null, true);
        }

        return set;
    }

    /**
     * Verify the dynmap corresponds to the list of protections by deleting all invalid regions
     *
     * @param protections
     */
    public void deleteInvalidRegions(ProtectionHandler protections) {
        MarkerSet markers = getMarkerSet();
        if (markers == null) return;

        // get existing regions from dynmap
        Map<String, AreaMarker> areas = new HashMap<>();

        for (AreaMarker area : markers.getAreaMarkers()) {
            areas.put(area.getMarkerID(), area);
        }

        // remove protections that already exist from the map
        for (Protection protection : protections) {
            areas.remove(protection.getIdSafeName());
        }

        // remaining areas are areas that are areas that don't exist. Remove these.
        for (AreaMarker area : areas.values()) {
            area.deleteMarker();
        }
    }

    /**
     * Create a new area if it doesn't already exist
     */
    public void create(Protection protection) {
        MarkerSet markers = getMarkerSet();
        if (markers == null) return;

        String id = protection.getIdSafeName();
        AreaMarker area = markers.findAreaMarker(id);

        if (area != null) {
            DynmapArea actualArea = new DynmapArea(area);
            DynmapArea expectedArea = new DynmapArea(protection);

            // update if the bounds are incorrect
            if (!actualArea.equals(expectedArea)) {
                area.deleteMarker();
                expectedArea.create(markers);
            }
        } else {
            // create new area
            new DynmapArea(protection).create(markers);
        }
    }

    /**
     * Delete the dynmap area and recreate it, such as a rename or an action that could change the world
     *
     * @param id         id of the old protection to delete
     * @param protection new protection
     */
    public void deleteAndCreate(String id, Protection protection) {
        delete(id);
        create(protection);
    }

    /**
     * Delete a dynmap area
     *
     * @param id
     */
    public void delete(String id) {
        MarkerSet markers = getMarkerSet();
        if (markers == null) return;

        AreaMarker area = markers.findAreaMarker(id);

        if (area != null) area.deleteMarker();
    }
}
