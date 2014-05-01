package com.schanz.android.surfjax;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

/** Singleton class */
public class WeatherFactory {
	
	private static final String TAG = "WeatherFactory";
	private static final String FILENAME = "Weather.json";
	
	public static final String SUNNY = "Sunny";
	public static final String RAINY = "Rainy";
	public static final String CLOUDY = "Cloudy";
	
	private ArrayList<Weather> mWeathers;
	private WeatherBirdJSONSerializer mSerializer;
	
	private static WeatherFactory sWeatherFactory;
	private Context mAppContext;
	
	private WeatherFactory(Context appContext) {
		mAppContext = appContext;
		mSerializer = new WeatherBirdJSONSerializer(mAppContext, FILENAME);
		
		mWeathers = new ArrayList<Weather>();
	}
	
	/**
	 * Unit test method for generating dummy weather objects.
	 * 
	 * @param appContext
	 * @param generate True to generate dummy values
	 */
	private WeatherFactory(Context appContext, boolean generate) {
		mAppContext = appContext;
		mSerializer = new WeatherBirdJSONSerializer(mAppContext, FILENAME);
		
		int j = 5;
		
		if (generate) {
			mWeathers = new ArrayList<Weather>();
			for (int i = 0; i < 20; i++) {
				Weather w = new Weather();
				w.setHigh(i + 20);
				w.setLow(i + 5);
				w.setWind(j + 6);
				if (i % 3 == 0) {
					w.setCondition(SUNNY);
					w.setDay("Monday");
				} else if (i % 3 == 1){
					w.setCondition(CLOUDY);
					w.setDay("Tuesday");
				} else {
					w.setCondition(RAINY);
					w.setDay("Wednesday");
				}
				mWeathers.add(w);
				Log.d(TAG, "Added Weather: " + w.getHigh() + " Low: " + w.getLow() + 
						w.getCondition());
				
				j += 0.08f;
			}
		}
	}
	
	public ArrayList<Weather> getWeathers() {
		return mWeathers;
	}
	
	public void addWeather(Weather w) {
		mWeathers.add(w);
	}
	
	public void deleteWeather(Weather w) {
		mWeathers.remove(w);
	}
	
	public void overwriteWeathers(ArrayList<Weather> newList) {
		mWeathers.clear();
		mWeathers = newList;
	}
	
	public Weather getWeather(UUID id) {
		for (Weather w : mWeathers) {
			if (w.getId().equals(id)) {
				return w;
			}
		}
		return null;
	}
	
	public static WeatherFactory get(Context c) {
		// If an instance doesn't exist yet, create a new one
		if (sWeatherFactory == null) {
			// Initially triggered by WeatherListActivity 
			sWeatherFactory = new WeatherFactory(c.getApplicationContext());
		}
		// Otherwise, return the only instance of the factory
		return sWeatherFactory;
	}
	
	public boolean saveWeathers() {
		try {
			mSerializer.saveWeathers(mWeathers);
			Log.d(TAG, "weathers saved to file");
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Error saving weather objects: " + e.getMessage());
			return false;
		}
	}
}
