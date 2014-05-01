
package com.schanz.android.surfjax;

import java.util.Locale;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * WeatherFragment is for Pager details extra details view for individual forecasts
 * 
 * @author kschanz
 *
 */
public class WeatherFragment extends Fragment {

    /** Interface for fragment communication */
    @SuppressWarnings("unused")
    private Callbacks mCallbacks;

    public static final String EXTRA_WEATHER_ID =
            "com.bignerdranch.android.weatherapp.weather_id";

    private static final String TAG = "WeatherFragment";

    private Weather mWeather;
    private TextView mDayView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mChanceView;
    private TextView mHumidityView;
    private TextView mSummaryView;
    private Button mNWSButton;
    private ImageView mConditionView;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onWeatherUpdated(Weather weather);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static WeatherFragment newInstance(UUID weatherId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_WEATHER_ID, weatherId);

        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Toast.makeText(getView().getContext(), "onActivityResult: " + resultCode,
                Toast.LENGTH_SHORT).show();
        // Stub for potential dialog choices
        // Handle returned dialog choices
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID weatherId = (UUID)getArguments().getSerializable(EXTRA_WEATHER_ID);
        mWeather = WeatherFactory.get(getActivity()).getWeather(weatherId);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Locale.getDefault() is to satisfy Lint... */
    private int getConditionResourceId(Weather w) {
        if (w.getCondition().toLowerCase(Locale.getDefault()).equals("cloudy")) {
            return R.drawable.cloudy;
        } else if (w.getCondition().toLowerCase(Locale.getDefault()).equals("rainy")) {
            return R.drawable.rainy;
        } else {
            return R.drawable.sunny;
        }
    }

    /**
     * Constructs pager view - larger fonts, more forecast details
     */
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather, parent, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        char degree = (char)0x00B0;

        mDayView = (TextView)v.findViewById(R.id.weather_view_day);
        mDayView.setText(mWeather.getDay() + " Forecast");

        mConditionView = (ImageView)v.findViewById(R.id.weather_condition_thumb);
        mConditionView.setImageResource(getConditionResourceId(mWeather));

        Log.d(TAG, "Current weather condition: " + mWeather.getCondition());

        mHighTempView = (TextView)v.findViewById(R.id.weather_view_high);
        mHighTempView.setText(String.valueOf(mWeather.getHigh()) + degree + "F");

        mLowTempView = (TextView)v.findViewById(R.id.weather_view_low);
        mLowTempView.setText(String.valueOf(mWeather.getLow()) + degree + "F");

        mChanceView = (TextView)v.findViewById(R.id.weather_view_wind);
        mChanceView.setText(String.valueOf(mWeather.getWind()) + " mph");

        mHumidityView = (TextView)v.findViewById(R.id.weather_view_humidity);
        mHumidityView.setText(String.valueOf(mWeather.getHumidity()) + "%");

        mSummaryView = (TextView)v.findViewById(R.id.weather_view_summary);
        mSummaryView.setText(mWeather.getForecastText());

        mNWSButton = (Button)v.findViewById(R.id.weather_button_nws);
        mNWSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getActivity(), "You win!", Toast.LENGTH_LONG);
                TextView tv = (TextView)toast.getView().findViewById(android.R.id.message);
                tv.setBackgroundColor(Color.BLUE);
                tv.setTextColor(Color.WHITE);
                toast.show();
            }
        });

        Log.d(TAG, "Weather Detail Fragment: " + mWeather.toString());

        return v;
    } // onCreateView(LayoutInflater, ViewGroup, Bundle)

    @Override
    public void onStart() {
        super.onStart();
        mConditionView.setImageResource(getConditionResourceId(mWeather));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
