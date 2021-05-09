package ch.epfl.sdp.peakar.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import java.util.Map;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.POIPoint;

/**
 * CameraUiView draws a canvas with the compass and mountain information on the camera-preview
 */
public class CameraUiView extends View {
    //Paints used to draw the mountain info
    private Paint mountainInfo;

    //Max opacity for the paints
    private static final int MAX_ALPHA = 255;

    //Rotation to be applied for the addition info of the mountains
    private static final int LABEL_ROTATION = -45;
    //Offset for the text to be above the marker
    private static final int OFFSET_MOUNTAIN_INFO = 15;

    //Factors for the sizes
    private static final int MAIN_TEXT_FACTOR = 20;

    //Heading of the user
    private float horizontalDegrees;
    private float verticalDegrees;

    //Number of pixels per degree
    private float pixDeg;

    //Range of the for-loop to draw the compass
    private float minDegrees;
    private float rangeDegreesVertical;
    private float rangeDegreesHorizontal;

    //Compass canvas
    private Canvas canvas;

    //Height of the view in pixel
    private int height;

    //Marker used to display mountains on camera-preview
    private Bitmap mountainMarkerVisible;
    private Bitmap mountainMarkerNotVisible;

    //Map that contains the labeled POIPoints
    private Map<POIPoint, Boolean> labeledPOIPoints;

    private final SharedPreferences sharedPref;

    private Boolean displayedToastMode;


    private static final String DISPLAY_ALL_POIS = "0";
    private static final String DISPLAY_POIS_IN_SIGHT = "1";
    private static final String DISPLAY_POIS_OUT_OF_SIGHT = "2";


    private final SharedPreferences.OnSharedPreferenceChangeListener listenerPreferences =
            (prefs, key) -> {
                String displayMode = prefs.getString(getResources().getString(R.string.displayPOIs_key), DISPLAY_ALL_POIS);
                boolean filterPOIs = prefs.getBoolean(getResources().getString(R.string.filterPOIs_key), true);

                switch (displayMode){
                    case DISPLAY_ALL_POIS:
                        setPOIs(filterPOIs ? ComputePOIPoints.getFilteredPOIs() : ComputePOIPoints.getPOIs());
                        break;
                    case DISPLAY_POIS_IN_SIGHT:
                        setPOIs(filterPOIs ? ComputePOIPoints.getFilteredPOIsInSight() : ComputePOIPoints.getPOIsInSight());
                        checkIfLineOfSightAvailable();
                        break;
                    case DISPLAY_POIS_OUT_OF_SIGHT:
                        setPOIs(filterPOIs ? ComputePOIPoints.getFilteredPOIsOutOfSight() : ComputePOIPoints.getPOIsOutOfSight());
                        checkIfLineOfSightAvailable();
                        break;
                }
            };

    /**
     * Constructor for the CompassView which initializes the widges like the font height and paints used
     * @param context Context of the activity on which the camera-preview is drawn
     * @param attrs AttributeSet so that the CompassView can be used from the xml directly
     */
    public CameraUiView(Context context, AttributeSet attrs){
        super(context, attrs);

        widgetInit();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.registerOnSharedPreferenceChangeListener(listenerPreferences);

        displayedToastMode = false;
    }

    /**
     * Initializes all needed widgets for the compass like paint variables
     */
    private void widgetInit(){
        //Initialize colors
        //Colors of the compass-view
        int mountainInfoColor = getResources().getColor(R.color.Black, null);

        //Initialize fonts
        float screenDensity = getResources().getDisplayMetrics().scaledDensity;

        //Initialize mountain marker that are in line of sight
        mountainMarkerVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_visible);

