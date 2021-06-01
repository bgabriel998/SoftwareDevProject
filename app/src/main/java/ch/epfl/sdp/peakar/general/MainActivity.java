package ch.epfl.sdp.peakar.general;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Objects;
import java.util.Stack;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments;

import static ch.epfl.sdp.peakar.general.MyPagerAdapter.CAMERA_FRAGMENT_INDEX;

public class MainActivity extends AppCompatActivity {

    private static final int SCREEN_MARGIN_EDGE = 100;
    private ViewPager2 viewPager;
    public static Stack<Integer> lastFragmentIndex;
    private boolean intercept = false;
    private double slideBorder = 0.1;
    private double width;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lastFragmentIndex = new Stack<>();

        viewPager = findViewById(R.id.viewPager);
        FragmentStateAdapter adapterViewPager = new MyPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);
        viewPager.setOffscreenPageLimit(Objects.requireNonNull(viewPager.getAdapter()).getItemCount());
        viewPager.setCurrentItem(CAMERA_FRAGMENT_INDEX);

        MenuBarHandlerFragments.setup(this, viewPager);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
    }

    @Override
    public void onBackPressed() {
        //Remove duplicates
        if(lastFragmentIndex.size()>=2){
            int lastFragment = lastFragmentIndex.pop();
            int nextFragment = lastFragment == viewPager.getCurrentItem() ? lastFragmentIndex.pop() : lastFragment;
            viewPager.setCurrentItem(nextFragment);
        }
        else{
            super.onBackPressed();
        }
    }

    // true if we intercep MOVE events in order to prevent the view pager to swipe views
    private boolean intercepMove = false;

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Prevent the ViewPager to swipe instead of scrolling
//        // See https://stackoverflow.com/questions/8594361/horizontal-scroll-view-inside-viewpager
//        // Touching the borders allow the view pager to swipe
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                // See if we touch the screen borders
//                intercepMove = 100 * event.getX() > 5 * width && 100 * event.getX() < 95 * width;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                intercepMove = 100 * event.getX() > 5 * width && 100 * event.getX() < 95 * width;
//                if (intercepMove && getParent() != null) {
//                    viewPager.requestDisallowInterceptTouchEvent(true);
//                }
//        }
//        return false;
//    }
}