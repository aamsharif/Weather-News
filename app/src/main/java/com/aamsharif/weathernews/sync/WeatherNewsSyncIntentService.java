package com.aamsharif.weathernews.sync;

/**
 * Created by A. A. M. Sharif on 21-Jan-18.
 */

import android.app.IntentService;
import android.content.Intent;

/**
 * An IntentService subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WeatherNewsSyncIntentService extends IntentService {

    public WeatherNewsSyncIntentService() {
        super("WeatherNewsSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WeatherNewsSyncTask.syncWeather(this);
    }
}
