
package com.schanz.android.surfjax;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Application entry point - populates a list view with weather forecast objects
 * Top fragment is formatted uniquely to display today's forecast
 * 
 * @author kschanz
 *
 */
public class WeatherListFragment extends ListFragment {
    private ArrayList<Weather> mWeathers;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;

    private static final String TAG = "WeatherListFragment";

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onWeatherSelected(Weather weather);
    }

    public void updateForecast() {
        ((WeatherAdapter)getListAdapter()).notifyDataSetChanged();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle(R.string.weathers_title);

        // Generate dummy values
        // mWeathers = WeatherFactory.get(getActivity()).getWeathers();

        // Throwing NPE?
        // WeatherAdapter adapter = new WeatherAdapter(mWeathers);
        // setListAdapter(adapter);

        setRetainInstance(true);
        mSubtitleVisible = false;

        new FetchItemsTask().execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Get the Weather object from the adapter
        Weather w = ((WeatherAdapter)getListAdapter()).getItem(position);
        mCallbacks.onWeatherSelected(w);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_weather_list, menu);

        /** menu-v11 */
        MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible && showSubtitle != null) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_show_subtitle:
                if (getActivity().getActionBar().getSubtitle() == null) {
                    getActivity().getActionBar().setSubtitle(R.string.subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
                } else {
                    getActivity().getActionBar().setSubtitle(null);
                    mSubtitleVisible = false;
                    item.setTitle(R.string.show_subtitle);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Entry point from WeatherListActivity */
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mSubtitleVisible) {
                getActivity().getActionBar().setSubtitle(R.string.subtitle);
            }
        }

        return v;
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
     * Private ArrayAdapter class extension
     * 
     * Configures each list item view of upcoming forecasts
     */
    private class WeatherAdapter extends ArrayAdapter<Weather> {
        public WeatherAdapter(ArrayList<Weather> weathers) {
            super(getActivity(), 0, weathers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // The first forecast is today's forecast, it has a unique layout
            Log.d(TAG, "Creating view at position: " + position);

            // Configure the view for this weather
            Weather w = getItem(position);
            char degree = (char)0x00B0;
            try {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_weather, null);

                TextView titleView = (TextView)convertView
                        .findViewById(R.id.weather_list_item_title);
                if (position == 0) {
                    titleView.setText("Today's Forecast");
                } else if (position == 1) {
                    titleView.setText("Tomorrow's Forecast");
                } else {
                    titleView.setText(String.valueOf(w.getDay() + " Forecast"));
                }

                ImageView conditionView = (ImageView)convertView
                        .findViewById(R.id.weather_condition_thumb);
                conditionView.setImageResource(getConditionResourceId(w));

                TextView highTempView = (TextView)convertView
                        .findViewById(R.id.weather_list_item_high);
                highTempView.setText(String.valueOf(w.getHigh()) + degree + "F");

                TextView lowTempView = (TextView)convertView
                        .findViewById(R.id.weather_list_item_low);
                lowTempView.setText(String.valueOf(w.getLow()) + degree + "F");

                TextView chanceView = (TextView)convertView
                        .findViewById(R.id.weather_list_item_wind);
                chanceView.setText(String.valueOf(w.getWind()) + " mph");
            } catch (NullPointerException npe) {
                Log.e(TAG, "convertView Hash: " + convertView.hashCode());
                Log.e(TAG, "Stack Trace: " + npe.getStackTrace());
            }

            return convertView;
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<Weather>> {
        @Override
        protected ArrayList<Weather> doInBackground(Void... params) {
            Activity activity = getActivity();
            if (activity == null) {
                return new ArrayList<Weather>();
            }
            Log.d(TAG, "Fetching items...");
            return new WundergroundHelper().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<Weather> items) {
            mWeathers = items;
            Toast.makeText(getActivity(), "Successfully loaded forecast from Wunderground!",
                    Toast.LENGTH_LONG).show();

            /**
             * Configure adapter after Weather items are retrieved
             */
            WeatherAdapter adapter = new WeatherAdapter(mWeathers);
            setListAdapter(adapter);

            /**
             * Overwrite existing weather list in factory
             */
            WeatherFactory.get(getActivity()).overwriteWeathers(mWeathers);

            /**
             * Notify the Adapter of a data set change
             */
            updateForecast();
        }
    }
}
