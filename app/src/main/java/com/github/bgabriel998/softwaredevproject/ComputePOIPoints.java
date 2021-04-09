package com.github.bgabriel998.softwaredevproject;

import android.content.Context;

import androidx.core.util.Pair;

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

    /**
     * Constructor of ComputePOIPoints, updates userPoint and gets the POIs for the userPoint
     * @param context Context of activity
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
                if(POIs != null){
                    for(POI poi : POIs){
                        POIPoint poiPoint = new POIPoint(poi);
                        if(!POIPoints.contains(poiPoint)){
                            POIPoints.add(poiPoint);
                        }
                    }
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
     * See: http://cosinekitty.com/compass.html
     * @param startPoint Point from where the angle is calculated
     * @param endPoint Point to where the angle is calculated
     * @return Angle in degrees between 0° (mountain exactly below) and
     * 180° (mountain exactly above), horizon is 90°
     */
    public static double getVerticalBearing(Point startPoint, Point endPoint){
        Pair<Point, Point> ap = locationToPoint(startPoint);
        Pair<Point, Point> bp = locationToPoint(endPoint);
        Point apGeocentric = ap.first;
        Point apGeodetic = ap.second;
        Point bpGeocentric = bp.first;

        Point bma = normalizeVectorDiff(bpGeocentric, apGeocentric);

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
    private static Point normalizeVectorDiff(Point b, Point a){
        // Calculate norm(b-a), where norm divides a vector by its length to produce a unit vector.
        double dx = b.getLatitude() - a.getLatitude();
        double dy = b.getLongitude() - a.getLongitude();
        double dz = b.getAltitude() - a.getAltitude();
        double dist2 = dx*dx + dy*dy + dz*dz;
        double dist = Math.sqrt(dist2);
        return new Point(dx/dist, dy/dist, dz/dist);
    }

    /**
     * Compute the location to the point
     * @param c Point that gets computed
     * @return new Point with computed location
     */
    private static Pair<Point, Point> locationToPoint(Point c){
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
    private static double earthRadiusInMeters(double latitudeRadians){
        //
        double a = 6378137.0;  // equatorial radius in meters
        double b = 6356752.3;  // polar radius in meters
        double cos = Math.cos (latitudeRadians);
        double sin = Math.sin (latitudeRadians);
        double t1 = a * a * cos;
        double t2 = b * b * sin;
        double t3 = a * cos;
        double t4 = b * sin;
        return Math.sqrt ((t1*t1 + t2*t2) / (t3*t3 + t4*t4));
    }

    /**
     * Converts a geodetic latitude (like GPS values) to a geocentric latitude (angle measured
     * from center of Earth between a point and the equator).
     * See https://en.wikipedia.org/wiki/Latitude#Geocentric_latitude
     * @param lat Geodetic latitude in radian
     * @return Converted latitude in radian to geocentric latitude
     */
    private static double geocentricLatitude(double lat){
        double e2 = 0.00669437999014;
        return Math.atan((1.0 - e2) * Math.tan(lat));
    }
}
