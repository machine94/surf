
package com.schanz.android.surfjax;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

/**
 * Swipe pager activity to swipe left and right between existing weather objects
 * 
 * @author kschanz
 *
 */
public class WeatherPagerActivity extends FragmentActivity
        implements WeatherFragment.Callbacks {

    private static final String TAG = "WeatherPagerActivity";

    private ViewPager mViewPager;
    private ArrayList<Weather> mWeathers;

    @Override
    public void onWeatherUpdated(Weather weather) {
        // Do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        mWeathers = WeatherFactory.get(this).getWeathers();

        FragmentManager manager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(manager) {
            @Override
            public int getCount() {
                return mWeathers.size();
            }

            @Override
            public Fragment getItem(int pos) {
                Weather weather = mWeathers.get(pos);
                return WeatherFragment.newInstance(weather.getId());
            }
        });

        /**
         * Implement OnPageChangeListener interface to display weather title
         * in the action bar
         */
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int pos) {
                Weather weather = mWeathers.get(pos);
                /** Tested, works as intended */
                Log.d(TAG, "Weather condition: " + weather.getCondition());
                // Attempt to change activity action bar with weather forecast day
                if (weather.getDay() != null) {
                    setTitle(weather.toString());
                }
            }

            @Override
            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        UUID weatherId = (UUID)getIntent()
                .getSerializableExtra(WeatherFragment.EXTRA_WEATHER_ID);

        for (int i = 0; i < mWeathers.size(); i++) {
            if (mWeathers.get(i).equals(weatherId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
