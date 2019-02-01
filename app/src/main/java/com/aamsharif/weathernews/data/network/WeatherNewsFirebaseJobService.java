package com.aamsharif.weathernews.data.network;

/**
 * Created by A. A. M. Sharif on 21-Jan-18.
 */
import android.util.Log;

import com.aamsharif.weathernews.utilities.InjectorUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class WeatherNewsFirebaseJobService extends JobService {

    private static final String LOG_TAG = WeatherNewsFirebaseJobService.class.getSimpleName();

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(LOG_TAG, "Job service started");

        WeatherNetworkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(getApplicationContext());
        networkDataSource.fetchWeather();

        jobFinished(jobParameters, false);

        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
