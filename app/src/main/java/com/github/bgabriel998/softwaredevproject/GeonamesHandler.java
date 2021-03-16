package com.github.bgabriel998.softwaredevproject;


import android.os.AsyncTask;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.UserPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.stream.Collectors;



public abstract class GeonamesHandler extends AsyncTask<Object,Void,Object> implements GeonamesHandlerIF{

    //Query Constants
    private static final int DEFAULT_RANGE_IN_KM = 20;
    private static final int DEFAULT_QUERY_MAX_RESULT = 300;
    private static final int DEFAULT_QUERY_TIMEOUT = 10;

    //List containing query POI's
    private ArrayList<POI> POIs;

    // API used to retrieve peaks POI
    private final OverpassAPIProvider poiProvider;

    private final UserPoint userLocation;
    private final double rangeInKm;
    private final int queryMaxResults;
    private final int queryTimeout;
    private String queryUrl;

    /**
     * Initializes provider
     */
    public GeonamesHandler(UserPoint userLocation) {
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");
        this.userLocation = userLocation;
        poiProvider = new OverpassAPIProvider();
        POIs = new ArrayList<POI>();
        rangeInKm = DEFAULT_RANGE_IN_KM;
        queryMaxResults = DEFAULT_QUERY_MAX_RESULT;
        queryTimeout = DEFAULT_QUERY_TIMEOUT;
    }

    /**
     * Class constructor.
     * Initialises query parameters and OverPassAPIProvider
     * Initialises result array list
     * @param userLocation user location (center of the query bounding box)
     * @param boundingBoxRangeKm range around the user location to compute the bounding box
     * @param queryMaxResults max results that the query should return (do not exceed 500)
     * @param queryTimeout query timeout
     */
    public GeonamesHandler(UserPoint userLocation, double boundingBoxRangeKm, int queryMaxResults, int queryTimeout){
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");

        this.userLocation = userLocation;
        this.rangeInKm = boundingBoxRangeKm;
        this.queryMaxResults = queryMaxResults;
        this.queryTimeout = queryTimeout;
        poiProvider = new OverpassAPIProvider();
        POIs = new ArrayList<POI>();
    }



    /**
     * onPreExecute method.
     * Setup bounding box for the POI query
     * Creates the url query
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BoundingBox boundingBox = userLocation.computeBoundingBox(rangeInKm);
        queryUrl = poiProvider.urlForTagSearchKml("natural=peak", boundingBox,queryMaxResults,queryTimeout);
    }

    /**
     *
     * @param o Object (not used)
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if(o != null) {
            //Filter out POI where the name or altitude is null
            POIs = ((ArrayList<POI>) o).stream().filter(point -> point.mType != null && point.mLocation.getAltitude() != 0).collect(Collectors.toCollection(ArrayList::new));
            onResponseReceived(POIs);
        }
        else onResponseReceived(null);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return getPOIsFromUrl(queryUrl);
    }


    private ArrayList<POI> getPOIsFromUrl(String url){
        Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:getPOIsFromUrl:"+url);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        if (jString == null) {
            Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
            return null;
        }
        try {
            //parse JSON and build POIs
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(jString);
            JsonObject jResult = json.getAsJsonObject();
            JsonArray jElements = jResult.get("elements").getAsJsonArray();
            ArrayList<POI> pois = new ArrayList<POI>(jElements.size());
            for (JsonElement j:jElements){
                JsonObject jo = j.getAsJsonObject();
                POI poi = new POI(POI.POI_SERVICE_OVERPASS_API);
                poi.mId = jo.get("id").getAsLong();
                poi.mCategory = jo.get("type").getAsString();
                if (jo.has("tags")){
                    JsonObject jTags = jo.get("tags").getAsJsonObject();
                    poi.mType = tagValueFromJson("name", jTags);
                    //Try to set a relevant POI type by searching for an OSM commonly used tag key, and getting its value:
                    poi.mDescription = tagValueFromJsonNotNull("amenity", jTags)
                            + tagValueFromJsonNotNull("boundary", jTags)
                            + tagValueFromJsonNotNull("building", jTags)
                            + tagValueFromJsonNotNull("craft", jTags)
                            + tagValueFromJsonNotNull("emergency", jTags)
                            + tagValueFromJsonNotNull("highway", jTags)
                            + tagValueFromJsonNotNull("historic", jTags)
                            + tagValueFromJsonNotNull("landuse", jTags)
                            + tagValueFromJsonNotNull("leisure", jTags)
                            + tagValueFromJsonNotNull("natural", jTags)
                            + tagValueFromJsonNotNull("shop", jTags)
                            + tagValueFromJsonNotNull("sport", jTags)
                            + tagValueFromJsonNotNull("tourism", jTags);
                    //remove first "," (quite ugly, I know)
                    if (poi.mDescription.length()>0)
                        poi.mDescription = poi.mDescription.substring(1);
                    //TODO: try to set a relevant thumbnail image, according to key/value tags.
                    //We could try to replicate Nominatim/lib/lib.php/getClassTypes(), but it sounds crazy for the added value.
                    poi.mUrl = tagValueFromJson("website", jTags);
                    if (poi.mUrl != null){
                        //normalize the url (often needed):
                        if (!poi.mUrl.startsWith("http://") && !poi.mUrl.startsWith("https://"))
                            poi.mUrl = "http://" + poi.mUrl;
                    }
                }
                if ("node".equals(poi.mCategory)){
                    poi.mLocation = geoPointFromJson(jo);
                } else {
                    if (jo.has("center")){
                        JsonObject jCenter = jo.get("center").getAsJsonObject();
                        poi.mLocation = geoPointFromJson(jCenter);
                    }
                }
                if (poi.mLocation != null)
                    pois.add(poi);
            }
            return pois;
        } catch (JsonSyntaxException e) {
            Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: parsing error.");
            return null;
        }
    }

    private String tagValueFromJson(String key, JsonObject jTags){
        JsonElement jTag = jTags.get(key);
        if (jTag == null)
            return null;
        else
            return jTag.getAsString();
    }

    private String tagValueFromJsonNotNull(String key, JsonObject jTags){
        String v = tagValueFromJson(key, jTags);
        return (v != null ? ","+v : "");
    }

    private GeoPoint geoPointFromJson(JsonObject jLatLon){
        double lat = jLatLon.get("lat").getAsDouble();
        double lon = jLatLon.get("lon").getAsDouble();
        String eleStr = (tagValueFromJsonNotNull("ele",jLatLon.get("tags").getAsJsonObject())).replace(",","");
        if(eleStr.isEmpty())
            return new GeoPoint(lat, lon);
        double alt = Double.parseDouble(eleStr);
        return new GeoPoint(lat, lon,alt);
    }

    public abstract void onResponseReceived(Object result);
}
