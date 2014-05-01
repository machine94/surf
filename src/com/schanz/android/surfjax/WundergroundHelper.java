package com.schanz.android.surfjax;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WundergroundHelper {
	
	private static final String TAG = "WundergroundHelper";
	
	private static final int NUM_DAYS = 3;
	
	private static final String ENDPOINT = "http://api.wunderground.com/api/";
	/** Wunderground API Key for: Kevin Schanz */
	private static final String API_KEY = "fec590462dd8f974";
	private static final String URL_FORECAST = "/forecast/q/";
	
	private static final String URL_STATE = "FL/";
	private static final String URL_CITY  = "Jacksonville.json";
	
	private static final String JSON_FORECAST 		 = "forecast";
	private static final String JSON_TEXT_FORECAST 	 = "txt_forecast";
	private static final String JSON_SIMPLE_FORECAST = "simpleforecast";
	private static final String JSON_FORECASTDAY 	 = "forecastday";
	
	private static final String JSON_DATE		= "date";
	private static final String JSON_WIND		= "avewind";
	private static final String JSON_HIGH		= "high";
	private static final String JSON_LOW		= "low";
	
	private static final String TEMP_UNIT		= "fahrenheit";
	
	private static final String FORECAST_TITLE		= "title";
	private static final String FORECAST_TEXT		= "fcttext";
	
	private static final String COND_SUNNY  = "Sunny";
	private static final String COND_CLEAR  = "Clear";
	private static final String COND_RAINY  = "Rainy";
	private static final String COND_CLOUDY = "Cloudy";
	
	// http://api.wunderground.com/api/######/forecast/q/STATE/City_Name.json
	
	private String retrieveJSON(String urlJSON) {
		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost(urlJSON);
		
		// Depends on your web service
		httppost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		String result = null;
		try {
		    HttpResponse response = httpclient.execute(httppost);           
		    HttpEntity entity = response.getEntity();

		    inputStream = entity.getContent();
		    
		    // json is UTF-8 by default
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null) {
		        sb.append(line + "\n");
		    }
		    // This is the JSON
		    result = sb.toString();	    
		} catch (Exception e) { 
			Log.e(TAG, "Exception occurred while retrieving JSON" + e.getMessage());
		} finally {
		    try {
		    	if (inputStream != null) inputStream.close();
		    } catch(Exception exc) {
		    	Log.e(TAG, "Exception occurred while retrieving JSON" + exc.getMessage());
		    }
		}

		return result;
	}
	
	/** Entry point from WeatherListFragment */
    public ArrayList<Weather> fetchItems() {
        ArrayList<Weather> items = new ArrayList<Weather>();
        
        try {
        	String url = ENDPOINT;
        	url += API_KEY;
        	url += URL_FORECAST + URL_STATE + URL_CITY;
        	
        	JSONObject forecastResults = new JSONObject(retrieveJSON(url));
		    
		    /** testing output */
		    logJSONData(forecastResults);
		    
		    decodeJSONWeather(forecastResults, items, NUM_DAYS);
        } catch (Exception ioe) {
            Log.e(TAG, "Failed to fetch items " + ioe.getMessage());
        } 
        return items;
    }	
    
    private void decodeJSONWeather(JSONObject data, ArrayList<Weather> items, int days) {
		try {		
			JSONObject forecast = data.getJSONObject(JSON_FORECAST);
			
			/**
			 * VERBOSE TEXT FORECAST
			 */
			JSONObject textForecast = forecast.getJSONObject(JSON_TEXT_FORECAST);
			JSONArray textDayArray = textForecast.getJSONArray(JSON_FORECASTDAY);
			
			/**
			 * DATA ELEMENTS FORECAST
			 */
			JSONObject simpleForecast = forecast.getJSONObject(JSON_SIMPLE_FORECAST);
			JSONArray simpleDayArray = simpleForecast.getJSONArray(JSON_FORECASTDAY);
			
			// Helper variable to skip nightly forecasts
			int skipNightly = 0;
			for (int i = 0; i < NUM_DAYS; i++) {
				Weather w = new Weather();

				
				/** 
				 * We only want elements 0, 2, 4 from the verbose forecast 
				 * to exclude nightly forecasts
				 */
				JSONObject verboseForecast = textDayArray.getJSONObject(skipNightly);	
				Log.d(TAG, "#" + skipNightly + " Title: " + verboseForecast.getString(FORECAST_TITLE));
				Log.d(TAG, "#" + skipNightly + " Text: " + verboseForecast.getString(FORECAST_TEXT));	
				skipNightly += 2;
				
				/**
				 * Extract data elements from the simple version of the forecast
				 */
				JSONObject dataForecast = simpleDayArray.getJSONObject(i);
				
				JSONObject date = dataForecast.getJSONObject(JSON_DATE);
				JSONObject high = dataForecast.getJSONObject(JSON_HIGH);
				JSONObject low = dataForecast.getJSONObject(JSON_LOW);
				JSONObject wind = dataForecast.getJSONObject(JSON_WIND);
				
				/**
				 * Construct date from Wunderground JSON Date object
				 */
				int year = date.getInt("year");
				int month = date.getInt("month");
				int day = date.getInt("day");
				Log.d(TAG, "DAY: " + day);
				Calendar calendar = Calendar.getInstance();
				calendar.clear();
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month - 1);
				calendar.set(Calendar.DAY_OF_MONTH, day);
				Date forecastDate = calendar.getTime();		
				
				/**
				 * Search the verbose forecast for condition keywords
				 */
				String condition = extractConditionString
						(verboseForecast.getString(FORECAST_TEXT));
				
				/**
				 * Configure Weather object
				 */
				w.setDay(verboseForecast.getString(FORECAST_TITLE));
				w.setForecastText(verboseForecast.getString(FORECAST_TEXT));
				w.setCondition(condition);
				w.setDate(forecastDate);
				w.setHigh(high.getInt(TEMP_UNIT));
				w.setLow(low.getInt(TEMP_UNIT));
				w.setWind(wind.getInt("mph"));
				w.setHumidity(dataForecast.getInt("avehumidity"));
				
				/**
				 * Add Weather object to ArrayList
				 */
				items.add(w);
			}
			
			Log.d(TAG, "======");
			for (Weather w : items) {
				Log.d(TAG, "Weather: " + w.getDay());
				Log.d(TAG, "Date: " + w.toString());
				Log.d(TAG, "Condition: " + w.getCondition());
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error decoding JSON!");
		}
    }
    
    private String extractConditionString(String verboseForecast) {
		String search = verboseForecast.toLowerCase(Locale.getDefault());
		
		
		if (search.contains(COND_SUNNY.toLowerCase(Locale.getDefault())) ||
				search.contains(COND_CLEAR.toLowerCase(Locale.getDefault()))) {
			return COND_SUNNY;
		} else if (search.contains(COND_CLOUDY.toLowerCase(Locale.getDefault()))) {
			return COND_CLOUDY;
		} else {
			return COND_RAINY;
		}
    }
    
	void logJSONData(JSONObject data) {
		Log.d(TAG, "==============================");
		Log.d(TAG, "======   BEGINNING LOG    ====");
		Log.d(TAG, "==============================");
		
		try {		
			JSONObject forecast = data.getJSONObject(JSON_FORECAST);
			
			/**
			 * VERBOSE TEXT FORECAST
			 */
			JSONObject textForecast = forecast.getJSONObject(JSON_TEXT_FORECAST);
			
			Log.d(TAG, "==============================");
			Log.d(TAG, "txt_forecast: " + textForecast.toString());
			
			JSONArray forecastdayArray = textForecast.getJSONArray(JSON_FORECASTDAY);
			
			for (int i = 0; i < forecastdayArray.length(); i++) {
				JSONObject item = forecastdayArray.getJSONObject(i);
				
				Log.d(TAG, "#" + i + " Title: " + item.getString(FORECAST_TITLE));
				Log.d(TAG, "#" + i + " Text: " + item.getString(FORECAST_TEXT));
			}
			
			
			/**
			 * DATA ELEMENTS FORECAST
			 */
			JSONObject simpleForecast = forecast.getJSONObject(JSON_SIMPLE_FORECAST);
			
			Log.d(TAG, "==============================");
			
			forecastdayArray = simpleForecast.getJSONArray(JSON_FORECASTDAY);
			
			for (int i = 0; i < forecastdayArray.length(); i++) {
				JSONObject outerItem = forecastdayArray.getJSONObject(i);
				JSONObject high = outerItem.getJSONObject(JSON_HIGH);
				JSONObject low = outerItem.getJSONObject(JSON_LOW);
				
				Log.d(TAG, "#" + i + " High: " + high.getString(TEMP_UNIT));
				Log.d(TAG, "#" + i + " Low: " + low.getString(TEMP_UNIT));	
			}
		} catch (JSONException e) {
			Log.e(TAG, "Data Log error!");
		}
	}
}
