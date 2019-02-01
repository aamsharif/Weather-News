package com.aamsharif.weathernews.utilities;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */

import android.content.Context;

import com.aamsharif.weathernews.AppExecutors;
import com.aamsharif.weathernews.data.WeatherNewsRepository;
import com.aamsharif.weathernews.data.database.WeatherNewsDatabase;
import com.aamsharif.weathernews.data.network.WeatherNetworkDataSource;
import com.aamsharif.weathernews.ui.detail.DetailViewModelFactory;
import com.aamsharif.weathernews.ui.list.MainViewModelFactory;

import java.util.Date;

/**
 * Provides static methods to inject the various classes needed for Weather News
 */
public class InjectorUtils {

    public static WeatherNewsRepository provideRepository(Context context) {
        WeatherNewsDatabase database = WeatherNewsDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        WeatherNetworkDataSource networkDataSource =
                WeatherNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return WeatherNewsRepository.getInstance(database.weatherDao(), networkDataSource, executors);
    }

    public static WeatherNetworkDataSource provideNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return WeatherNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static DetailViewModelFactory provideDetailViewModelFactory(Context context, Date date) {
        WeatherNewsRepository repository = provideRepository(context.getApplicationContext());
        return new DetailViewModelFactory(repository, date);
    }

    public static MainViewModelFactory provideMainActivityViewModelFactory(Context context) {
        WeatherNewsRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }

}