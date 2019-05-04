package eu.bidtrans.sunshine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import eu.bidtrans.sunshine.domain.Units;

public class WeatherDataParser {

    private static final String DAYS_KEY = "list";
    private static final String WEATHER_KEY = "weather";
    private static final String TEMPERATURE_KEY = "temp";
    private static final String TEMP_MIN_KEY = "min";
    private static final String TEMP_MAX_KEY = "max";
    private static final String WEATHER_DESCRIPTION_KEY = "main";

    public static final String[] getWeatherDataFromJson(String forecastJsonStr, Units unit)
            throws JSONException {
        if (forecastJsonStr == null)
            return null;

        JSONObject json = new JSONObject(forecastJsonStr);
        JSONArray days = json.getJSONArray(DAYS_KEY);
        WeatherDayInfo[] weatherList = new WeatherDayInfo[days.length()];
        JSONObject weather, temperature;

        long time;
        Calendar cal = Calendar.getInstance();


        for (int i = 0; i < days.length(); i++) {
            time = cal.getTimeInMillis();
            temperature = days.getJSONObject(i).getJSONObject(TEMPERATURE_KEY);
            weather = days.getJSONObject(i).getJSONArray(WEATHER_KEY).getJSONObject(0);
            weatherList[i] = new WeatherDayInfo(temperature.getDouble(TEMP_MIN_KEY), temperature.getDouble(TEMP_MAX_KEY),
                    time, weather.getString(WEATHER_DESCRIPTION_KEY));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        String[] result = new String[weatherList.length];
        for (int i = 0; i < weatherList.length; i++) {
            result[i] = weatherList[i].toString(unit);
        }
        return result;
    }
}
