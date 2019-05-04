package eu.bidtrans.sunshine;

import java.text.SimpleDateFormat;

import eu.bidtrans.sunshine.domain.Units;

public class WeatherDayInfo {
    private double minTemp, maxTemp;
    private long time;
    private String weather;

    public WeatherDayInfo(double minTemp, double maxTemp, long time, String weather) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.time = time;
        this.weather = weather;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low, Units unit) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round( transformTemperature(high, unit));
        long roundedLow = Math.round(transformTemperature(low, unit));

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public String toString(Units unit) {
        return getReadableDateString(time) + " - " + weather + " - " + formatHighLows(maxTemp, minTemp, unit);
    }

    public static double transformTemperature(double temp, Units unit) {
        if (unit == Units.metric) {
            return temp;
        } else if (unit == Units.imperial) {
            return temp * 1.8 + 32;
        } else
            return 0;
    }
}
