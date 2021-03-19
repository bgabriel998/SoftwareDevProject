package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)

public class GeonamesHandlerTest {

    private static ArrayList<POI> resultPOI;

    private static long startTimeMs;
    private static double queryTimeS;


    private static final int MILLI_SEC_TO_SEC = 1000;

    private static final int DEFAULT_QUERY_MAX_RESULT = 300;
    private static final int DEFAULT_QUERY_TIMEOUT = 10;

    private static final int GIVEN_RANGE_IN_KM = 20;
    private static final int GIVEN_QUERY_MAX_RESULT = 30;
    private static final int GIVEN_QUERY_TIMEOUT = 10;



    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Create and send query to the API
     */
    @BeforeClass
    public static void setup(){
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        startTimeMs = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(46.519251915333676, 6.558563221333525, 220);
        new GeonamesHandler(userPoint) {
            @Override
            public void onResponseReceived(Object result) {
                resultPOI = (ArrayList<POI>) result;
                queryTimeS = ((double)System.currentTimeMillis() - startTimeMs)/ MILLI_SEC_TO_SEC;
            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testResultType failed");
        }
    }

    /**
     * Checks that the result array list contains POI
     */
    @Test
    public void testResultsQuantity(){
        if (resultPOI == null) fail("testResultsQuantity failed");
        assertThat(resultPOI.size(), greaterThan(0));
    }

    /**
     * Checks that the POI contained in the ArrayList are POI
     * from type natural:peaks
     */
    @Test
    public void testResultType(){
        if (resultPOI == null) fail("testResultType failed");
        for(POI point : resultPOI){
            assertEquals(point.mDescription,"peak");
            Log.v("GEONAMES","Descr: "+ point.mDescription);
        }
    }



    /**
     * Checks that the name of the POI contained in the list
     * are not empty strings. Only POI with valid names should
     * be returned
     */
    @Test
    public void testResultNameNonNull(){
        if (resultPOI == null) fail("testResultNameNonNull failed");
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
        if (resultPOI == null) fail("testResultHeightNonNull failed");
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
        if (resultPOI == null) fail("testResultListNotExceedLimit failed");
        for(POI point : resultPOI){
            assertThat(resultPOI.size(),lessThanOrEqualTo(DEFAULT_QUERY_MAX_RESULT));
        }
    }

    /**
     * Checks that the query doesn't exceed the timeout set in the
     * query parameters. (default value of the query is defined by DEFAULT_QUERY_TIMEOUT)
     */
    @Test
    public void testResultListNotExceedTimeLimit(){
        assertThat(queryTimeS, lessThanOrEqualTo((double)DEFAULT_QUERY_TIMEOUT));
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
            public void onResponseReceived(Object result) {
            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationException failed");
        }
    }

    /**
     * Check GeonamesHandler creation without valid parameters
     * Checking userPoint parameter here
     */
    @Test
    public void testGeonamesObjCreationExceptionUserPointNull() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UserPoint user location can't be null");

        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = null;

        new GeonamesHandler(userPoint,
                GIVEN_RANGE_IN_KM,
                GIVEN_QUERY_MAX_RESULT,
                GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(Object result) {
            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_1 failed");
        }
    }

    /**
     * Check GeonamesHandler creation without valid parameters
     * Checking Query max result parameter here
     */
    @Test
    public void testGeonamesObjCreationExceptionCustom_Arg_2() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("BoundingBoxRangeKm can't be null or negative (also not under 100m)");
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(46.519251915333676,6.558563221333525,220);
        new GeonamesHandler(userPoint,
                0.01,
                GIVEN_QUERY_MAX_RESULT,
                GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(Object result) {

            }
        }.execute();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("testGeonamesObjCreationExceptionCustom_Arg_2 failed");
        }
    }


    /**
     * Check GeonamesHandler creation without valid parameters
     * Checking Query max result parameter here
     */
    @Test
    public void testGeonamesObjCreationExceptionCustom_Arg_3() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QueryMaxResult parameter can't be less than 1");

        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(46.519251915333676,6.558563221333525,220);

        new GeonamesHandler(userPoint,
                GIVEN_RANGE_IN_KM,
                0,
                GIVEN_QUERY_TIMEOUT) {
            @Override
            public void onResponseReceived(Object result) {

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
     * Checking query timeout parameter here
     */
    @Test
    public void testGeonamesObjCreationExceptionCustom_Arg_4() throws IllegalArgumentException{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QueryTimeout parameter can't be less than 1 sec");
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = new UserPoint(46.519251915333676,6.558563221333525,220);

        new GeonamesHandler(userPoint,
                GIVEN_RANGE_IN_KM,
                GIVEN_QUERY_MAX_RESULT,
                0) {
            @Override
            public void onResponseReceived(Object result) {

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
