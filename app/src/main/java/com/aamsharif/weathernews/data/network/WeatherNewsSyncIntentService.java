package com.aamsharif.weathernews.data.network;

/**
 * Created by A. A. M. Sharif on 21-Jan-18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.aamsharif.weathernews.utilities.InjectorUtils;

/**
 * An IntentService subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WeatherNewsSyncIntentService extends IntentService {

    private static final String LOG_TAG = WeatherNewsSyncIntentService.class.getSimpleName();

    public WeatherNewsSyncIntentService() {
        super("WeatherNewsSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Intent service started");
        WeatherNetworkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetchWeather();
    }
}
