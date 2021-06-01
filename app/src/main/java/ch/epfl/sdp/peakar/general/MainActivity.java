package ch.epfl.sdp.peakar.general;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Objects;
import java.util.Stack;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments;

import static ch.epfl.sdp.peakar.general.MyPagerAdapter.CAMERA_FRAGMENT_INDEX;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    public static Stack<Integer> lastFragmentIndex;


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
}