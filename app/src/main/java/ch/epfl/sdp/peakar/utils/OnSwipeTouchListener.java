package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.gallery.GalleryActivity;
import ch.epfl.sdp.peakar.general.SettingsActivity;
import ch.epfl.sdp.peakar.map.MapActivity;
import ch.epfl.sdp.peakar.social.SocialActivity;

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
     * Starts the intent for the left swipe
     * @param intent intent to be started
     */
    public void onSwipeLeft(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    /**
     * Starts the intent for the right swipe
     * @param intent intent to be started
     */
    public void onSwipeRight(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    /**
     * Triggers callback for swipe gestures detection
     *
     * @param v view
     * @param event current motion event
     * @return True if motion has been consumed, else false
     */
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Gets the intent to be started when a left swipe is detected
     * @return Intent to start
     */
    public Intent getIntentLeft() {
        String activityName = context.getClass().getSimpleName();
        switch (activityName){
            case "CameraActivity":
                return new Intent(context, GalleryActivity.class);
            case "GalleryActivity":
                return new Intent(context, SettingsActivity.class);
            case "SocialActivity":
                return new Intent(context, MapActivity.class);
            case "MapActivity":
            default:
                return new Intent(context, CameraActivity.class);
        }
    }

    /**
     * Gets the intent to be started when a right swipe is detected
     * @return Intent to start
     */
    public Intent getIntentRight() {
        String activityName = context.getClass().getSimpleName();
        switch (activityName){
            case "CameraActivity":
                return new Intent(context, MapActivity.class);
            case "MapActivity":
                return new Intent(context, SocialActivity.class);
            case "SettingsActivity":
                return new Intent(context, GalleryActivity.class);
            case "GalleryActivity":
            default:
                return new Intent(context, CameraActivity.class);
        }
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
                    onSwipeRight(getIntentLeft());
                }
                else{
                    onSwipeLeft(getIntentRight());
                }
                return true;
            }
            return false;
        }
    }
}