package com.aamsharif.weathernews.ui.detail;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.aamsharif.weathernews.data.WeatherNewsRepository;
import com.aamsharif.weathernews.data.database.WeatherEntry;

import java.util.Date;

/**
 * Created by A. A. M. Sharif on 29-Jan-19.
 */

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link WeatherNewsRepository} and a date for the current {@link WeatherEntry}
 */
public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final WeatherNewsRepository mRepository;
    private final Date mDate;

    public DetailViewModelFactory(WeatherNewsRepository repository, Date date) {
        this.mRepository = repository;
        this.mDate = date;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DetailActivityViewModel(mRepository, mDate);
    }
}
