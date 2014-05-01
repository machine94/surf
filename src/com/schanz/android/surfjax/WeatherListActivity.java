
package com.schanz.android.surfjax;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class WeatherListActivity extends SingleFragmentActivity
        implements WeatherListFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new WeatherListFragment();
    }

    @Override
    /** res/values/refs.xml */
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    public void onWeatherUpdated(Weather weather) {
        // Update the UI display for list fragments when a weather object is changed
        FragmentManager manager = this.getSupportFragmentManager();
        WeatherListFragment listFragment = (WeatherListFragment)manager
                .findFragmentById(R.id.fragmentContainer);
        listFragment.updateForecast();
    }

    /**
     * Triggered when a weather object is selected by user
     * @param weather A weather forecast object
     */
    @Override
    public void onWeatherSelected(Weather weather) {
        if (findViewById(R.id.detailFragmentContainer) == null) {
            // Start an instance of WeatherPagerActivity
            Intent i = new Intent(this, WeatherPagerActivity.class);
            i.putExtra(WeatherFragment.EXTRA_WEATHER_ID, weather.getId());
            startActivity(i);
        } else {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment oldDetail = manager.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = WeatherFragment.newInstance(weather.getId());

            if (oldDetail != null) {
                ft.remove(oldDetail);
            }

            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }
    }
}
