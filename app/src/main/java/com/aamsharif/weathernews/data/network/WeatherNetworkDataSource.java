package com.aamsharif.weathernews.data.network;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import com.aamsharif.weathernews.AppExecutors;
import com.aamsharif.weathernews.data.WeatherNewsPreferences;
import com.aamsharif.weathernews.data.database.WeatherEntry;
import com.aamsharif.weathernews.utilities.NotificationUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.net.URL;
import java.util.concurrent.TimeUnit;


/**
 * Provides an API for doing all operations with the server data
 */
public class WeatherNetworkDataSource {
    // The number of days we want our API to return, set to 14 days or two weeks
    public static final int NUM_DAYS = 14;
    private static final String LOG_TAG = WeatherNetworkDataSource.class.getSimpleName();

    // Interval at which to sync with the weather. Use TimeUnit for convenience, rather than
    // writing out a bunch of multiplication ourselves and risk making a silly mistake.
    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;
    private static final String WEATHER_NEWS_SYNC_TAG = "weathernews-sync";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static WeatherNetworkDataSource sInstance;
    private final Context mContext;

    private final AppExecutors mExecutors;

    // LiveData storing the latest downloaded weather forecasts
    private final MutableLiveData<WeatherEntry[]> mDownloadedWeatherForecasts;

    private WeatherNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedWeatherForecasts = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static WeatherNetworkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new WeatherNetworkDataSource(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public LiveData<WeatherEntry[]> getCurrentWeatherForecasts() {
        return mDownloadedWeatherForecasts;
    }

    /**
     * Starts an intent service to fetch the weather.
     */
    public void startFetchWeatherService() {
        Intent intentToFetch = new Intent(mContext, WeatherNewsSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Service created");
    }

    /**
     * Schedules a repeating job service which fetches Weather News's weather data using FirebaseJobDispatcher.
     */
    public void scheduleRecurringFetchWeatherSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create the Job to periodically sync Weather News
        Job syncWeatherNewsJob = dispatcher.newJobBuilder()
                // The Service that will be used to sync Weather News's data
                .setService(WeatherNewsFirebaseJobService.class)
                // Set the UNIQUE tag used to identify this Job
                .setTag(WEATHER_NEWS_SYNC_TAG)
                // Network constraints on which this Job should run
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want Weather News's weather data to stay up to date, so we tell this Job
                 * to recur.
                 */
                .setRecurring(true)
                // We want the weather data to be synced every 3 to 4 hours
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncWeatherNewsJob);
    }

    /**
     * Gets the newest weather by fetching from Open Weather Map server
     */
    void fetchWeather() {
        Log.d(LOG_TAG, "Fetch weather started");
        mExecutors.networkIO().execute(() -> {
            try {
                // The getUrl method will return the URL that we need to get the forecast JSON for the
                // weather. It will decide whether to create a URL based off of the latitude and
                // longitude or off of a simple location as a String.
                URL weatherRequestUrl = NetworkUtils.getUrl(mContext);

                // Use the URL to retrieve the JSON
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                // Parse the JSON into a list of weather forecasts
                WeatherResponse response = OpenWeatherJsonParser.parse(mContext, jsonWeatherResponse);
                Log.d(LOG_TAG, "JSON Parsing finished");

                // As long as there are weather forecasts, update the LiveData storing the most recent
                // weather forecasts. This will trigger observers of that LiveData, such as the
                // WeatherNewsRepository.
                if (response != null && response.getWeatherForecast().length != 0) {
                    Log.d(LOG_TAG, "JSON not null and has " + response.getWeatherForecast().length
                            + " values");
                    Log.d(LOG_TAG, String.format("First value is %1.0f and %1.0f",
                            response.getWeatherForecast()[0].getMin(),
                            response.getWeatherForecast()[0].getMax()));

                    mDownloadedWeatherForecasts.postValue(response.getWeatherForecast());
                }
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }
        });
    }

    /**
     * This will notify the user that new weather has been loaded if the user
     * hasn't been notified of the weather within the last day AND they haven't
     * disabled notifications in the preferences screen.
     */
    public void notifyUserIfNeeded(int weatherId, double high, double low, long todaysTimestamp){
        /*
         * Finally, after we insert data into the database, determine whether or not
         * we should notify the user that the weather has been refreshed.
         */
        boolean notificationsEnabled = WeatherNewsPreferences.areNotificationsEnabled(mContext);

        /*
         * If the last notification was shown was more than 1 day ago, we want to send
         * another notification to the user that the weather has been updated.
         */
        long timeSinceLastNotification = WeatherNewsPreferences
                .getEllapsedTimeSinceLastNotification(mContext);

        boolean oneDayPassedSinceLastNotification = false;

        if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
            oneDayPassedSinceLastNotification = true;
        }

        /*
         * We only want to show the notification if the user wants them shown and we
         * haven't shown a notification in the past day.
         */
        if (notificationsEnabled && oneDayPassedSinceLastNotification) {
            NotificationUtils.notifyUserOfNewWeather(mContext, weatherId, high, low, todaysTimestamp);
        }
    }
}