        //Initialize mountain marker that are not in line of sight
        mountainMarkerNotVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_not_visible);

        //Initialize paints
        mountainInfo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mountainInfo.setTextAlign(Paint.Align.LEFT);
        mountainInfo.setTextSize(MAIN_TEXT_FACTOR*screenDensity);
        mountainInfo.setColor(mountainInfoColor);
        mountainInfo.setAlpha(MAX_ALPHA);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Set the horizontal and vertical degrees for the compass and markers. When setDegrees is called,
     * it updates the canvas calling invalidate() and requestLayout() which redraws the view.
     * @param horizontalDegrees set the horizontal heading in degrees
     * @param verticalDegrees set the vertical heading in degrees
     */
    public void setDegrees(float horizontalDegrees, float verticalDegrees) {
        this.horizontalDegrees = horizontalDegrees;
        this.verticalDegrees = verticalDegrees;
        invalidate();
        requestLayout();
    }

    /**
     * Sets the range in degrees of the compass-view, corresponds to the field of view of the camera
     * @param cameraFieldOfView Pair containing the horizontal and vertical field of view
     */
    @SuppressWarnings("ConstantConditions")
    public void setRange(Pair<Float, Float> cameraFieldOfView) {
        int orientation = getResources().getConfiguration().orientation;

        //Switch horizontal and vertical fov depending on the orientation
        this.rangeDegreesHorizontal = orientation==Configuration.ORIENTATION_LANDSCAPE ?
                cameraFieldOfView.first : cameraFieldOfView.second;
        this.rangeDegreesVertical = orientation==Configuration.ORIENTATION_LANDSCAPE ?
                cameraFieldOfView.second : cameraFieldOfView.first;
    }

    /**
     * Set the POIs that will be drawn on the camera-preview
     * @param labeledPOIPoints Map of the POIPoints with the line of sight boolean
     */
    public void setPOIs(Map<POIPoint, Boolean> labeledPOIPoints){
        this.labeledPOIPoints = labeledPOIPoints;
        invalidate();
        requestLayout();
    }


    /**
     * onDraw method is used to draw the POIs on the screen. The POIs are drawn by going through
     * a for-loop starting from minDegree until maxDegrees. They correspond to the actual heading
     * minus and plus half of the field of vie of the device camera.
     *
     * @param canvas Canvas on which the compass is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
        //Get width and height of the view in Pixels
        int width = getMeasuredWidth();
        height = getMeasuredHeight();

        //Get the starting degree and ending degree of the compass
        minDegrees = horizontalDegrees - rangeDegreesHorizontal/2;
        float maxDegrees = horizontalDegrees + rangeDegreesHorizontal / 2;

        //Calculate the width in pixel of one degree
        pixDeg = width/rangeDegreesHorizontal;

        //Draws the POIs
        for(int i = (int)Math.floor(minDegrees); i <= Math.ceil(maxDegrees); i++){
            //Draw the mountains on the canvas
            if(labeledPOIPoints != null && !labeledPOIPoints.isEmpty()) {
                drawLabeledPOIs(i);
            }
            else{
                listenerPreferences.onSharedPreferenceChanged(sharedPref, null);
            }
        }
    }

    /**
     * Draws the POIs depending on their visibility on the display using the horizontal and
     * vertical bearing of the mountain to the user
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawLabeledPOIs(int actualDegree){
        //Go through all POIPoints
        labeledPOIPoints.entrySet().stream()
                .filter(p -> (int)p.getKey().getHorizontalBearing() == (actualDegree + 360) % 360)
                        .forEach(p -> drawMountainMarker(p.getKey(), p.getValue(), actualDegree));
    }

    /**
     * Draws the mountain marker on the canvas depending on the visibility of the POIPoint
     * @param poiPoint POIPoint that gets drawn
     * @param isVisible Boolean that indicates if the POIPoint is visible or not
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawMountainMarker(POIPoint poiPoint, Boolean isVisible, int actualDegree){
        //Use both results and substract the actual vertical heading
        float deltaVerticalAngle = (float) (poiPoint.getVerticalBearing() - verticalDegrees);

        //Calculate position in Pixel to display the mountainMarker
        float mountainMarkerPosition = height * (rangeDegreesVertical - 2*deltaVerticalAngle) / (2*rangeDegreesVertical)
                - (float)mountainMarkerVisible.getHeight()/2;

        //Calculate the horizontal position
        float left = pixDeg * (actualDegree - minDegrees);

        //Draw the marker on the preview depending on the line of sight
        Bitmap mountainMarker = isVisible ? mountainMarkerVisible : mountainMarkerNotVisible;

        canvas.drawBitmap(mountainMarker, left, mountainMarkerPosition, null);

        //Save status before Screen Rotation
        canvas.save();
        canvas.rotate(LABEL_ROTATION, left, mountainMarkerPosition);
        canvas.drawText(poiPoint.getName() + " " + poiPoint.getAltitude() + "m",
                left + mountainInfo.getTextSize() - OFFSET_MOUNTAIN_INFO,
                mountainMarkerPosition + mountainInfo.getTextSize() + OFFSET_MOUNTAIN_INFO,
                mountainInfo);
        //Restore the saved state
        canvas.restore();
    }

    /**
     * Checks if the line of sight has been computed. If not display only one toast informing the user
     */
    private void checkIfLineOfSightAvailable() {
        if(!ComputePOIPoints.isLineOfSightAvailable() && !displayedToastMode){
            Toast.makeText(getContext(), getResources().getString(R.string.lineOfSightNotDownloaded), Toast.LENGTH_SHORT).show();
            displayedToastMode = true;
        }
    }

    /**
     * Get a bitmap of the compass-view
     * @return a bitmap of the compass-view
     */
    public Bitmap getBitmap(){
        CameraUiView cameraUiView = findViewById(R.id.compass);
        cameraUiView.setDrawingCacheEnabled(true);
        cameraUiView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cameraUiView.getDrawingCache());
        cameraUiView.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
