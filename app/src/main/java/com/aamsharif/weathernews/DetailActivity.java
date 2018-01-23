package com.aamsharif.weathernews;

/**
 * Created by A. A. M. Sharif on 22-Jan-18.
 */

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aamsharif.weathernews.data.WeatherContract;
import com.aamsharif.weathernews.databinding.ActivityDetailBinding;
import com.aamsharif.weathernews.utilities.WeatherNewsDateUtils;
import com.aamsharif.weathernews.utilities.WeatherUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // In this Activity, we can share the selected day's forecast using a hashtag
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    /*
     * The columns of data that we are interested in displaying within our DetailActivity's
     * weather display.
     */
    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day.
     */
    private static final int ID_DETAIL_LOADER = 353;

    // A summary of the forecast that can be shared by clicking the share button in the ActionBar
    private String mForecastSummary;

    // The URI that is used to access the chosen day's weather details
    private Uri mUri;


    /*
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DataBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        // This connects our Activity into the loader lifecycle
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Settings menu item clicked
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        // Share menu item clicked
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing. We have set
     * the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    /**
     * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
     *
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Within onLoadFinished, we bind the data to our views so the user can see the details of the
     * weather on the date they selected from the forecast.
     *
     * @param loader The cursor loader that finished.
     * @param data   The cursor that is being returned.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            // We have valid data, continue on to bind the data to the UI
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            // No data to display, simply return and do nothing
            return;
        }

        /****************
         * Weather Icon *
         ****************/
        // Read weather condition ID from the cursor (ID provided by Open Weather Map)
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        // Use our utility method to determine the resource ID for the proper art
        int weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = WeatherNewsDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDetailBinding.primaryInfo.date.setText(dateText);

        /***********************
         * Weather Description *
         ***********************/
        // Use the weatherId to obtain the proper description
        String description = WeatherUtils.getStringForWeatherCondition(this, weatherId);

        // Create the accessibility (a11y) String from the weather description
        String descriptionA11y = getString(R.string.a11y_forecast, description);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);

        // Set the content description on the weather image (for accessibility purposes)
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = WeatherUtils.formatTemperature(this, highInCelsius);

        // Create the accessibility (a11y) String from the weather description
        String highA11y = getString(R.string.a11y_high_temp, highString);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = WeatherUtils.formatTemperature(this, lowInCelsius);

        String lowA11y = getString(R.string.a11y_low_temp, lowString);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /************
         * Humidity *
         ************/
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /****************************
         * Wind speed and direction *
         ****************************/
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = WeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        String windA11y = getString(R.string.a11y_wind, windString);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);

        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);

        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        // Set the text and content description (for accessibility purposes)
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        // Store the forecast summary String in our forecast summary field to share later
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}