package ch.epfl.sdp.peakar.general;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Objects;
import java.util.Stack;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.UserPoint;
import ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments;
import ch.epfl.sdp.peakar.utils.MainPagerAdapter;

import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.CAMERA_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.PermissionUtilities.hasLocationPermission;

/**
 * MainsActivity displays displays all the different pages with the ViewPager2 and the menu-bar
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    public static Stack<Integer> lastFragmentIndex;
    private boolean locationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lastFragmentIndex = new Stack<>();

        viewPager = findViewById(R.id.viewPager);
        FragmentStateAdapter adapterViewPager = new MainPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);
        viewPager.setOffscreenPageLimit(Objects.requireNonNull(viewPager.getAdapter()).getItemCount());

        Bundle b = getIntent().getExtras();
        viewPager.setCurrentItem(b != null ? b.getInt(getString(R.string.setPagerOnRestart)) : CAMERA_FRAGMENT_INDEX);

        MenuBarHandlerFragments.setup(this, viewPager);
    }

    /**
     * Sets the viewPager to a certain item, used to change the item from the fragments directly
     *
     * @param position new item to be displayed
     */
    public void setCurrentPagerItem(int position){
        if(viewPager!=null){
            viewPager.setCurrentItem(position);
        }
    }

    /**
     * Gets the viewPager current item
     *
     * @return position of current item
     */
    public int getCurrentPagerItem(){
        return viewPager.getCurrentItem();
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

    @Override
    protected void onPause() {
        super.onPause();
        locationPermission = hasLocationPermission(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!locationPermission && hasLocationPermission(this)){
            UserPoint userPoint = UserPoint.getInstance(this);
            userPoint.updateGPSTracker(this);
            userPoint.update();
        }
    }
}