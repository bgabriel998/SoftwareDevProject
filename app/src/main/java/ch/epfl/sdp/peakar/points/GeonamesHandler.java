package ch.epfl.sdp.peakar.points;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpException;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.R;


public abstract class GeonamesHandler extends AsyncTask<Void,Void,ArrayList<POI>> implements Geonames {

    //Query Constants
    public static final int DEFAULT_RANGE_IN_KM = 20;
    private static final int DEFAULT_QUERY_MAX_RESULT = 300;
    private static final int DEFAULT_QUERY_TIMEOUT = 10;

    private static final int DEFAULT_NUMBER_OF_RETRY = 2;

    //List containing query POI's
    private ArrayList<POI> POIs;

    // API used to retrieve peaks POI
    private final OverpassAPIProvider poiProvider;

    private final Point userLocation;
    private final double rangeInKm;
    private final int queryMaxResults;
    private final int queryTimeout;
    private String queryUrl;
    private int retryNbr;

    /**
     * Initializes provider
     * @param userLocation Point containing user location inforamtions
     */
    public GeonamesHandler(Point userLocation, Context context) {
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");
        this.userLocation = userLocation;
        this.poiProvider = new OverpassAPIProvider();
        this.POIs = new ArrayList<POI>();

        //Retrieve the range from the shared preferences
        this.rangeInKm = getSelectedRange(context);
        this.queryMaxResults = DEFAULT_QUERY_MAX_RESULT;
        this.queryTimeout = DEFAULT_QUERY_TIMEOUT;
        this.retryNbr = 0;
    }


    /**
     * Class constructor.
     * Initialises query parameters and OverPassAPIProvider
     * Initialises result array list
     * @param userLocation user location (center of the query bounding box)
     * @param boundingBoxRangeKm range around the user location to compute the bounding box
     * @param queryMaxResults max results that the query should return (please do not exceed 500)
     * @param queryTimeout query timeout
     */
    public GeonamesHandler(Point userLocation, double boundingBoxRangeKm, int queryMaxResults, int queryTimeout){
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");
        if(boundingBoxRangeKm <= 0.1)
            throw new IllegalArgumentException("BoundingBoxRangeKm can't be null or negative (also not under 100m)");
        if(queryMaxResults < 1)
            throw new IllegalArgumentException("QueryMaxResult parameter can't be less than 1");
        if(queryTimeout <= 1)
            throw new IllegalArgumentException("QueryTimeout parameter can't be less than 1 sec");

        this.userLocation = userLocation;
        this.rangeInKm = boundingBoxRangeKm;
        this.queryMaxResults = queryMaxResults;
        this.queryTimeout = queryTimeout;
        this.poiProvider = new OverpassAPIProvider();
        this.POIs = new ArrayList<POI>();
        this.retryNbr = 0;
    }

    /**
     * Retrieve range from preferences
     * @param context application context
     * @return range in integer format
     */
    private int getSelectedRange(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedRange = sharedPreferences.getString(context.getResources().getString(R.string.range_key),
                context.getResources().getStringArray(R.array.range_values)[3]);
        int returnVal = DEFAULT_RANGE_IN_KM;
        switch (selectedRange){
            case "first_range":
                returnVal = 5;
                break;
            case "sec_range":
                returnVal = 10;
                break;
            case "third_range":
                returnVal = 20;
                break;
            case "fourth_range":
                returnVal = 30;
                break;
            case "fifth_range":
                returnVal = 50;
                break;
        }
        return returnVal;
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
     * filterPOI : filter out point of interest list. The result list contains only peaks
     * @param pois list containing all points of interests around Geopoint
    */
    @Override
    protected void onPostExecute(ArrayList<POI> pois) {
        super.onPostExecute(pois);
        if(pois != null) {
            //Filter out POI where the name or altitude is null
            POIs = ((ArrayList<POI>) pois).stream().filter(point -> point.mType != null && point.mLocation.getAltitude() != 0).collect(Collectors.toCollection(ArrayList::new));
            onResponseReceived(POIs);
        }
        else onResponseReceived(null);
    }

    /**
     * returns the query result
     * @param voids nothing
     * @return list of POI
     */
    @Override
    protected ArrayList<POI> doInBackground(Void... voids) {
        ArrayList<POI> resList = null;

        while(this.retryNbr<= DEFAULT_NUMBER_OF_RETRY){
            try {
                resList = getPOIsFromUrl(queryUrl);
                break;
            } catch (HttpException e) {
                //If the HTTP request fails retry (at most DEFAULT_NUMBER_OF_RETRY times)
                this.retryNbr++;
            }
        }
        return resList;
    }


    /**
     * Search for POI.
     * @param url full URL request, built with #urlForPOISearch or equivalent.
     * Main requirements: <br>
     * - Content must be in JSON format<br>
     * - ways and relations must contain the "center" element. <br>
     * @return elements as a list of POI
     */
    private ArrayList<POI> getPOIsFromUrl(String url) throws HttpException {
        Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:getPOIsFromUrl:"+url);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        if (jString == null) {
            Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
            throw new HttpException("OverpassAPIProvider: request failed. --> requestStringFromUrl");
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
                    poi.mDescription = tagValueFromJsonNotNull("natural", jTags);
                    //remove first "," (quite ugly, I know)
                    if (poi.mDescription.length()>0)
                        poi.mDescription = poi.mDescription.substring(1);
                    //We could try to replicate Nominatim/lib/lib.php/getClassTypes(), but it sounds crazy for the added value.
                    poi.mUrl = tagValueFromJson("website", jTags);

                }
                if ("node".equals(poi.mCategory)){
                    poi.mLocation = geoPointFromJson(jo);
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

    /**
     * Extract value identified by key of JSON
     * @param key key tag for value extraction
     * @param jTags JSON object containing tags
     * @return Value
     */
    private String tagValueFromJson(String key, JsonObject jTags){
        JsonElement jTag = jTags.get(key);
        if (jTag == null)
            return null;
        else
            return jTag.getAsString();
    }

    /**
     * Extract value identified by key of JSON with null handling
     * @param key key tag for value extraction
     * @param jTags JSON object containing tags
     * @return Value
     */
    private String tagValueFromJsonNotNull(String key, JsonObject jTags){
        String v = tagValueFromJson(key, jTags);
        return (v != null ? ","+v : "");
    }

    /**
     * Create location Geopoint ot of Json
     * @param jLatLon JSON object containing lat and long
     * @return Location of POI
     */
    private GeoPoint geoPointFromJson(JsonObject jLatLon){
        double lat = jLatLon.get("lat").getAsDouble();
        double lon = jLatLon.get("lon").getAsDouble();
        String eleStr = (tagValueFromJsonNotNull("ele",jLatLon.get("tags").getAsJsonObject())).replace(",","");
        if(eleStr.isEmpty())
            return new GeoPoint(lat, lon);
        double alt = Double.parseDouble(eleStr);
        return new GeoPoint(lat, lon,alt);
    }

    /**
     * Callback function called when POI list is received
     * @param result ArrayList containing POI
     */
    public abstract void onResponseReceived(ArrayList<POI> result);
}
