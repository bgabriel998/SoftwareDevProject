package ch.epfl.sdp.peakar;

public class TestingConstants {
    //User constants
    public final static String BASIC_USERNAME = "test";
    public final static String USERNAME_CAMERA = "abcd";

    public final static int SHORT_SLEEP_TIME = 500;
    public final static int THREAD_SLEEP_1S = 1000;
    public final static int LONG_SLEEP_TIME = 1500;
    public final static int THREAD_SLEEP_6S = 6000;
    public final static int USER_SCORE = 200000;

    //Map constant
    public static final float TILE_SCALING_FACTOR = 1.5f;

    //Constants POI names
    public static final String MONT_BLANC_NAME = "Mont Blanc - Monte Bianco";
    public static final String DENT_DU_GEANT_NAME = "Dent du Geant";
    public static final String POINTE_DE_LAPAZ_NAME = "Pointe de Lapaz";
    public static final String AIGUILLE_DU_PLAN_NAME = "Aiguille du Plan";
    //Constants POI coordinates
    public static final double MONT_BLANC_LAT = 45.8325;
    public static final double MONT_BLANC_LONG = 6.8641666666667;
    public static final double MONT_BLANC_ALT = 4810;

    public static final double DENT_DU_GEANT_LAT = 45.86355980599387;
    public static final double DENT_DU_GEANT_LONG = 6.951348205683087;
    public static final double DENT_DU_GEANT_ALT = 4013;

    public static final double AIGUILLE_DU_PLAN_LAT = 45.891667;
    public static final double AIGUILLE_DU_PLAN_LONG = 6.907222;
    public static final double AIGUILLE_DU_PLAN_ALT = 3673;

    public static final double POINTE_DE_LAPAZ_LAT = 45.920774986207014;
    public static final double POINTE_DE_LAPAZ_LONG = 6.812914656881065;
    public static final double POINTE_DE_LAPAZ_ALT = 3660;

    //Geonames Handler testing constants
    public static final int MILLI_SEC_TO_SEC = 1000;
    public static final int DEFAULT_QUERY_MAX_RESULT = 300;
    public static final int DEFAULT_QUERY_TIMEOUT = 10;
    public static final int GIVEN_RANGE_IN_KM = 20;
    public static final int GIVEN_QUERY_MAX_RESULT = 30;
    public static final int GIVEN_QUERY_TIMEOUT = 10;

    public static final double MOCK_LOCATION_LAT_LAUSANNE = 46.519251915333676;
    public static final double MOCK_LOCATION_LON_LAUSANNE = 6.558563221333525;
    public static final double MOCK_LOCATION_ALT_LAUSANNE = 220;

    public static final double MOCK_LOCATION_LAT_CHAMONIX = 45.92839376413104;
    public static final double MOCK_LOCATION_LON_CHAMONIX = 6.873333749580832;
    public static final double MOCK_LOCATION_ALT_CHAMONIX = 1035;

    //Cache constants
    public static final String CACHE_FILE_NAME_TEST = "POIPointsCache.txt";

    //Display modes POIs preferences
    public static final String DISPLAY_ALL_POIS = "0";
    public static final String DISPLAY_POIS_IN_SIGHT = "1";
    public static final String DISPLAY_POIS_OUT_OF_SIGHT = "2";
}
