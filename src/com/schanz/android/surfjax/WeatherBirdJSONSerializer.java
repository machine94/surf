package com.schanz.android.surfjax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;

public class WeatherBirdJSONSerializer {

	private Context mContext;
	private String mFilename;
	
	public WeatherBirdJSONSerializer(Context c, String f) {
		mContext = c;
		mFilename = f;
	}
	
	/**
	 * Method attempts to load/retrieve an array of weather objects from storage.
	 * @return ArrayList<Weather>
	 * @throws IOException
	 * @throws JSONException
	 */
	public ArrayList<Weather> loadWeathers() throws IOException, JSONException {
		ArrayList<Weather> weathers = new ArrayList<Weather>();
		BufferedReader reader = null;
		
		try {
			// Open and read
			InputStream in = mContext.openFileInput(mFilename);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Omit line breaks
				jsonString.append(line);
			}
			
			// Parse the JSON with JSONTokener
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			
			// Build the array of weather objects from JSONObjects
			for (int i = 0; i < array.length(); i++) {
				weathers.add(new Weather(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// Occurs when starting fresh
		} finally {
			if (reader != null)
				reader.close();
		}
		
		return weathers;
	}
	
	/**
	 * Method saves an array of weather objects to storage.
	 * 
	 * @param weathers ArrayList of weather objects to be written to storage
	 * @throws JSONException
	 * @throws IOException
	 */
	public void saveWeathers(ArrayList<Weather> weathers) throws JSONException, IOException {
		// Build array in JSON
		JSONArray array = new JSONArray();
		for (Weather w : weathers) {
			array.put(w.toJSON());
		}
		
		// Write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
