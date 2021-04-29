package com.github.bgabriel998.softwaredevproject.points;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ComputePOIPointsUnitTest {

    /**
     * Test that only the highest mountains within a horizontal bearing of 6° are filtered and that
     * the mountain in the line of sight has a priority
     */
    @Test
    public void filterHighestPOIsTest() {
        Map<POIPoint, Boolean> pois = new HashMap<>();
        Point startPoint = new Point(0,0,0);

        for(int i = 0; i < 10; i++){
            POIPoint poi = new POIPoint(""+i, i+1, i+1, i);
            poi.setHorizontalBearing(startPoint);
            pois.put(poi, false);
        }

        Map<POIPoint, Boolean> filteredPois = ComputePOIPoints.filterHighestPOIs(pois);
        assertEquals(1, filteredPois.size());
        POIPoint highestPoi = (POIPoint) filteredPois.keySet().toArray()[0];
        assertEquals(9, highestPoi.altitude, 0);

        //Add POIPoint that is in lineOfSight
        POIPoint poiInLineOfSight1 = new POIPoint("inLineOfSight", 5, 5, 0);
        POIPoint poiInLineOfSight2 = new POIPoint("inLineOfSight", 6, 6, 3);
        poiInLineOfSight1.setHorizontalBearing(startPoint);
        poiInLineOfSight2.setHorizontalBearing(startPoint);
        pois.put(poiInLineOfSight1, true);
        pois.put(poiInLineOfSight2, true);
        filteredPois = ComputePOIPoints.filterHighestPOIs(pois);
        //Check that the old highest mountain gets removed
        assertEquals(1, filteredPois.size());
        highestPoi = (POIPoint) filteredPois.keySet().toArray()[0];
        //Highest point should now be the POI in the line of sight
        assertEquals(3, highestPoi.altitude, 0);

        //Add POIPoint that is outside the 6° range
        POIPoint poiOutside6 = new POIPoint("poiOutside6", 45, 38, 0);
        poiOutside6.setHorizontalBearing(startPoint);
        pois.put(poiOutside6, false);
        filteredPois = ComputePOIPoints.filterHighestPOIs(pois);
        //Check that the size increased
        assertEquals(2, filteredPois.size());
        POIPoint newlyAdded = (POIPoint) filteredPois.keySet().toArray()[1];
        //Newly added name should be name of poiOutside6
        assertEquals("poiOutside6", newlyAdded.getName());
    }
}
