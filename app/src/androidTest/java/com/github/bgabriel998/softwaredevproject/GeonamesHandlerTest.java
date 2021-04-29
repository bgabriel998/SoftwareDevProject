package com.github.bgabriel998.softwaredevproject;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;

import static com.github.bgabriel998.softwaredevproject.TestingConstants.MOCK_LOCATION_ALT_LAUSANNE;
import static com.github.bgabriel998.softwaredevproject.TestingConstants.MOCK_LOCATION_LAT_LAUSANNE;
import static com.github.bgabriel998.softwaredevproject.TestingConstants.MOCK_LOCATION_LON_LAUSANNE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)

public class GeonamesHandlerTest {

    private static ArrayList<POI> resultPOI;

    private static long startTimeMs;
    private static double queryTimeS;


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Create and send query to the API
     */
    @BeforeClass
    public static void setup() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(MOCK_LOCATION_LAT_LAUSANNE,
                                            MOCK_LOCATION_LON_LAUSANNE,
                                            MOCK_LOCATION_ALT_LAUSANNE);
        startTimeMs = System.currentTimeMillis();

        GeonamesHandler handler = (GeonamesHandler) new GeonamesHandler(userPoint) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                resultPOI = result;
                queryTimeS = ((double) System.currentTimeMillis() - startTimeMs) / TestingConstants.MILLI_SEC_TO_SEC;
            }
        }.execute();



        Thread.sleep(TestingConstants.DEFAULT_QUERY_TIMEOUT* TestingConstants.MILLI_SEC_TO_SEC*3);

    }

    /**
     * Checks that the result array list contains POI
     */
    @Test
    public void testResultsQuantity(){
        assertNotNull("testResultsQuantity failed. Acquired POI List is empty...", resultPOI);
        assertThat(resultPOI.size(), greaterThan(0));
    }

    /**
     * Checks that the POI contained in the ArrayList are POI
     * from type natural:peaks
     */
    @Test
    public void testResultType(){
        assertNotNull("testResultType failed. Acquired POI List is empty...", resultPOI);
        for(POI point : resultPOI){
            assertEquals(point.mDescription,"peak");
        }
    }



    /**
     * Checks that the name of the POI contained in the list
     * are not empty strings. Only POI with valid names should
     * be returned
     */

    @Test
    public void testResultNameNonNull(){
        assertNotNull("testResultNameNonNull failed. Acquired POI List is empty...", resultPOI);
        for(POI point : resultPOI){
            assertNotEquals(point.mType,isEmptyOrNullString());
        }
    }

    /**
     * Checks that the height of the POI returned is valid (non null)
     * A POI where the height parameter is missing should not be
     * added to the result list.
     */
    @Test
    public void testResultHeightNonNull(){

        assertNotNull("testResultHeightNonNull failed. Acquired POI List is empty...", resultPOI);
        for(POI point : resultPOI){
            assertNotEquals(point.mLocation.getAltitude(),0.0F);
        }
    }

    /**
     * Checks that the result list size does not exceed the limit
     * set in the query (default value of the query is defined by DEFAULT_QUERY_MAX_RESULT
     * parameter in class GeonamesHandler.java
     */
   @Test
    public void testResultListNotExceedLimit(){
        assertNotNull("testResultListNotExceedLimit failed. Acquired POI List is empty...", resultPOI);
        for(POI point : resultPOI){
            assertThat(resultPOI.size(),lessThanOrEqualTo(TestingConstants.DEFAULT_QUERY_MAX_RESULT));
        }
    }

    /**
     * Checks that the query doesn't exceed the timeout set in the
     * query parameters. (default value of the query is defined by DEFAULT_QUERY_TIMEOUT)
     */
        @Test
       public void testResultListNotExceedTimeLimit(){
            assertThat(queryTimeS, lessThanOrEqualTo((double) TestingConstants.DEFAULT_QUERY_TIMEOUT*3));
            assertNotNull(resultPOI);
        }

    /**
     * Check GeonamesHandler creation without valid parameters
     */
    @Test
    public void testGeonamesObjCreationException() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UserPoint user location can't be null");

        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = null;

        new GeonamesHandler(userPoint) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
            }
        }.execute();
        try {
            Thread.sleep(TestingConstants.DEFAULT_QUERY_TIMEOUT* TestingConstants.MILLI_SEC_TO_SEC);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationException failed");
        }
    }

    /**
     * Check GeonamesHandler creation without valid parameters
     * With userPoint as null
     */
    @Test
    public void testGeonamesObjCreationExceptionUserPointNull() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UserPoint user location can't be null");

        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = null;

        new GeonamesHandler(userPoint,
                TestingConstants.GIVEN_RANGE_IN_KM,
                TestingConstants.GIVEN_QUERY_MAX_RESULT,
                TestingConstants.GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
            }
        }.execute();
        try {
            Thread.sleep(TestingConstants.GIVEN_QUERY_TIMEOUT* TestingConstants.MILLI_SEC_TO_SEC);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_1 failed");
        }
    }

    /**
     * Check GeonamesHandler creation without valid parameters
     *  With too short range in km
     */
    @Test
    public void testGeonamesObjCreationExceptionShortRange() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("BoundingBoxRangeKm can't be null or negative (also not under 100m)");
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(MOCK_LOCATION_LAT_LAUSANNE,MOCK_LOCATION_LON_LAUSANNE,MOCK_LOCATION_ALT_LAUSANNE);
        new GeonamesHandler(userPoint,
                0.01,
                TestingConstants.GIVEN_QUERY_MAX_RESULT,
                TestingConstants.GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
            }
        }.execute();
        try {
            Thread.sleep(TestingConstants.GIVEN_QUERY_TIMEOUT* TestingConstants.MILLI_SEC_TO_SEC);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_2 failed");
        }
    }


    /**
     * Check GeonamesHandler creation without valid parameters
     * With query max as zero
     */
    @Test
    public void testGeonamesObjCreationExceptionQueeryMaxAsZero() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QueryMaxResult parameter can't be less than 1");

        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(MOCK_LOCATION_LAT_LAUSANNE,MOCK_LOCATION_LON_LAUSANNE,MOCK_LOCATION_ALT_LAUSANNE);

        new GeonamesHandler(userPoint,
                TestingConstants.GIVEN_RANGE_IN_KM,
                0,
                TestingConstants.GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_3 failed");
        }
    }

    /**
     * Check GeonamesHandler creation without valid parameters
     * With timeout as zero
     */
    @Test
    public void testGeonamesObjCreationExceptionTimeoutAsZero() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QueryTimeout parameter can't be less than 1 sec");
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(MOCK_LOCATION_LAT_LAUSANNE,MOCK_LOCATION_LON_LAUSANNE,MOCK_LOCATION_ALT_LAUSANNE);

        new GeonamesHandler(userPoint,
                TestingConstants.GIVEN_RANGE_IN_KM,
                TestingConstants.GIVEN_QUERY_MAX_RESULT,
                0) {
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_4 failed");
        }
    }
}
