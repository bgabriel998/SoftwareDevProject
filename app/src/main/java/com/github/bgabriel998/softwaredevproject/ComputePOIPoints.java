package com.github.bgabriel998.softwaredevproject;

import android.content.Context;

import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;
import java.util.List;

public class ComputePOIPoints {
    private static List<POI> POIs;
    public static List<POIPoint> POIPoints;
    public UserPoint userPoint;
    private final static double EARTH_RADIUS = 6378137; // value in meters

    /**
     * Constructor of ComputePOIPoints, updates userPoint and gets the POIs for the userPoint
     * @param context
     */
    public ComputePOIPoints(Context context){
        POIPoints = new ArrayList<>();
        userPoint = new UserPoint(context);
        userPoint.update();
        getPOIs(userPoint);
    }

    /**
     * Gets the POIs for the userPoint
     * @param userPoint location of the user
     */
    private static void getPOIs(UserPoint userPoint){
        new GeonamesHandler(userPoint) {
            @Override
            public void onResponseReceived(Object result) {
                POIs = (ArrayList<POI>) result;
                for(POI poi : POIs){
                    POIPoint poiPoint = new POIPoint(poi);
                    POIPoints.add(poiPoint);
                }
            }
        }.execute();
    }

    /**
     * Calculates the horizontal angle between the point and another point
     * Using the great circle path formula
     * @param startPoint point from where we calculate the bearing
     * @return Angle in degrees between 0 and 360
     */
    public static double getHorizontalBearing(Point startPoint, Point endPoint){
        double startLat = Math.toRadians(startPoint.getLatitude());
        double startLon = startPoint.getLongitude();
        double endLat = Math.toRadians(endPoint.getLatitude());
        double endLon = endPoint.getLongitude();

        double deltaLong = Math.toRadians(endLon - startLon);
        double y = Math.sin(deltaLong) * Math.cos(Math.toRadians(endLat));
        double x = Math.cos(startLat) * Math.sin(endLat)
                - Math.sin(startLat) * Math.cos(endLat) * Math.cos(deltaLong);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    /**
     * Calculates the vertical angle between a startpoint and endpoint
     * See https://stackoverflow.com/a/41566887
     * @param startPoint Point from where the angle is calculated
     * @param endPoint Point to where the angle is calculated
     * @return Angle in degrees between 0 and 360°, horizon is 90°
     */
    public static double getVerticalBearing(Point startPoint, Point endPoint) {
        Point fromECEF = getECEF(startPoint);
        Point toECEF = getECEF(endPoint);
        Point deltaECEF = getDeltaECEF(fromECEF, toECEF);

        double fromLat = fromECEF.getLatitude();
        double fromLon = fromECEF.getLongitude();
        double fromAlt = fromECEF.getAltitude();

        double deltaLat = deltaECEF.getLatitude();
        double deltaLon = deltaECEF.getLongitude();
        double deltaAlt = deltaECEF.getAltitude();
        
        double d = (fromLat * deltaLat + fromLon * deltaLon + fromAlt * deltaAlt);
        double a = ((fromLat * fromLat) + (fromLon * fromLon) + (fromAlt * fromAlt));
        double b = ((deltaLat * deltaLat) + (deltaAlt * deltaAlt) + (deltaAlt * deltaAlt));
        double angle = Math.toDegrees(Math.acos(d / Math.sqrt(a * b)));
        angle = (angle + 360) % 360;
        return angle;
    }

    /**
     * Computes the delta between two ECEF points
     * @param from startpoint in ECEF
     * @param to endpoint in ECEF
     * @return Point
     */
    private static Point getDeltaECEF(Point from, Point to) {
        double X = to.getLatitude() - from.getLatitude();
        double Y = to.getLongitude() - from.getLongitude();
        double Z = to.getAltitude() - from.getAltitude();

        return new Point(X, Y, Z);
    }

    /**
     * Calculates a ECEF point from a Point
     * @param point Point to be computed
     * @return New Point with ECEF coordinate system
     */
    private static Point getECEF(Point point) {
        double lat = Math.toRadians(point.getLatitude());
        double lon = Math.toRadians(point.getLongitude());
        double alt = Math.toRadians(point.getAltitude());
        
        double polarRadius = 6356752.312106893;

        double asqr = EARTH_RADIUS * EARTH_RADIUS;
        double bsqr = polarRadius * polarRadius;
        double e = Math.sqrt((asqr-bsqr)/asqr);

        double sinlatitude = Math.sin(lat);
        double denom = Math.sqrt(1 - e * e * sinlatitude * sinlatitude);
        double N = EARTH_RADIUS / denom;

        double ratio = (bsqr / asqr);

        double X = (N + alt) * Math.cos(lat) * Math.cos(lon);
        double Y = (N + alt) * Math.cos(lat) * Math.sin(lon);
        double Z = (ratio * N + alt) * Math.sin(lat);

        return new Point(X, Y, Z);
    }


    /**
     * Calculates the vertical angle between a startpoint and endpoint
     * See: http://cosinekitty.com/compass.html
     * @param startPoint Point from where the angle is calculated
     * @param endPoint Point to where the angle is calculated
     * @return Angle in degrees between 0 and 360°, horizon is 90°
     */
    public static double calculateElevationAngle(Point startPoint, Point endPoint){
        Point ap = LocationToPoint(startPoint);
        Point bp = LocationToPoint(endPoint);

        Point bma = NormalizeVectorDiff(bp, ap);

        double elevation = Math.acos(bma.getLatitude() * ap.getLatitude() +
                bma.getLongitude() * ap.getLongitude() + bma.getAltitude() * ap.getAltitude());
        return Math.toDegrees(elevation);
    }

    /**
     * Uses two points to create a new normalized Point
     * @param b first Point
     * @param a second Point
     * @return Point that is normalized
     */
    private static Point NormalizeVectorDiff(Point b, Point a){
        // Calculate norm(b-a), where norm divides a vector by its length to produce a unit vector.
        double dx = b.getLatitude() - a.getLatitude();
        double dy = b.getLongitude() - a.getLongitude();
        double dz = b.getAltitude() - a.getAltitude();
        double dist2 = dx*dx + dy*dy + dz*dz;
        if (dist2 == 0) {
            return null;
        }
        double dist = Math.sqrt(dist2);
        return new Point(dx/dist, dy/dist, dz/dist);
    }

    /**
     * Compute the location to the point
     * @param c Point that gets computed
     * @return new Point with computed location
     */
    private static Point LocationToPoint(Point c){
        // Convert (lat, lon, elv) to (x, y, z).
        double lat = c.getLatitude() * Math.PI / 180.0;
        double lon = c.getLongitude() * Math.PI / 180.0;

        double cosLon = Math.cos(lon);
        double sinLon = Math.sin(lon);

        // We used geocentric latitude to calculate (x,y,z) on the Earth's ellipsoid.
        // Now we use geodetic latitude to calculate normal vector from the surface, to correct for elevation.
        double cosGlat = Math.cos(lat);
        double sinGlat = Math.sin(lat);

        double nx = cosGlat * cosLon;
        double ny = cosGlat * sinLon;
        double nz = sinGlat;

        return new Point(nx, ny, nz);
    }
}
