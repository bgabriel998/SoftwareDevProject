package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.text.PrecomputedText;

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

    public ComputePOIPoints(Context context){
        POIPoints = new ArrayList<>();
        userPoint = new UserPoint(context);
        getPOIs(userPoint);
    }

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
     * Calculates the vertical angle between the point and another point
     * @param startPoint StartPoint from where the distance is calculated
     * @return Angle in degrees between 0 and 360
     */
//    public double getVerticalAngleToPoint(Point startPoint, Point endPoint){
//        double startLat = Math.toRadians(startPoint.getLatitude());
//        double startLon = Math.toRadians(startPoint.getLongitude());
//        double startAlt = startPoint.getAltitude();
//        double endLat = Math.toRadians(endPoint.getLatitude());
//        double endLon = Math.toRadians(endPoint.getLongitude());
//        double endAlt = endPoint.getAltitude();
//
//        double distance = startPoint.computeDistance(this);
//        double dropInHeight = distance / 2*EARTH_RADIUS;
//        double elevationAngle = (this.altitude - startPoint.altitude)/distance;
//        //Add offset of 90°
//        double angleRadians = elevationAngle - dropInHeight;
//        double angleDegree  = Math.toDegrees(angleRadians);
//        double angle = (angleDegree + 360 + 90) % 360;
//        return angle;
//    }



    // Horizon is 0 degree, Up is 90 degree

    /**
     * Calculates the vertical angle between a startpoint and endpoint
     * @param startPoint Point from where the angle is calculated
     * @param endPoint Point to where the angle is calculated
     * @return Angle in degrees between 0 and 360°, horizon is 90°
     */
    public static double getVerticalBearing(Point startPoint, Point endPoint) {
        double startLat = Math.toRadians(startPoint.getLatitude());
        double startLon = Math.toRadians(startPoint.getLongitude());
        double startAlt = startPoint.getAltitude();
        double endLat = Math.toRadians(endPoint.getLatitude());
        double endLon = Math.toRadians(endPoint.getLongitude());
        double endAlt = endPoint.getAltitude();

        double[] fromECEF = getECEF(startLat, startLon, startAlt);
        double[] toECEF = getECEF(endLat, endLon, endAlt);
        double[] deltaECEF = getDeltaECEF(fromECEF, toECEF);

        double d = (fromECEF[0] * deltaECEF[0] + fromECEF[1] * deltaECEF[1] + fromECEF[2] * deltaECEF[2]);
        double a = ((fromECEF[0] * fromECEF[0]) + (fromECEF[1] * fromECEF[1]) + (fromECEF[2] * fromECEF[2]));
        double b = ((deltaECEF[0] * deltaECEF[0]) + (deltaECEF[2] * deltaECEF[2]) + (deltaECEF[2] * deltaECEF[2]));
        double angle = Math.toDegrees(Math.acos(d / Math.sqrt(a * b)));
        angle = (angle + 360) % 360;

        return angle;
    }

    private static double[] getDeltaECEF(double[] from, double[] to) {
        double X = to[0] - from[0];
        double Y = to[1] - from[1];
        double Z = to[2] - from[2];

        return new double[]{X, Y, Z};
    }

    private static double[] getECEF(double lat, double lon, double alt) {
        double polarRadius = 6356752.312106893;

        double asqr = EARTH_RADIUS * EARTH_RADIUS;
        double bsqr = polarRadius * polarRadius;
        double e = Math.sqrt((asqr-bsqr)/asqr);

        double N = getN(EARTH_RADIUS, e, lat);
        double ratio = (bsqr / asqr);

        double X = (N + alt) * Math.cos(lat) * Math.cos(lon);
        double Y = (N + alt) * Math.cos(lat) * Math.sin(lon);
        double Z = (ratio * N + alt) * Math.sin(lat);

        return new double[]{X, Y, Z};
    }

    private static double getN(double a, double e, double lat) {
        double sinlatitude = Math.sin(lat);
        double denom = Math.sqrt(1 - e * e * sinlatitude * sinlatitude);
        return a / denom;
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

        double elevation = (180.0 / Math.PI)*Math.acos(bma.getLatitude() * ap.getLatitude() +
                bma.getLongitude() * ap.getLongitude() + bma.getAltitude() * ap.getAltitude());
        return elevation;
    }

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
