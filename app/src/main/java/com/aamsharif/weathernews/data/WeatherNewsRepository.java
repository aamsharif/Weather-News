package com.aamsharif.weathernews.data;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */

import androidx.lifecycle.LiveData;
import android.util.Log;

import com.aamsharif.weathernews.AppExecutors;
import com.aamsharif.weathernews.data.database.ListWeatherEntry;
import com.aamsharif.weathernews.data.database.WeatherDao;
import com.aamsharif.weathernews.data.database.WeatherEntry;
import com.aamsharif.weathernews.data.network.WeatherNetworkDataSource;
import com.aamsharif.weathernews.utilities.WeatherNewsDateUtils;

import java.util.Date;
import java.util.List;

/**
 * Handles data operations in Weather News. Acts as a mediator between {@link WeatherNetworkDataSource}
 * and {@link WeatherDao}
 */
public class WeatherNewsRepository {
    private static final String LOG_TAG = WeatherNewsRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static WeatherNewsRepository sInstance;
    private final WeatherDao mWeatherDao;
    private final WeatherNetworkDataSource mWeatherNetworkDataSource;
    private final AppExecutors mExecutors;
    private static boolean sInitialized = false;

    private WeatherNewsRepository(WeatherDao weatherDao,
                               WeatherNetworkDataSource weatherNetworkDataSource,
                               AppExecutors executors) {
        mWeatherDao = weatherDao;
        mWeatherNetworkDataSource = weatherNetworkDataSource;
        mExecutors = executors;

        LiveData<WeatherEntry[]> networkData = mWeatherNetworkDataSource.getCurrentWeatherForecasts();
        networkData.observeForever(newForecastsFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                // Deletes old historical data because we don't need to keep multiple days' data
                mWeatherDao.deleteAllData();
                Log.d(LOG_TAG, "Old weather deleted");
                // Insert our new weather data into Weather News's database
                mWeatherDao.bulkInsert(newForecastsFromNetwork);
                Log.d(LOG_TAG, "New values inserted");

                Date today = WeatherNewsDateUtils.getNormalizedUtcDateForToday();
                WeatherEntry weatherEntry = mWeatherDao.getWeatherEntryByDate(today);

                int weatherId = weatherEntry.getWeatherIconId();
                double high = weatherEntry.getMax();
                double low = weatherEntry.getMin();
                long todaysTimestamp = weatherEntry.getDate().getTime();

                mWeatherNetworkDataSource.notifyUserIfNeeded(weatherId, high, low, todaysTimestamp);
            });
        });
    }

    public synchronized static WeatherNewsRepository getInstance(
            WeatherDao weatherDao, WeatherNetworkDataSource weatherNetworkDataSource,
            AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new WeatherNewsRepository(weatherDao, weatherNetworkDataSource,
                        executors);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    public LiveData<WeatherEntry> getWeatherbyDate(Date date){
        initializeData();
        return mWeatherDao.getWeatherByDate(date);
    }

    public LiveData<List<ListWeatherEntry>> getCurrentWeatherForecasts() {
        initializeData();
        Date today = WeatherNewsDateUtils.getNormalizedUtcDateForToday();
        return mWeatherDao.getCurrentWeatherForecasts(today);
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    private synchronized void initializeData() {
        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (sInitialized) return;
        sInitialized = true;

        mWeatherNetworkDataSource.scheduleRecurringFetchWeatherSync();

        mExecutors.diskIO().execute(() -> {
            if (isFetchNeeded()) startFetchWeatherService();
        });
    }

    /**
     * Database related operation
     **/

    /**
     * Checks if there are enough days of future weather for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    private boolean isFetchNeeded() {
        Date today = WeatherNewsDateUtils.getNormalizedUtcDateForToday();
        int count = mWeatherDao.countAllFutureWeather(today);
        return (count < WeatherNetworkDataSource.NUM_DAYS);
    }

    /**
     * Network related operation
     */
    private void startFetchWeatherService() {
        mWeatherNetworkDataSource.startFetchWeatherService();
    }
}
