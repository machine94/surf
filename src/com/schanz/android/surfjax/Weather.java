package com.schanz.android.surfjax;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;

public class Weather {

	public static final String TAG = "Weather";
	
	private static final String JSON_ID    	= "id";
	private static final String JSON_DATE 	= "date";
	private static final String JSON_DAY    = "day";
	private static final String JSON_COND   = "condition";
	private static final String JSON_HIGH  	= "high";
	private static final String JSON_LOW   	= "low";
	
	private UUID mId;
	private Date mDate;
	private String mUrl;
	private String mCondition;
	private String mDay;
	private String mForecastText;
	private int mHumidity;
	private int mWind;
	private int mHigh;
	private int mLow;
	
	public Weather() {
		// Generate unique identifier
		mId = UUID.randomUUID();
		mDate = new Date();
		
		// Use dummy default values, reduce risk of null pointer exceptions
		mCondition = "Sunny";
		mHumidity = 70;
		mWind = 12;
	}
	
	/**
	 * Just in case we need to retrieve saved weathers from storage
	 * 
	 * @param json
	 * @throws JSONException
	 */
	public Weather(JSONObject json) throws JSONException {
		mId = UUID.fromString(json.getString(JSON_ID));
		mDate = new Date(json.getLong(JSON_DATE));
		mCondition = json.getString(JSON_COND);
		mDay = json.getString(JSON_DAY);
	}
	
	/**
	 * Convert a weather object to JSON object to save to file
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId.toString());
		json.put(JSON_DATE, mDate);
		json.put(JSON_DAY, mDay);
		json.put(JSON_COND, mCondition);
		json.put(JSON_HIGH, mHigh);
		json.put(JSON_LOW, mLow);
		
		return json;
	}
	
	/**
	 * Returns a String formatted like Jan 20, 2014
	 */
	@Override
	public String toString() {
		return (String)DateFormat.format("MMM dd, yyyy", this.getDate());
	}
	
	public String getForecastText() {
		return mForecastText;
	}

	public int getHumidity() {
		return mHumidity;
	}

	public void setHumidity(int humidity) {
		mHumidity = humidity;
	}

	public void setForecastText(String forecastText) {
		mForecastText = forecastText;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public UUID getId() {
		return mId;
	}

	public void setId(UUID id) {
		mId = id;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public String getCondition() {
		return mCondition;
	}

	public void setCondition(String condition) {
		mCondition = condition;
	}

	public String getDay() {
		return mDay;
	}

	public void setDay(String day) {
		mDay = day;
	}

	public int getHigh() {
		return mHigh;
	}

	public void setHigh(int high) {
		mHigh = high;
	}

	public int getLow() {
		return mLow;
	}

	public void setLow(int low) {
		mLow = low;
	}

	public int getWind() {
		return mWind;
	}

	public void setWind(int wind) {
		mWind = wind;
	}	
}
