package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


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
    private int textColor;
    private int lineColor;

    //Font size of the text
    private int mainTextSize;

    //Heading of the user
    private float horizontalDegrees;
    private float verticalDegrees;

    //Number of pixels per degree
    private float pixDeg;

    //Range of the for-loop to draw the compass
    private float minDegrees;
    private float maxDegrees;

    //Compass canvas
    private Canvas canvas;

    //Heights of the compass
    int textHeight;
    int mainLineHeight;
    int secondaryLineHeight;
    int terciaryLineHeight;

    //Height of the view in pixel
    int height;

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
     * Initializes all needed widgets like paint variables
     */
    private void widgetInit(){
        //Initialize colors
        textColor = R.color.Black;
        lineColor = R.color.Black;

        //Initialize fonts
        float screenDensity = getResources().getDisplayMetrics().scaledDensity;
        mainTextSize = (int) (15 * screenDensity);
        int secondaryTextSize = (int) (12 * screenDensity);

        //Initialize paints
        //Paint used for the mian text heading (N, E, S, W)
        mainTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainTextPaint.setTextAlign(Paint.Align.CENTER);
        mainTextPaint.setColor(textColor);
        mainTextPaint.setTextSize(mainTextSize);

        //Paint used for the secondary text heading (NE, SE, SW, NW)
        secondaryTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryTextPaint.setTextAlign(Paint.Align.CENTER);
        secondaryTextPaint.setColor(textColor);
        secondaryTextPaint.setTextSize(secondaryTextSize);

        //Paint used for the main lines (0°, 90°, 180°, ...)
        mainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainLinePaint.setStrokeWidth(8f);
        mainLinePaint.setColor(lineColor);

        //Paint used for the secondary lines (45°, 135°, 225°, ...)
        secondaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryLinePaint.setStrokeWidth(6f);
        secondaryLinePaint.setColor(lineColor);

        //Paint used for the terciary lines (15°, 30°, 60°, 75°, 105°, ...)
        terciaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        terciaryLinePaint.setStrokeWidth(3f);
        terciaryLinePaint.setColor(lineColor);
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
        //mainLineHeight is just under the text so we add the text size
        mainLineHeight = textHeight + mainTextSize;
        //Then increment each by mainTextSize to get the next line height
        // (the higher the result the lower the line)
        secondaryLineHeight = mainLineHeight + 2*mainTextSize;
        terciaryLineHeight = secondaryLineHeight + mainTextSize;

        //Field of view of device
        //TODO get fov of device
        float fov = 180;

        //Get the starting degree and ending degree of the compass
        minDegrees = horizontalDegrees - fov/2;
        maxDegrees = horizontalDegrees + fov/2;

        //Calculate the width in pixel of one degree
        pixDeg = width/fov;

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

            //Draw terciary line for every 15° excluding every 45° and 90° (15, 30, 60, 75, ...)
            else if (i % 15 == 0){
                drawLine(i, terciaryLineHeight, terciaryLinePaint);
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
     * Used to get the string of the actual heading
     * @param degree degree to get the string
     * @return String for the degree
     */
    private String selectHeadingString(int degree){
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
