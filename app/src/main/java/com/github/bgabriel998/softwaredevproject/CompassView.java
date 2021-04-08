package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;


/**
 * CompassView draws the compass on the display
 */
public class CompassView extends View {
    //Paints used to draw the lines and heading of the compass on the camera-preview
    private Paint mainLinePaint;
    private Paint secondaryLinePaint;
    private Paint terciaryLinePaint;
    private Paint mainTextPaint;
    private Paint secondaryTextPaint;

    //Colors of the compass-view
    private int compassColor;

    //Font size of the text
    private int mainTextSize;

    //Max opacity for the paints
    private static final int MAX_ALPHA = 255;

    //Factors for the sizes
    private static final int MAIN_TEXT_FACTOR = 20;
    private static final int SEC_TEXT_FACTOR = 15;
    private static final int MAIN_LINE_FACTOR = 5;
    private static final int SEC_LINE_FACTOR = 3;
    private static final int TER_LINE_FACTOR = 2;

    //Heading of the user
    private float horizontalDegrees;
    private float verticalDegrees;

    //Number of pixels per degree
    private float pixDeg;

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
    private Bitmap mountainMarker;

    private List<POIPoint> POIPoints;
    private Point userPoint;

    /**
     * Constructor for the CompassView which initializes the widges like the font height and paints used
     * @param context Context of the activity on which the camera-preview is drawn
     * @param attrs AttributeSet so that the CompassView can be used from the xml directly
     */
    public CompassView(Context context, AttributeSet attrs){
        super(context, attrs);

        widgetInit();
    }

    /**
     * Initializes all needed widgets for the compass like paint variables
     */
    private void widgetInit(){
        //Initialize colors
        compassColor = R.color.Black;

        //Initialize fonts
        float screenDensity = getResources().getDisplayMetrics().scaledDensity;
        mainTextSize = (int) (MAIN_TEXT_FACTOR * screenDensity);

        //Initialize mountain marker
        mountainMarker = BitmapFactory.decodeResource(getResources(), R.drawable.mountain_marker);
        mountainMarker = Bitmap.createScaledBitmap(mountainMarker, 150, 150, true);

        //Initialize paints
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
     * @param rangeDegreesHorizontal range in degrees
     * @param rangeDegreesVertical range in degrees
     */
    public void setRange(float rangeDegreesHorizontal, float rangeDegreesVertical) {
        this.rangeDegreesHorizontal = rangeDegreesHorizontal;
        this.rangeDegreesVertical = rangeDegreesVertical;
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

            //Draw a main line for every 90°
            if (i % 90 == 0){
                //Draw the line,
                //startX starts with 0 at the top left corner and increases when going from left to right
                //startY also increases from top to bottom -> to start at the bottom,
                // we will use the height of the compass-view.
                //stopX is the same as startX since we want to draw a vertical line
                //stopY is the height of the canvas minus some height to leave enough space to write the heading above
                drawLine(i, mainLineHeight, mainLinePaint);

                //Select the correct heading depending on the degree with selecHeadingString()
                //Draw the heading with the mainTextPaint above the line
                canvas.drawText(selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, mainTextPaint);
            }

            //Draw a secondary line for every 45° excluding every 90° (45°, 135°, 225° ...)
            else if (i % 45 == 0){
                drawLine(i, secondaryLineHeight, secondaryLinePaint);
                canvas.drawText(selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, secondaryTextPaint);
            }

            //Draw tertiary line for every 15° excluding every 45° and 90° (15, 30, 60, 75, ...)
            else if (i % 15 == 0){
                drawLine(i, terciaryLineHeight, terciaryLinePaint);
            }

            //Draw the mountains on the canvas
            if(POIPoints != null && !POIPoints.isEmpty()){
                drawPOIs(i);
            }
        }
    }

    /**
     * Draws a line on the compass canvas
     * @param degree Degree where the line needs to be drawn
     * @param lineHeight Height of the line in pixel
     * @param paint paint to be used
     */
    private void drawLine(float degree, int lineHeight, Paint paint){
        canvas.drawLine(pixDeg * (degree - minDegrees), height, pixDeg * (degree - minDegrees), lineHeight, paint);
    }

    /**
     * Set the POIs that will be drawn on the camera-preview
     * TODO replace mock geopoints
     * @param POIPoints List of POIPoints
     * @param userPoint location of the user
     */
    public void setPOIs(List<POIPoint> POIPoints, Point userPoint){
        this.POIPoints = POIPoints;
        this.userPoint = userPoint;
        GeoPoint amsterdam = new GeoPoint(52.38336341146919, 4.899839273125784, 0);
        GeoPoint corse = new GeoPoint(42.412878661343186, 8.951160966878296, 2706);
        GeoPoint wien = new GeoPoint(48.22066363087269, 16.396538068482492, 150);
        GeoPoint bordeaux = new GeoPoint(44.8447387495189, -0.5700051995730769, 15);
        POIPoints.add(new POIPoint(amsterdam));
        POIPoints.add(new POIPoint(corse));
        POIPoints.add(new POIPoint(wien));
        POIPoints.add(new POIPoint(bordeaux));
        invalidate();
        requestLayout();
    }

    /**
     * Draws the POIs on the display
     * @param actualDegree degree of the actual heading of the user
     */
    private void drawPOIs(int actualDegree){
        //Go through all POIPoints
        for(POIPoint poiPoint : POIPoints){
            int horizontalAngle = (int)ComputePOIPoints.getHorizontalBearing(userPoint, poiPoint);
            if(horizontalAngle == actualDegree){
                //Calculate position in pixel of marker
                float deltaVerticalAngle = (float) (ComputePOIPoints.getVerticalBearing(userPoint, poiPoint) - verticalDegrees);
                float deltaVerticalAngle2 = (float) (ComputePOIPoints.calculateElevationAngle(userPoint, poiPoint) - verticalDegrees);
                float mountainMarkerPosition = height * (rangeDegreesVertical - 2*deltaVerticalAngle2) / (2*rangeDegreesVertical)
                        - (float)mountainMarker.getHeight()/2;

                canvas.drawBitmap(mountainMarker, pixDeg * (actualDegree - minDegrees),
                         mountainMarkerPosition, null);
            }
        }
    }

    /**
     * Used to get the string of the actual heading
     * @param degree degree to get the string
     * @return String for the degree
     */
    String selectHeadingString(int degree){
        switch (degree){
            case 0: case 360:
                return "N";
            case 90: case 450:
                return "E";
            case -180: case 180:
                return "S";
            case -90: case 270:
                return "W";
            case 45: case 405:
                return "NE";
            case -45: case 315:
                return "NW";
            case 135: case 495:
                return "SE";
            case -135: case 225:
                return "SW";
            default:
                return "";
        }
    }
}
