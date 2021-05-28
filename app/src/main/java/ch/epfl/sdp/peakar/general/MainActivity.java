package ch.epfl.sdp.peakar.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarTransparentBlack;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupTransparentTopBar;

public class MainActivity extends AppCompatActivity {

    FragmentStateAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //StatusBarTransparentBlack(this);
        //setupTransparentTopBar(this, R.color.Black);
        //MenuBarHandler.setup((AppCompatActivity) this);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem(2);
    }
}