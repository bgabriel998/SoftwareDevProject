package ch.epfl.sdp.peakar.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.points.Point;

import static org.junit.Assert.assertEquals;

public class POIPointsUtilitiesTest {
    /**
     * Test that only the highest mountains within a horizontal bearing of 6° are filtered and that
     * the mountain in the line of sight has a priority
     */
    @Test
    public void filterHighestPOIsTest() {
        Map<POIPoint, Boolean> pois = new HashMap<>();
        Point startPoint = new Point(0,0,0);

        for(int i = 0; i < 10; i++){
            POIPoint poi = new POIPoint(""+i, (i+1)/10f, (i+1)/10f, i, null);
            poi.setHorizontalBearing(startPoint);
            poi.setVerticalBearing(startPoint);
            pois.put(poi, false);
        }

        Map<POIPoint, Boolean> filteredPois = POIPointsUtilities.filterHighestPOIs(pois);
        assertEquals(1, filteredPois.size());
        POIPoint highestPoi = (POIPoint) filteredPois.keySet().toArray()[0];
        assertEquals(9, highestPoi.getAltitude(), 0);

        //Add POIPoint that is in lineOfSight
        POIPoint poiInLineOfSight1 = new POIPoint("inLineOfSight", 5, 5, 0, null);
        POIPoint poiInLineOfSight2 = new POIPoint("inLineOfSight", 6, 6, 3, null);
        poiInLineOfSight1.setHorizontalBearing(startPoint);
        poiInLineOfSight2.setHorizontalBearing(startPoint);
        poiInLineOfSight1.setVerticalBearing(startPoint);
        poiInLineOfSight2.setVerticalBearing(startPoint);
        pois.put(poiInLineOfSight1, true);
        pois.put(poiInLineOfSight2, true);
        filteredPois = POIPointsUtilities.filterHighestPOIs(pois);
        //Check that the old highest mountain gets removed
        assertEquals(1, filteredPois.size());
        highestPoi = (POIPoint) filteredPois.keySet().toArray()[0];
        //Highest point should now be the POI in the line of sight
        assertEquals(3, highestPoi.getAltitude(), 0);

        //Add POIPoint that is outside the 6° range
        POIPoint poiOutside6 = new POIPoint("poiOutside6", 45, 38, 0, null);
        poiOutside6.setHorizontalBearing(startPoint);
        poiOutside6.setVerticalBearing(startPoint);
        pois.put(poiOutside6, false);
        filteredPois = POIPointsUtilities.filterHighestPOIs(pois);
        //Check that the size increased
        assertEquals(2, filteredPois.size());
        POIPoint newlyAdded = (POIPoint) filteredPois.keySet().toArray()[1];
        //Newly added name should be name of poiOutside6
        assertEquals("poiOutside6", newlyAdded.getName());


        //Add POIPoint that is higher
        POIPoint poiHigh = new POIPoint("poiHigh", 0.5, 0.5, 5000, null);
        poiHigh.setHorizontalBearing(startPoint);
        poiHigh.setVerticalBearing(startPoint);
        pois.put(poiHigh, false);
        filteredPois = POIPointsUtilities.filterHighestPOIs(pois);
        //Check that the size increased
        assertEquals(3, filteredPois.size());

    }
}
