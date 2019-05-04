package eu.bidtrans.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import eu.bidtrans.sunshine.domain.Units;

public class ForecastFragment extends Fragment {
    private static final String TAG = ForecastFragment.class.getSimpleName();
    public static final String KEY_WEATHER = "u.bidtrans.sunshine.weather";

    private ArrayAdapter<String> mForecastAdapter;
    private ListView mForecastListView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private WeatherLocation getLocationFromSettings() {
        WeatherLocation location = new WeatherLocation("077190", "RO");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs != null) {
            location.setCountryCode(prefs.getString(getString(R.string.country_code_key), "RO"));
            location.setZipCode(prefs.getString(getString(R.string.zip_code_key), "077190"));
        }
        return location;
    }

    private Units getUnitFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs != null) {
            String value = prefs.getString(getString(R.string.unit_key), Units.metric.toString());
            return Units.fromString(value);
        } else {
            Log.w(TAG, "cannot read SharedPreferences");
            return Units.metric;
        }
    }

    private void refreshWeather() {
        FetchWeatherData fetchDataTask = new FetchWeatherData();
        WeatherLocation location = getLocationFromSettings();
        Units unit = getUnitFromSettings();
        fetchDataTask.execute(location.getCountryCode(), location.getZipCode(), unit.toString());
    }

    private void showLocationOnMap(WeatherLocation location) {
        Uri uri = Uri.parse(String.format("geo:0,0?q=zip+%s+country+%s", location.getZipCode(), location.getCountryCode()));
        Log.d(TAG, "showLocation - URI is " + uri.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else
            Toast.makeText(getContext(), "Unable to find a MAP application", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshWeather();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (item.getItemId() == R.id.action_map) {
            showLocationOnMap(getLocationFromSettings());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshWeather();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<String> forecastData = new ArrayList<String>();
        mForecastAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastData);
        View fragment = inflater.inflate(R.layout.forecast_fragment, container, false);
        mForecastListView = (ListView) fragment.findViewById(R.id.forecast_listview);
        mForecastListView.setAdapter(mForecastAdapter);
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getContext(), String.format("You just clicked on %s", mForecastAdapter.getItem(position)), Toast.LENGTH_LONG).show();
                startDetailActivity(mForecastAdapter.getItem(position));
            }

        });

        return fragment;
    }

    private void startDetailActivity(String data) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(KEY_WEATHER, data);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    private class WeatherLocation {
        private String zipCode;
        private String countryCode;

        public WeatherLocation(String zipCode, String countryCode) {
            this.zipCode = zipCode;
            this.countryCode = countryCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public WeatherLocation() {
        }

        public String getZipCode() {
            return zipCode;
        }

        public String getCountryCode() {
            return countryCode;
        }
    }

    private class FetchWeatherData extends AsyncTask<String, Void, String[]> {

        private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

        private static final String PARAM_ZIP_CODE = "zip";
        private static final String PARAM_MODE = "json";
        private static final String PARAM_UNITS = "units";
        private static final String PARAM_DAYS_COUNT = "cnt";
        private static final String PARAM_API_KEY = "appid";

        private String getWeatherData(String countryCode, String postalCode) {
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
            if (params.length == 0)
                return null;
            String jsonData = getWeatherData(params[0], params[1]);
            Log.d(TAG, "json received " + jsonData);
            String[] weather = null;
            try {
                weather = WeatherDataParser.getWeatherDataFromJson(jsonData, Units.fromString(params[2]));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String[] s) {
            if (s != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(s);
            }
        }
    }
}