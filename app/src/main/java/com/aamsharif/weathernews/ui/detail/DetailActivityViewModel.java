package com.aamsharif.weathernews.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.aamsharif.weathernews.data.WeatherNewsRepository;
import com.aamsharif.weathernews.data.database.WeatherEntry;

import java.util.Date;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */
public class DetailActivityViewModel extends ViewModel {
    // Weather forecast the user is looking at
    private final LiveData<WeatherEntry> mWeather;
    // Date for the weather forecast
    private final Date mDate;
    private final WeatherNewsRepository mRepository;

    public DetailActivityViewModel(WeatherNewsRepository repository, Date date) {
        mRepository = repository;
        mDate = date;
        mWeather = mRepository.getWeatherbyDate(mDate);
    }

    public LiveData<WeatherEntry> getWeather() {
        return mWeather;
    }
}
