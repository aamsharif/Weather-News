package com.aamsharif.weathernews.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.aamsharif.weathernews.data.WeatherNewsRepository;
import com.aamsharif.weathernews.data.database.ListWeatherEntry;

import java.util.List;


/**
 * Created by A. A. M. Sharif on 30-Jan-19.
 */
public class MainActivityViewModel extends ViewModel {
    // list of weather forecast
    private final LiveData<List<ListWeatherEntry>> mForecast;
    private final WeatherNewsRepository mRepository;

    public MainActivityViewModel(WeatherNewsRepository repository) {
        this.mRepository = repository;
        this.mForecast = mRepository.getCurrentWeatherForecasts();
    }

    public LiveData<List<ListWeatherEntry>> getForecast() {
        return mForecast;
    }
}
