package eu.bidtrans.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends Fragment {
    private static final String TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;
    private ListView mForecastListView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            FetchWeatherData fetchDataTask = new FetchWeatherData();
            fetchDataTask.execute("RO", "077190");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<String> forecastData = new ArrayList<String>(Arrays.asList("Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"));
        mForecastAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastData);

        View fragment = inflater.inflate(R.layout.forecast_fragment, container, false);
        mForecastListView = (ListView) fragment.findViewById(R.id.forecast_listview);
        mForecastListView.setAdapter(mForecastAdapter);

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    private class FetchWeatherData extends AsyncTask<String, Void, String[]> {

        private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

        private static final String PARAM_ZIP_CODE = "zip";
        private static final String PARAM_MODE = "json";
        private static final String PARAM_UNITS = "units";
        private static final String PARAM_DAYS_COUNT = "cnt";
        private static final String PARAM_API_KEY = "appid";

        private String getWeatherData(String countryCode ,String postalCode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                Uri.Builder builder = Uri.parse(WEATHER_URL).buildUpon();
                Uri uri = builder.appendQueryParameter(PARAM_ZIP_CODE, postalCode + "," + countryCode)
                        .appendQueryParameter(PARAM_MODE, "json")
                        .appendQueryParameter(PARAM_UNITS, "metric")
                        .appendQueryParameter(PARAM_DAYS_COUNT, "7")
                        .appendQueryParameter(PARAM_API_KEY, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();
                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String jsonData = getWeatherData(params[0] , params[1]);
            String[] weather = null;
            try {
                weather = WeatherDataParser.getWeatherDataFromJson(jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String[] s) {
            if (s != null) {
                Log.d(TAG, "data received : ");
                for (int i =0;i < s.length; i++) {
                    Log.d(TAG, i + " - " + s[i]);
                }
            }

        }
    }
}