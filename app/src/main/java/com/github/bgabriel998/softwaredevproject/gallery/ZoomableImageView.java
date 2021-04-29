package com.github.bgabriel998.softwaredevproject.gallery;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class that extends image views to make the zoomable
 */
public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {

    private final static int CLICK_DISTANCE_THRESH = 3;
    private final static float MIN_SCALE = 1f;
    private final static float MAX_SCALE = 10f;

    // The view can be in different stages to control zooming
    private enum Mode {
        None, Drag, Zoom
    }
    private Mode mode;

    // Used scale on pinch gesture
    private ScaleGestureDetector scaleGestureDetector;
    private Matrix imageMatrix;

    // Remember Touches
    private PointF start;
    private PointF last;

    // Width and Height, scale
    private float currScale;
    private int viewWidth, viewHeight;
    private float origWidth, origHeight;
    private float currWidth, currHeight;

    /**
     * Constructor
     * @param context context
     */
    public ZoomableImageView(@NonNull Context context) {
        super(context);
        constructor(context);
    }

    /**
     * Constructor with attributes
     * @param context context
     * @param attrs attributes
     */
    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        constructor(context);
    }

    /**
     * Constructor with attributes and style attribute
     * @param context context
     * @param attrs attributes
     * @param defStyleAttr style attribute
     */
    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context);
    }

    /**
     * Constructor for class, called by all super constructors from image view.
     * @param context the context
     */
    private void constructor(Context context) {
        super.setClickable(true);
        mode = Mode.None;
        currScale = MIN_SCALE;
        scaleGestureDetector = new ScaleGestureDetector(context, new ZoomListener());

        // Setup matrix
        imageMatrix = new Matrix();
        setImageMatrix(imageMatrix);
        setScaleType(ScaleType.MATRIX);

        // Setup touches
        start = new PointF();
        last = new PointF();

        setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            PointF currTouch = new PointF(event.getX(), event.getY());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    firstTouch(currTouch);
                    break;

                case MotionEvent.ACTION_MOVE :
                    drag(currTouch);
                    break;

                case MotionEvent.ACTION_UP :
                    mode = Mode.None;
                    int xDiff = (int) Math.abs(currTouch.x - start.x);
                    int yDiff = (int) Math.abs(currTouch.y - start.y);
                    if (xDiff < CLICK_DISTANCE_THRESH &&
                            yDiff < CLICK_DISTANCE_THRESH) {
                        v.performClick();
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP :
                    mode = Mode.None;
                    break;
            }

            setImageMatrix(imageMatrix);
            invalidate();
            return true;
        });
    }

    /**
     * Gets the current scale, used to test zoom functionality
     * @return current scale.
     */
    public float getCurrentScale() {
        return currScale;
    }

    /**
     * Called on first touch
     * Sets mode and touch positions.
     * @param currTouch the current/first touch
     */
    private void firstTouch(PointF currTouch) {
        start.set(currTouch);
        last.set(currTouch);
        mode = Mode.Drag;
    }

    /**
     * Preform a drag of a touch, by translating the matrix
     * @param currTouch current touch position
     */
    private void drag(PointF currTouch) {
        if (mode == Mode.Drag) {
            float deltaX = (viewWidth < currWidth) ? currTouch.x - last.x : 0;
            float deltaY = (viewHeight < currHeight) ? currTouch.y - last.y : 0;
            imageMatrix.postTranslate(deltaX, deltaY);
            fixMatrixTranslation();
            last.set(currTouch);
        }
    }

    /**
     * Fix the matrix Translation
     */
    private void fixMatrixTranslation() {
        float[] m = new float[9];
        imageMatrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixedTransX = getFixTrans(transX, viewWidth, currWidth);
        float fixedTransY = getFixTrans(transY, viewHeight, currHeight);
        if (fixedTransX != 0 || fixedTransY != 0) {
            imageMatrix.postTranslate(fixedTransX, fixedTransY);
        }
    }

    /**
     * Based on a trans point, get the fixed point
     * @param trans given trans point
     * @param viewSize the view size
     * @param currSize the current scaled size
     * @return the trans point fixed
     */
    private float getFixTrans(float trans, float viewSize, float currSize) {
        float minTrans, maxTrans;

        if (currSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - currSize;
        }
        else {
            minTrans = viewSize - currSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {
            return minTrans - trans;
        }
        if (trans > maxTrans) {
            return maxTrans - trans;
        }
        return 0;
    }

    /**
     * Called when view is not fitting bounds.
     * Sets new sizes and rescales if needed.
     * @param widthMeasureSpec width
     * @param heightMeasureSpec height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Rescale if rotated
        if (MeasureSpec.getSize(widthMeasureSpec) != viewWidth ||
                MeasureSpec.getSize(heightMeasureSpec) != viewHeight) {
            viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            viewHeight = MeasureSpec.getSize(heightMeasureSpec);
            rescale();
        }

    }

    /**
     * Rescale everything The full image
     */
    private void rescale() {
        if (viewWidth != 0 && viewHeight != 0 && currScale == 1){
            Drawable drawable = getDrawable();
            if (drawable != null && drawable.getIntrinsicWidth() != 0 &&
                    drawable.getIntrinsicHeight() != 0) {
                float bmWidth = (float) drawable.getIntrinsicWidth();
                float bmHeight = (float) drawable.getIntrinsicHeight();

                float scaleX = (float) viewWidth / bmWidth;
                float scaleY = (float) viewHeight / bmHeight;
                float scale = Math.min(scaleX, scaleY);
                imageMatrix.setScale(scale, scale);

                // Center Image
                float redundantX = ((float) viewWidth - (scale * bmWidth)) / 2;
                float redundantY = ((float) viewHeight - (scale * bmHeight)) / 2;
                imageMatrix.postTranslate(redundantX, redundantY);

                origWidth = viewWidth - 2 * redundantX;
                origHeight = viewHeight - 2 * redundantY;
                setImageMatrix(imageMatrix);
            }
        }
        fixMatrixTranslation();
    }

    /**
     * Extended scale gesture detector to preform zooming.
     */
    private class ZoomListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        /**
         * Starts zoom
         * @param detector unsused
         * @return always true
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = Mode.Zoom;
            return true;
        }

        /**
         * Preforms zoom
         * @param detector the detector to get X,Y and scale factor
         * @return always true
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = setScale(detector.getScaleFactor());
            setCurrWidthHeight();

            if (currWidth <= viewWidth || currHeight <= viewHeight) {
                imageMatrix.postScale(scaleFactor, scaleFactor,
                        (float) viewWidth / 2, (float) viewHeight / 2);
            }
            else {
                imageMatrix.postScale(scaleFactor, scaleFactor,
                        detector.getFocusX(), detector.getFocusY());
            }
            fixMatrixTranslation();
            return true;
        }

        /**
         * Sets the current scale and returns the scale factor
         * @param scaleFactor gets the detector scale factor
         * @return the scale factor that will be used,
         */
        private float setScale(float scaleFactor) {
            float lastScale = currScale;
            currScale *= scaleFactor;
            // If scale is too big set to Max
            if (currScale > MAX_SCALE) {
                currScale = MAX_SCALE;
                scaleFactor = MAX_SCALE / lastScale;
            }
            // If scale is too small set to min
            else if (currScale < MIN_SCALE) {
                currScale = MIN_SCALE;
                scaleFactor = MIN_SCALE / lastScale;
            }
            return scaleFactor;
        }

        /**
         * Sets the current width and height.
         */
        private void setCurrWidthHeight() {
            currWidth = origWidth * currScale;
            currHeight = origHeight * currScale;
        }
    }
}
