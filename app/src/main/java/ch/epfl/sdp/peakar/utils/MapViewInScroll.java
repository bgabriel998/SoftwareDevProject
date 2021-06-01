package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;

/**
 * Used to override the dispatchEvent to handle the swipes on the MapView to be able to use the app
 * while being able to use the ViewPager2 to change the activities when swiping from the edge
 */
public class MapViewInScroll extends MapView {

    private static final double BORDER_PERCENTAGE = 0.1;
    private boolean intercepMove = false;

    /**
     * Constructor of MapViewInScroll
     * @param context context
     */
    public MapViewInScroll(Context context) {
        super(context);
    }

    /**
     * Constructor of MapViewInScroll
     * @param context context
     * @param attributeSet attributeSet
     */
    public MapViewInScroll(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepMove = ev.getX() > BORDER_PERCENTAGE * getWidth() && ev.getX() < (1-BORDER_PERCENTAGE) * getWidth();
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(intercepMove);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}