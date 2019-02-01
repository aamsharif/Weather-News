package com.aamsharif.weathernews.utilities;

/**
 * Created by A. A. M. Sharif on 21-Jan-18.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import com.aamsharif.weathernews.R;
import com.aamsharif.weathernews.ui.detail.DetailActivity;
import com.aamsharif.weathernews.data.WeatherNewsPreferences;

public class NotificationUtils {
    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be useful when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary.
     */
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to build notification and use with various Utility methods
     */
    public static void notifyUserOfNewWeather(Context context, int weatherId, double high, double low, long todaysTimestamp) {

        Resources resources = context.getResources();
        int largeArtResourceId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        Bitmap largeIcon = BitmapFactory.decodeResource(
                resources,
                largeArtResourceId);

        String notificationTitle = context.getString(R.string.app_name);

        String notificationText = getNotificationText(context, weatherId, high, low);

        // getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID
        int smallArtResourceId = WeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(smallArtResourceId)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setAutoCancel(true);

        /*
         * This Intent will be triggered when the user clicks the notification. In our case,
         * we want Weather News to open the DetailActivity to display the newly updated weather.
         */
        Intent detailIntentForToday = new Intent(context, DetailActivity.class);
        detailIntentForToday.putExtra(DetailActivity.WEATHER_ID_EXTRA, todaysTimestamp);


        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
        PendingIntent resultPendingIntent = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // WEATHER_NOTIFICATION_ID allows us to update or cancel the notification later on
        notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

        /*
         * Since we just showed a notification, save the current time. That way, we can check
         * next time the weather is refreshed if we should show another notification.
         */
        WeatherNewsPreferences.saveLastNotificationTime(context, System.currentTimeMillis());
    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     *
     * The String returned from this method will look something like this:
     *
     * Forecast: Sunny - High: 14°C Low: 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" or "sky is clear".
         */
        String shortDescription = WeatherUtils.getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        return String.format(notificationFormat,
                shortDescription,
                WeatherUtils.formatTemperature(context, high),
                WeatherUtils.formatTemperature(context, low));
    }
}
