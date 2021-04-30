package ch.epfl.sdp.peakar.points;

import androidx.core.util.Pair;

import org.osmdroid.util.BoundingBox;

/**
 * Point is a class that represents a general point on earth.
 * A point is described by three components:
 * <ul>
 * <li>Latitude
 * <li>Longitude
 * <li>Altitude
 * </ul>
 * <p>
 * This class contains a method to compute distance in meters between two points.
 */
public class Point {

    final static double EARTH_RADIUS = 6378137; // value in meters
    private final double POLAR_RADIUS_EARTH = 6356752.3; //in meters
    private final double ECCENTRICITY_SQUARED = 0.00669437999014; //in meters
    private final double ADJUST_COORDINATES = 0.008983112; // 1km in degrees at equator.

    protected double latitude;
    protected double longitude;

    protected double altitude;

    private double horizontalBearing;
    private double verticalBearing;

    /**
     * Constructor for the Point class.
     *
     * @param latitude  latitude of the point (in degrees)
     * @param longitude longitude of the point (in degrees)
     * @param altitude  altitude of the point (in meters)
     */
    public Point(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Method for computing distance between two point as the crow flies.
     *
     * @param other the other point to compute the distance
     * @return      a value in meters representing the distance
     */
    public double computeDistance(Point other) {

        double rThis = EARTH_RADIUS + this.altitude;
        double rOther = EARTH_RADIUS + other.altitude;

        return computeSphericalDistance(other, rThis, rOther);

    }

    /**
     * Method for computing distance between two point as the crow flies,
     * without taking in account the altitude of the two points
     *
     * @param other the other point to compute the distance
     * @return      a value in meters representing the distance
     */
    public double computeFlatDistance(Point other) {
        return computeSphericalDistance(other, EARTH_RADIUS, EARTH_RADIUS);
    }

    /**
     * Given the radious of the two points, it computes the distance in spherical coordinates
     *
     * @param other     the other point to compute the distance
     * @param rThis     the radius of this point
     * @param rOther    the radius of the other point
     * @return          a value in meters representing the distance
     */
    private double computeSphericalDistance(Point other, double rThis, double rOther) {

        double squaredDistance;

        double latThis = Math.toRadians(this.latitude);
        double lonThis = Math.toRadians(this.longitude);

        double latOther = Math.toRadians(other.latitude);
        double lonOther = Math.toRadians(other.longitude);

        // computing distance in spherical polar coordinates
        squaredDistance = Math.pow(rThis, 2) + Math.pow(rOther, 2) -
                2*rThis*rOther*(
                        Math.cos(latThis)*Math.cos(latOther)*Math.cos(lonThis - lonOther) +
                                Math.sin(latThis)*Math.sin(latOther)
                );

        return Math.sqrt(squaredDistance);

    }

    /**
     *
     * @param latitude  latitude to set (in degrees)
     */
    public void setLatitude(double latitude) { this.latitude = latitude; }

    /**
     *
     * @param longitude longitude to set (in degrees)
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @param altitude altitude to set (in meters)
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     *
     * @return point latitude (in degrees)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @return point longitude (in degrees)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     *
     * @return point altitude (in meters)
     */
    public double getAltitude() {
        return altitude;
    }


    /**
     * Gets the horizontal bearing
     * @return horizontal bearing in degrees
     */
    public double getHorizontalBearing() {
        return horizontalBearing;
    }

    /**
     * Computes the horizontal bearing from a given startpoint
     * @param startPoint point from where the bearing is calculated
     * @return new calculated bearing in degrees
     */
    public double setHorizontalBearing(Point startPoint) {
        this.horizontalBearing = computeHorizontalBearing(startPoint);
        return horizontalBearing;
    }

    /**
     * Gets the vertical bearing
     * @return vertical bearing in degrees
     */
    public double getVerticalBearing() {
        return verticalBearing;
    }

    /**
     * Computes the vertical bearing from a given startpoint
     * @param startPoint point from where the bearing is calculated
     * @return new calculated bearing in degrees
     */
    public double setVerticalBearing(Point startPoint) {
        this.verticalBearing = computeVerticalBearing(startPoint);
        return verticalBearing;
    }

    /**
     * Calculates the horizontal angle between the point and another point
     * Using the great circle path formula
     * @param startPoint point from where we calculate the bearing
     * @return Angle in degrees between 0 and 360
     */
    private double computeHorizontalBearing(Point startPoint){
        double startLat = Math.toRadians(startPoint.getLatitude());
        double startLon = startPoint.getLongitude();
        double endLat = Math.toRadians(this.getLatitude());
        double endLon = this.getLongitude();

        double deltaLong = Math.toRadians(endLon - startLon);
        double y = Math.sin(deltaLong) * Math.cos(Math.toRadians(endLat));
        double x = Math.cos(startLat) * Math.sin(endLat)
                - Math.sin(startLat) * Math.cos(endLat) * Math.cos(deltaLong);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    /**
     * Calculates the vertical angle between a startpoint and the POIPoint
     * See: http://cosinekitty.com/compass.html
     * @param startPoint Point from where the angle is calculated
     * @return Angle in degrees between 0° (mountain exactly below) and
     * 180° (mountain exactly above), horizon is 90°
     */
    private double computeVerticalBearing(Point startPoint){
        Pair<Point, Point> ap = locationToPoint(startPoint);
        Pair<Point, Point> bp = locationToPoint(this);
        Point apGeocentric = ap.first;
        Point apGeodetic = ap.second;
        Point bpGeocentric = bp.first;

        assert bpGeocentric != null;
        assert apGeocentric != null;
        Point bma = normalizeVectorDiff(bpGeocentric, apGeocentric);

        assert apGeodetic != null;
        double delta = bma.getLatitude() * apGeodetic.getLatitude() +
                bma.getLongitude() * apGeodetic.getLongitude() + bma.getAltitude() * apGeodetic.getAltitude();

        double elevation = Math.acos(-1*delta);
        return Math.toDegrees(elevation);
    }

    /**
     * Uses two points to create a new normalized Point
     * @param b first Point
     * @param a second Point
     * @return Point that is normalized
     */
    private Point normalizeVectorDiff(Point b, Point a){
        // Calculate norm(b-a), where norm divides a vector by its length to produce a unit vector.
        double dx = b.getLatitude() - a.getLatitude();
        double dy = b.getLongitude() - a.getLongitude();
        double dz = b.getAltitude() - a.getAltitude();
        double dist2 = dx*dx + dy*dy + dz*dz;
        double dist = Math.sqrt(dist2);
        return new Point(dx/dist, dy/dist, dz/dist);
    }

    /**
     * Converts a Point of geodetic coordinate system to a geocentric coordinate system
     * @param c Point that gets computed
     * @return Pair<Point, Point></> the first point represents the geocentric point and the second
     * Point the normal vector from the surface.
     */
    private Pair<Point, Point> locationToPoint(Point c){
        // Convert (lat, lon, elv) to (x, y, z).
        double lat = c.getLatitude() * Math.PI / 180.0;
        double lon = c.getLongitude() * Math.PI / 180.0;
        double elv = c.getAltitude();

        double radius = earthRadiusInMeters(lat);
        double clat   = geocentricLatitude(lat);

        double cosLon = Math.cos(lon);
        double sinLon = Math.sin(lon);

        double cosLat = Math.cos(clat);
        double sinLat = Math.sin(clat);
        double x = radius * cosLon * cosLat;
        double y = radius * sinLon * cosLat;
        double z = radius * sinLat;

        // We used geocentric latitude to calculate (x,y,z) on the Earth's ellipsoid.
        // Now we use geodetic latitude to calculate normal vector from the surface, to correct for elevation.
        double cosGlat = Math.cos(lat);
        double sinGlat = Math.sin(lat);

        double nx = cosGlat * cosLon;
        double ny = cosGlat * sinLon;
        double nz = sinGlat;

        x += elv * nx;
        y += elv * ny;
        z += elv * nz;

        return new Pair<>(new Point(x, y, z), new Point(nx, ny, nz));
    }

    /**
     * Calculates the earth radius given the latitude
     * See http://en.wikipedia.org/wiki/Earth_radius
     * @param latitudeRadians latitude in radians (geodetic)
     * @return Radius of the earth in meters
     */
    private double earthRadiusInMeters(double latitudeRadians){
        double cos = Math.cos (latitudeRadians);
        double sin = Math.sin (latitudeRadians);
        double t1 = EARTH_RADIUS * EARTH_RADIUS * cos;
        double t2 = POLAR_RADIUS_EARTH * POLAR_RADIUS_EARTH * sin;
        double t3 = EARTH_RADIUS * cos;
        double t4 = POLAR_RADIUS_EARTH * sin;
        return Math.sqrt ((t1*t1 + t2*t2) / (t3*t3 + t4*t4));
    }

    /**
     * Converts a geodetic latitude (like GPS values) to a geocentric latitude (angle measured
     * from center of Earth between a point and the equator).
     * See https://en.wikipedia.org/wiki/Latitude#Geocentric_latitude
     * @param lat Geodetic latitude in radian
     * @return Converted latitude in radian to geocentric latitude
     */
    private double geocentricLatitude(double lat){
        return Math.atan((1.0 - ECCENTRICITY_SQUARED) * Math.tan(lat));
    }

    /**
     * Computes bounding box around
     * @param rangeInKm range around user point to compute the bounding box (in km)
     * @return Bounding box around point
     */
    public BoundingBox computeBoundingBox(double rangeInKm){
        double north = latitude + ( rangeInKm * ADJUST_COORDINATES);
        double south = latitude - ( rangeInKm * ADJUST_COORDINATES);
        double lngRatio = 1/Math.cos(Math.toRadians(latitude));
        double east = longitude + (rangeInKm * ADJUST_COORDINATES) * lngRatio;
        double west = longitude - (rangeInKm * ADJUST_COORDINATES) * lngRatio;
        return new BoundingBox(north,east,south,west);
    }

}
