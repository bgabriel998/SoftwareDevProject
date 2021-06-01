package ch.epfl.sdp.peakar.general;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Objects;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;
import ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments;

public class MainActivity extends AppCompatActivity {

    FragmentStateAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);
        viewPager.setOffscreenPageLimit(Objects.requireNonNull(viewPager.getAdapter()).getItemCount());
        viewPager.setCurrentItem(2);

        MenuBarHandlerFragments.setup(this, viewPager);
    }
}