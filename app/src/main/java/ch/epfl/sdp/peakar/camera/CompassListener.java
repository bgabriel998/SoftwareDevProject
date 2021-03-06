package ch.epfl.sdp.peakar.camera;

/**
 * Interface for the CompassListener
 */
public interface CompassListener {
    /**
     * Gets the headings in degree
     * @param heading horizontal heading
     * @param headingV vertical heading
     */
    void onNewHeading(float heading, float headingV);
}
