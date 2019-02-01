package com.aamsharif.weathernews.ui.list;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.aamsharif.weathernews.data.WeatherNewsRepository;



/**
 * Created by A. A. M. Sharif on 30-Jan-19.
 */
public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final WeatherNewsRepository mRepository;

    public MainViewModelFactory(WeatherNewsRepository mRepository) {
        this.mRepository = mRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainActivityViewModel(mRepository);
    }
}
