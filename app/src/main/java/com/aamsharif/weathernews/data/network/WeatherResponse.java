package com.aamsharif.weathernews.data.network;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */

import android.support.annotation.NonNull;

import com.aamsharif.weathernews.data.database.WeatherEntry;

/**
 * Weather response from the backend. Contains the weather forecasts.
 */
class WeatherResponse {

    @NonNull
    private final WeatherEntry[] mWeatherForecast;

    public WeatherResponse(@NonNull final WeatherEntry[] weatherForecast) {
        mWeatherForecast = weatherForecast;
    }

    public WeatherEntry[] getWeatherForecast() {
        return mWeatherForecast;
    }
}
