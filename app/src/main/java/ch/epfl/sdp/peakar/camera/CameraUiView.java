package ch.epfl.sdp.peakar.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.utils.CameraUtilities;

import static ch.epfl.sdp.peakar.utils.ImageHandler.rotateBitmap;

/**
 * CameraUiView draws a canvas with the compass and mountain information on the camera-preview
 */
public class CameraUiView extends View implements Observer {

    // computePOIPointsInstance instance
    private final ComputePOIPoints computePOIPointsInstance;

    //Paints used to draw the lines and heading of the compass on the camera-preview
    private Paint mainLinePaint;
    private Paint secondaryLinePaint;
    private Paint terciaryLinePaint;
    private Paint mainTextPaint;
    private Paint mountainInfo;
    private Paint secondaryTextPaint;
    private Paint backgroundRectPaint;

    //Colors of the compass-view
    private int compassColor;

    //Font size of the text
    private int mainTextSize;

    //Max opacity for the paints
    private static final int MAX_ALPHA = 255;

    //Rotation to be applied for the addition info of the mountains
    private static final int LABEL_ROTATION = -45;
    //Offset for the text to be above the marker
    private static final int OFFSET_MOUNTAIN_INFO = 15;

    //Factors for the sizes
    private static final int RADIUS_RECT_CORNER = 60;
    private static final int MAIN_TEXT_FACTOR = 20;
    private static final int SEC_TEXT_FACTOR = 15;
    private static final int OFFSET_RECTANGLE_X_EDGE = 7;
    private static final int MAIN_LINE_FACTOR = 5;
    private static final int OFFSET_RECTANGLE_Y_EDGE = 4;
    private static final int SEC_LINE_FACTOR = 3;
    private static final int TER_LINE_FACTOR = 2;

    //Heading of the user
    private float horizontalDegrees;
    private float verticalDegrees;

    //Number of pixels per degree
    private float pixDeg;
    private float screenDensity;

    //Range of the for-loop to draw the compass
    private float minDegrees;
    private float maxDegrees;
    private float rangeDegreesVertical;
    private float rangeDegreesHorizontal;

    //Compass canvas
    private Canvas canvas;

    //Heights of the compass
    private int textHeight;
    private int mainLineHeight;
    private int secondaryLineHeight;
    private int terciaryLineHeight;

    //Height of the view in pixel
    private int height;

    //Marker used to display mountains on camera-preview
    private Bitmap mountainMarkerVisible;
    private Bitmap mountainMarkerNotVisible;
    private Bitmap distanceBitmap;
    private Bitmap heightBitmap;

    //Map that contains the labeled POIPoints
    private Map<POIPoint, Boolean> labeledPOIPoints;

    private final SharedPreferences sharedPref;

    private Boolean displayedToastMode;
    private Boolean displayCompass;

    private static final String DISPLAY_ALL_POIS = "0";
    private static final String DISPLAY_POIS_IN_SIGHT = "1";
    private static final String DISPLAY_POIS_OUT_OF_SIGHT = "2";

    private final List<POIPoint> discoveredPOIPoints;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");


    private final SharedPreferences.OnSharedPreferenceChangeListener listenerPreferences =
            (prefs, key) -> {
                POISetter(prefs);
                displayCompass = prefs.getBoolean(getResources().getString(R.string.displayCompass_key), false);
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

        discoveredPOIPoints = new ArrayList<>();

        displayedToastMode = false;
        
        displayCompass = sharedPref.getBoolean(getResources().getString(R.string.displayCompass_key), false);

        computePOIPointsInstance = ComputePOIPoints.getInstance(context);

        computePOIPointsInstance.addObserver(this);

        POISetter(sharedPref);
    }

    /**
     * Handles the setting of the displayed POIpoints
     *
     * @param prefs sharedPreferences to determine the POIPoints to be displayed
     */
    private void POISetter(SharedPreferences prefs) {

        String displayMode = prefs.getString(getResources().getString(R.string.displayPOIs_key), DISPLAY_ALL_POIS);
        boolean filterPOIs = prefs.getBoolean(getResources().getString(R.string.filterPOIs_key), true);

        switch (displayMode){
            case DISPLAY_ALL_POIS:
                setPOIs(filterPOIs ? computePOIPointsInstance.getFilteredPOIs() : computePOIPointsInstance.getPOIs());
                break;
            case DISPLAY_POIS_IN_SIGHT:
                setPOIs(filterPOIs ? computePOIPointsInstance.getFilteredPOIsInSight() : computePOIPointsInstance.getPOIsInSight());
                checkIfLineOfSightAvailable();
                break;
            case DISPLAY_POIS_OUT_OF_SIGHT:
                setPOIs(filterPOIs ? computePOIPointsInstance.getFilteredPOIsOutOfSight() : computePOIPointsInstance.getPOIsOutOfSight());
                checkIfLineOfSightAvailable();
                break;
        }

    }

