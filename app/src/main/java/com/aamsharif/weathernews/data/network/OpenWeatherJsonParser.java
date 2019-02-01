package com.aamsharif.weathernews.data.network;

/**
 * Created by A. A. M. Sharif on 21-Jan-18.
 */
import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Date;

import com.aamsharif.weathernews.data.WeatherNewsPreferences;
import com.aamsharif.weathernews.data.database.WeatherEntry;
import com.aamsharif.weathernews.utilities.WeatherNewsDateUtils;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonParser {

    // Location information
    private static final String OWM_CITY = "city";
    private static final String OWM_COORD = "coord";

    // Location coordinate
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";

    // Weather information. Each day's forecast info is an element of the "list" array
    private static final String OWM_LIST = "list";

    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    // All temperatures are children of the "temp" object
    private static final String OWM_TEMPERATURE = "temp";

    // Max temperature for the day
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";

    private static boolean hasHttpError(JSONObject forecastJson) throws JSONException {
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    return false;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // Location invalid
                default:
                    // Server probably down
                    return true;
            }
        }
        return false;
    }

    private static WeatherEntry fromJson(final JSONObject dayForecast,
                                         long dateTimeMillis) throws JSONException {
        // We ignore all the datetime values embedded in the JSON and assume that
        // the values are returned in-order by day (which is not guaranteed to be correct).
        double pressure = dayForecast.getDouble(OWM_PRESSURE);
        int humidity = dayForecast.getInt(OWM_HUMIDITY);
        double windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
        double windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

        // Description is in a child array called "weather", which is 1 element long.
        // That element also contains a weather code.
        JSONObject weatherObject =
                dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

        int weatherId = weatherObject.getInt(OWM_WEATHER_ID);

        //  Temperatures are sent by Open Weather Map in a child object called "temp".
        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        double min = temperatureObject.getDouble(OWM_MIN);
        double max = temperatureObject.getDouble(OWM_MAX);

        // Create the weather entry object
        return new WeatherEntry(weatherId, new Date(dateTimeMillis), min, max,
                humidity, pressure, windSpeed, windDirection);
    }

    private static WeatherEntry[] fromJson(Context context, final JSONObject forecastJson) throws JSONException {
        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        // Save latitude and longitude in WeatherNewsPreferences from the response
        WeatherNewsPreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        WeatherEntry[] weatherEntries = new WeatherEntry[jsonWeatherArray.length()];

        /*
         * OWM returns daily forecasts based upon the local time of the city that is being asked
         * for, which means that we need to know the GMT offset to translate this data properly.
         * Since this data is also sent in-order and the first day is always the current day, we're
         * going to take advantage of that to get a nice normalized UTC date for all of our weather.
         */
        long normalizedUtcStartDay = WeatherNewsDateUtils.getNormalizedUtcMsForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {
            // Get the JSON object representing the day
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);

            // Create the weather entry object
            long dateTimeMillis = normalizedUtcStartDay + WeatherNewsDateUtils.DAY_IN_MILLIS * i;
            WeatherEntry weather = fromJson(dayForecast, dateTimeMillis);

            weatherEntries[i] = weather;
        }
        return weatherEntries;
    }

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     *
     * @param forecastJsonStr JSON response from server
     * @return Array of WeatherEntry wrapped into WeatherResponse describing weather data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    @Nullable
    public static WeatherResponse parse(Context context, final String forecastJsonStr) throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        // Is there an error?
        if (hasHttpError(forecastJson)) {
            return null;
        }

        WeatherEntry[] weatherForecast = fromJson(context, forecastJson);

        return new WeatherResponse(weatherForecast);
    }
}
