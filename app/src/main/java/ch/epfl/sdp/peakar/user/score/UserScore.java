package ch.epfl.sdp.peakar.user.score;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import ch.epfl.sdp.peakar.database.DatabaseHelper;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static ch.epfl.sdp.peakar.utils.POIPointsUtilities.getCountryFromCoordinates;

public class UserScore {


    /*Local database cache*/
    private HashMap<String, CountryHighPoint> databaseCache = null;
    private DatabaseHelper dataBaseHelper = null;

    private final Context mContext;


    private static final int COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE = 20;

    /**
     * initialises countryHighPoint database
     * initialises cache
     */
    public UserScore(Context context){
        this.mContext = context;

        databaseCache = new HashMap<String, CountryHighPoint>();
        if(dataBaseHelper == null){
            dataBaseHelper = new DatabaseHelper(context);
        }
    }

    /**
     * Compute user bonus.
     * If the user discovers the highest peak in a country
     * a bonus is added to the his score
     * @return bonus to add
     */
    private long computeUserBonus(POIPoint poiPoint){
        long retValue = 0;
        retValue += computeCountryHighPointBonus(poiPoint);
        retValue += computePeakHeightBonus(poiPoint);
        return retValue;
    }

    /**
     * Compute part of the user bonus.
     * If the user discovers the highest peak in a country
     * a bonus is added to the his score
     * @return bonus to add
     */
    private long computeCountryHighPointBonus(POIPoint poiPoint){
        long retValue = 0;
        dataBaseHelper.openDataBase();
        /*Search country*/
        String country = getCountryFromCoordinates(mContext, poiPoint.getLatitude(), poiPoint.getLongitude());
        if(country != null){
            CountryHighPoint countryInfo = getDataFromCache(country);
            if (countryInfo == null) {
                //Value was not in the cache
                //Retrieve info to cache it
                countryInfo = new CountryHighPoint(dataBaseHelper.queryHighestPeakName(country),
                        dataBaseHelper.queryHighestPeakHeight(country));
                databaseCache.put(country,countryInfo);
            }
            //Check if the current peak is one country high point
            if(comparePeakNameWithCacheData(countryInfo, poiPoint) && !countryHighPointAlreadyDiscovered(poiPoint,country)) {
                retValue += ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;
                countryInfo.setCountryName(country);
                countryInfo.setCountryHighPoint(poiPoint.getName());
                FirebaseAuthService.getInstance().getAuthAccount().setDiscoveredCountryHighPoint(countryInfo);
            }
        }
        dataBaseHelper.close();
        return  retValue;
    }

    /**
     * Compute part of the user bonus.
     * If the user discovers a new height range
     * a bonus is added to the his score
     * @return bonus to add
     */
    private long computePeakHeightBonus(POIPoint poiPoint){
        int roundedHeight = (int)(poiPoint.getAltitude() - (poiPoint.getAltitude() % 1000));
        if(!FirebaseAuthService.getInstance().getAuthAccount().getDiscoveredPeakHeights().contains(roundedHeight)){
            FirebaseAuthService.getInstance().getAuthAccount().setDiscoveredPeakHeights(roundedHeight);
            switch (roundedHeight){
                case ScoringConstants.BADGE_1st_1000_M_PEAK:
                    return ScoringConstants.BONUS_1st_1000_M_PEAK;
                case ScoringConstants.BADGE_1st_2000_M_PEAK:
                    return ScoringConstants.BONUS_1st_2000_M_PEAK;
                case ScoringConstants.BADGE_1st_3000_M_PEAK:
                    return ScoringConstants.BONUS_1st_3000_M_PEAK;
                case ScoringConstants.BADGE_1st_4000_M_PEAK:
                    return ScoringConstants.BONUS_1st_4000_M_PEAK;
                case ScoringConstants.BADGE_1st_5000_M_PEAK:
                    return ScoringConstants.BONUS_1st_5000_M_PEAK;
                case ScoringConstants.BADGE_1st_6000_M_PEAK:
                    return ScoringConstants.BONUS_1st_6000_M_PEAK;
                case ScoringConstants.BADGE_1st_7000_M_PEAK:
                    return ScoringConstants.BONUS_1st_7000_M_PEAK;
                case ScoringConstants.BADGE_1st_8000_M_PEAK:
                    return ScoringConstants.BONUS_1st_8000_M_PEAK;
            }
        }
        return 0;
    }

    /**
     * Compare the poi peak name with the given cache entry name
     * @param countryInfo cache entry containing highest point name from the same country as poiPoint
     * @param poiPoint peak retrieved with provider
     * @return true if the comparison matches
     */
    private boolean comparePeakNameWithCacheData(CountryHighPoint countryInfo, POIPoint poiPoint){
        return (countryInfo.getCountryHighPoint().contains(poiPoint.getName()) ||
                poiPoint.getName().contains(countryInfo.getCountryHighPoint()))
                && Math.abs(poiPoint.getAltitude() - countryInfo.getHighPointHeight()) < COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE;
    }

    /**
     * Check in the database if the user has already discovered this country
     * highest point
     * @param poiPoint new discovered peak
     * @param country country in which the new discovered peak is located
     * @return true if the
     */
    private boolean countryHighPointAlreadyDiscovered(POIPoint poiPoint, String country){
        HashMap<String, CountryHighPoint> countryHighPointDiscovered = FirebaseAuthService.getInstance().getAuthAccount().getDiscoveredCountryHighPoint();
        if(countryHighPointDiscovered == null) return false;
        if(countryHighPointDiscovered.containsKey(country))
            return countryHighPointDiscovered.get(country).getCountryHighPoint().contains(poiPoint.getName());
        return false;
    }

    /**
     * getDataFromCache try to get country HighPoint from cache. If entry is not present
     * in the cache, the function returns null
     * @param country country name to get its highest point
     * @return CacheEntry containing
     */
    private CountryHighPoint getDataFromCache(String country){
        if(databaseCache.containsKey(country)){
            return databaseCache.get(country);
        }
        else return null;
    }

    /**
     * Computes the new user score given the list of freshly scanned peaks
     * Updates discovered country high point list in the DB
     * @param scannedPeaks peaks the user scanned
     * @return new user Score
     */
    private long computeUserScore(ArrayList<POIPoint> scannedPeaks){

        //Get previous score from user database
        long userScore = FirebaseAuthService.getInstance().getAuthAccount().getScore();
        //Foreach loop over all scanned peaks
        for(POIPoint poiPoint : scannedPeaks){
            /*Add classical amount of points*/
            userScore += poiPoint.getAltitude() * ScoringConstants.PEAK_FACTOR;
            userScore += computeUserBonus(poiPoint);
        }
        return userScore;
    }

    /**
     * Computes the list of newly discovered peaks and the new user score
     * Writes the user score and the new discovered peaks to the database
     * @param scannedPeaks list of scanned peaks (unfiltered : may contain already scanned peaks)
     */
    public void updateUserScoreAndDiscoveredPeaks(ArrayList<POIPoint> scannedPeaks){
        //Filter out all already discovered peaks
        ArrayList<POIPoint> filteredScannedPeaks = FirebaseAuthService.getInstance().getAuthAccount().filterNewDiscoveredPeaks(scannedPeaks);
        long userScore = computeUserScore(filteredScannedPeaks);
        //Overwrite score value in the database
        FirebaseAuthService.getInstance().getAuthAccount().setScore(userScore);
        //Add all new discovered peaks to the database
        FirebaseAuthService.getInstance().getAuthAccount().setDiscoveredPeaks(filteredScannedPeaks);
    }
}