    /**
     * Initializes all needed widgets for the compass like paint variables
     */
    private void widgetInit(){
        //Initialize colors
        compassColor = getResources().getColor(R.color.Black, null);
        int mountainInfoColor = getResources().getColor(R.color.White, null);

        //Initialize fonts
        screenDensity = getResources().getDisplayMetrics().scaledDensity;
        mainTextSize = (int) (MAIN_TEXT_FACTOR * screenDensity);

        //Initialize mountain marker that are in line of sight
        mountainMarkerVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_visible);

        //Initialize mountain marker that are not in line of sight
        mountainMarkerNotVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_not_visible);

        heightBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_double_arrow);
        distanceBitmap = rotateBitmap(heightBitmap);

        //Initialize paints
        mountainInfo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mountainInfo.setTextAlign(Paint.Align.LEFT);
        mountainInfo.setTextSize(SEC_TEXT_FACTOR*screenDensity);
        mountainInfo.setColor(mountainInfoColor);
        mountainInfo.setAlpha(MAX_ALPHA);

        //Paint used for the main text heading (N, E, S, W)
        mainTextPaint = configureTextPaint(mainTextSize);

        //Paint used for the secondary text heading (NE, SE, SW, NW)
        secondaryTextPaint = configureTextPaint(SEC_TEXT_FACTOR*screenDensity);

        //Paint used for the main lines (0°, 90°, 180°, ...)
        mainLinePaint = configureLinePaint(MAIN_LINE_FACTOR*screenDensity);

        //Paint used for the secondary lines (45°, 135°, 225°, ...)
        secondaryLinePaint = configureLinePaint(SEC_LINE_FACTOR*screenDensity);

        //Paint used for the terciary lines (15°, 30°, 60°, 75°, 105°, ...)
        terciaryLinePaint = configureLinePaint(TER_LINE_FACTOR*screenDensity);

        backgroundRectPaint = configureRectPaint();
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
     * Method to create the line paints for the compass
     * @param strokeWidth width of the lines
     * @return configured paint
     */
    private Paint configureLinePaint(float strokeWidth){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(compassColor);
        paint.setAlpha(MAX_ALPHA);
        return paint;
    }

    /**
     * Method to create a rectangle paint around the text
     * @return configured paint
     */
    private Paint configureRectPaint(){
        Paint paint = new Paint();
        paint.setStrokeWidth(0);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true);
        paint.setColor(typedValue.data);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(100);
        return paint;
    }

    /**
     * Method to create the text paints for the compass
     * @param textSize size of the text
     * @return configured paint
     */
    private Paint configureTextPaint(float textSize){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(textSize);
        paint.setColor(compassColor);
        paint.setAlpha(MAX_ALPHA);
        return paint;
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
     * onDraw method is used to draw the compass on the screen.
     * To draw the compass, 3 different types of lines are used, mainLinePaint, secondaryLinePaint
     * and terciaryLinePaint. The compass is drawn by going through a for-loop starting from minDegree
     * until maxDegrees. They correspond to the actual heading minus and plus half of the field of view
     * of the device camera.
     * @param canvas Canvas on which the compass is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
        //Get width and height of the view in Pixels
        int width = getMeasuredWidth();
        height = getMeasuredHeight();
        //Make the canvas take 1/5 of the screen height
        //The text is at the highest point
        textHeight = height - height/5;
        //mainLineHeight is just under the text
        //The text is centered thus we add the textsize divided by 2
        mainLineHeight = textHeight + mainTextSize/2;
        //Then increment each by mainTextSize to get the next line height
        // (the higher the result the lower the line)
        secondaryLineHeight = mainLineHeight + mainTextSize;
        terciaryLineHeight = secondaryLineHeight + mainTextSize;

        //Get the starting degree and ending degree of the compass
        minDegrees = horizontalDegrees - rangeDegreesHorizontal/2;
        maxDegrees = horizontalDegrees + rangeDegreesHorizontal/2;

        //Calculate the width in pixel of one degree
        pixDeg = width/rangeDegreesHorizontal;

        //Draws the compass
        drawCanvas();
    }

    /**
     * Draws the compass on the canvas
     */
    private void drawCanvas(){
        //Start going through the loop to draw the compass
        for(int i = (int)Math.floor(minDegrees); i <= Math.ceil(maxDegrees); i++){
            //Draw the compass
            if(displayCompass){
                drawCompass(i);
            }

            //Draw the mountains on the canvas
            if(labeledPOIPoints != null && !labeledPOIPoints.isEmpty()) {
                drawLabeledPOIs(i);
            }
        }
    }

    /**
     * Draws the compass on the canvas. Compass consists of a for-loop going from the heading -
     * fov/2 until heading + fov/2.
     * @param i degree of the compass
     */
    private void drawCompass(int i) {
        float lineXpos = pixDeg * (i - minDegrees);
        //Draw a main line and heading for every 90°
        if (i % 90 == 0){
            canvas.drawLine(lineXpos, height, lineXpos, mainLineHeight, mainLinePaint);
            canvas.drawText(CameraUtilities.selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, mainTextPaint);
        }

        //Draw a secondary line for every 45° excluding every 90° (45°, 135°, 225° ...)
        else if (i % 45 == 0){
            canvas.drawLine(lineXpos, height, lineXpos, secondaryLineHeight, secondaryLinePaint);
            canvas.drawText(CameraUtilities.selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, secondaryTextPaint);
        }

        //Draw tertiary line for every 15° excluding every 45° and 90° (15, 30, 60, 75, ...)
        else if (i % 15 == 0){
            canvas.drawLine(lineXpos, height, lineXpos, terciaryLineHeight, terciaryLinePaint);
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
     * Draws the mountain marker on the canvas depending on the visibility of the POIPoint and adds
     * them to the discovered POIPoints if the user looks at them and they are in the line of sight
     * @param poiPoint POIPoint that gets drawn
     * @param isVisible Boolean that indicates if the POIPoint is visible or not
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawMountainMarker(POIPoint poiPoint, Boolean isVisible, int actualDegree){
        if(isVisible && (int)poiPoint.getHorizontalBearing()== (int)horizontalDegrees && !discoveredPOIPoints.contains(poiPoint)){
            Date discoveredDate = new Date();
            poiPoint.setDiscoveredDate(DATE_FORMAT.format(discoveredDate));
            discoveredPOIPoints.add(poiPoint);
        }

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

        String textName = poiPoint.getName();
        float xName = left + mountainInfo.getTextSize();
        float yName = mountainMarkerPosition + mountainInfo.getTextSize() + OFFSET_RECTANGLE_Y_EDGE*screenDensity;

        float xBitmapHeight = xName - OFFSET_RECTANGLE_Y_EDGE*screenDensity;
        float yBitmapHeight = yName + 2*screenDensity;

        String textHeight = " " + (int)poiPoint.getAltitude() + "m, ";
        float xTextHeight = xBitmapHeight + heightBitmap.getWidth()/2f;
        float yTextHeight = yName + mountainInfo.getTextSize();

        float xBitmapDistance = xTextHeight + mountainInfo.measureText(textHeight);
        float yBitmapDistance = yBitmapHeight;

        Formatter mToKm = new Formatter();
        mToKm.format("%.2f", poiPoint.getDistanceToUser()/1000);
        String textDistance = " " + mToKm.toString() + "km";
        float xTextDistance = xBitmapDistance + distanceBitmap.getWidth();
        float yTextDistance = yTextHeight;
        
        float leftRect = left - mountainMarker.getHeight()/screenDensity;
        float topRect = yName - mountainInfo.getTextSize() + 2*screenDensity;
        float bottomRect = yTextDistance + OFFSET_RECTANGLE_Y_EDGE*screenDensity;
        float rightRect = Math.max(xTextDistance + mountainInfo.measureText(textDistance), xName + mountainInfo.measureText(textName)) + OFFSET_RECTANGLE_X_EDGE*screenDensity;

        float xNameCentered = xName + (rightRect - xName - mountainInfo.measureText(textName) - OFFSET_RECTANGLE_X_EDGE*screenDensity)/2;

        //Draw first rectangle to overdraw the background
        canvas.drawRoundRect(leftRect, topRect, rightRect, bottomRect, RADIUS_RECT_CORNER, RADIUS_RECT_CORNER, backgroundRectPaint);

        canvas.drawText(textName, Math.max(xNameCentered, xName), yName, mountainInfo);

        canvas.drawBitmap(heightBitmap, xBitmapHeight, yBitmapHeight, null);
        canvas.drawText(textHeight, xTextHeight, yTextHeight, mountainInfo);
        canvas.drawBitmap(distanceBitmap, xBitmapDistance, yBitmapDistance, null);
        canvas.drawText(textDistance, xTextDistance, yTextDistance, mountainInfo);

        //Restore the saved state
        canvas.restore();
    }

    /**
     * Gets the new discovered peaks
     * @return List of the new discovered peaks
     */
    public List<POIPoint> getDiscoveredPOIPoints(){
        return discoveredPOIPoints;
    }

    /**
     * Checks if the line of sight has been computed. If not display only one toast informing the user
     */
    private void checkIfLineOfSightAvailable() {
        if(!computePOIPointsInstance.isLineOfSightAvailable() && !displayedToastMode){
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

    @Override
    public void update(Observable o, Object arg) {
        POISetter(sharedPref);
    }
}
