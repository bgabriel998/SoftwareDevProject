package ch.epfl.sdp.peakar.user;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;
import ch.epfl.sdp.peakar.user.score.UserScore;
import ch.epfl.sdp.peakar.user.services.AuthService;

import static ch.epfl.sdp.peakar.TestingConstants.*;
import static ch.epfl.sdp.peakar.user.AccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AccountTest.removeAuthUser;
import static org.junit.Assert.assertEquals;


public class UserScoreTest {

    private static UserScore userScore;

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before a test */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();

        userScore = new UserScore(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeAuthUser();
    }

    /**
     * Test compute user bonus function
     * compute the amount of points given to the user with a given Array
     * List of POI
     */
    @Test
    public void testComputeUserBonus(){
        GeoPoint geoPoint_1 = new GeoPoint(MONT_BLANC_LAT,MONT_BLANC_LONG,MONT_BLANC_ALT);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName(MONT_BLANC_NAME);

        GeoPoint geoPoint_2 = new GeoPoint(DENT_DU_GEANT_LAT, DENT_DU_GEANT_LONG,DENT_DU_GEANT_ALT);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName(DENT_DU_GEANT_NAME);

        GeoPoint geoPoint_3 = new GeoPoint(AIGUILLE_DU_PLAN_LAT, AIGUILLE_DU_PLAN_LONG,AIGUILLE_DU_PLAN_ALT);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName(AIGUILLE_DU_PLAN_NAME);

        GeoPoint geoPoint_4 = new GeoPoint(POINTE_DE_LAPAZ_LAT, POINTE_DE_LAPAZ_LONG,POINTE_DE_LAPAZ_ALT);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName(POINTE_DE_LAPAZ_NAME);

        ArrayList<POIPoint> inputArrayList = new ArrayList<POIPoint>();
        inputArrayList.add(point_2);
        inputArrayList.add(point_1);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);


        userScore.updateUserScoreAndDiscoveredPeaks(inputArrayList);
        long expectedUserScore = (long)(point_1.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_2.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_3.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_4.getAltitude() * ScoringConstants.PEAK_FACTOR)+
                                ScoringConstants.BONUS_1st_3000_M_PEAK+
                                ScoringConstants.BONUS_1st_4000_M_PEAK+
                                ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;

        assertEquals(expectedUserScore, AuthService.getInstance().getAuthAccount().getScore());
    }


}
