package com.aamsharif.weathernews.data.network;

import android.content.Context;

import com.aamsharif.weathernews.utilities.InjectorUtils;
import com.google.common.util.concurrent.ListenableFuture;



import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.ListenableWorker;

import androidx.work.WorkerParameters;

/**
 * Created by A. A. M. Sharif on 23-Feb-19.
 */
public class FetchWorker extends ListenableWorker {
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public FetchWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        // do fetching
        WeatherNetworkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(getApplicationContext());
        networkDataSource.fetchWeather();

        ResolvableFuture<Result> mFuture = ResolvableFuture.create();
        mFuture.set(Result.success());
        return mFuture;
    }

}
