package ch.epfl.sdp.peakar.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CameraUtilitiesTest {
    /**
     * Check if the correct orientation string is returned
     */
    @Test
    public void getCorrectOrientationString(){
        assertEquals(CameraUtilities.selectHeadingString(0), "N");
        assertEquals(CameraUtilities.selectHeadingString(45), "NE");
        assertEquals(CameraUtilities.selectHeadingString(90), "E");
        assertEquals(CameraUtilities.selectHeadingString(135), "SE");
        assertEquals(CameraUtilities.selectHeadingString(180), "S");
        assertEquals(CameraUtilities.selectHeadingString(225), "SW");
        assertEquals(CameraUtilities.selectHeadingString(270), "W");
        assertEquals(CameraUtilities.selectHeadingString(315), "NW");
        assertEquals(CameraUtilities.selectHeadingString(360), "N");
        assertEquals(CameraUtilities.selectHeadingString(1), "");
    }
}
