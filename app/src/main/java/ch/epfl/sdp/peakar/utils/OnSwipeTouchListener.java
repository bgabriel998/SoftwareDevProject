package ch.epfl.sdp.peakar.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ch.epfl.sdp.peakar.map.MapActivity;

import static ch.epfl.sdp.peakar.utils.MenuBarHandler.onSwipeLeftToRight;
import static ch.epfl.sdp.peakar.utils.MenuBarHandler.onSwipeRightToLeft;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final Context context;

    /**
     * Constructor of the OnSwipeListener, creates the GestureListener
     * @param context context of the application
     */
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.context = context;
    }

    /**
     * Use onClick to override a simple click event
     */
    public void onClick(){}

    /**
     * Triggers callback for swipe gestures detection
     *
     * @param v view
     * @param event current motion event
     * @return True if motion has been consumed, else false
     */
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * GestureListener that handles left and right swipes
     */
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private static final int SCREEN_MARGIN_EDGE = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float startX = e1.getX();
            float endX = e2.getX();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int sizeX = dm.widthPixels;
            float distanceX = endX - startX;
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if(context.getClass().getSimpleName().equals(MapActivity.class.getSimpleName())){
                    //If current activity is MapActivity then check that swipe was made starting form the edge
                    if(startX > SCREEN_MARGIN_EDGE && startX < sizeX - SCREEN_MARGIN_EDGE){
                        return false;
                    }
                }
                if (distanceX > 0){
                    onSwipeLeftToRight(context);
                }
                else{
                    onSwipeRightToLeft(context);
                }
                return true;
            }
            return false;
        }
    }
}