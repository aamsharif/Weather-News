package com.aamsharif.weathernews.utilities;

/**
 * Created by A. A. M. Sharif on 20-Jan-18.
 */
import com.aamsharif.weathernews.R;
import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


/**
 * Class for handling date conversions that are useful for Weather News.
 */
public final class WeatherNewsDateUtils {

    // Milliseconds in a day
    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    /**
     * This method returns the number of milliseconds (UTC time) for today's date at midnight
     * (in GMT time) for the time zone we are currently in. In other words, the GMT date will
     * always represent our date.
     *
     * Since UTC / GMT time are the standard for all time zones in the world, we use it to
     * normalize our dates that are stored in the database. When we extract values from the
     * database, we adjust for the current time zone using time zone offsets.
     *
     * @return The number of milliseconds (UTC / GMT) for today's date at midnight in the local
     * time zone
     */
    public static long getNormalizedUtcMsForToday() {

        long utcNowMillis = System.currentTimeMillis();

        /*
         * This TimeZone represents the device's current time zone. It provides us with a means
         * of acquiring the offset for local time from a UTC time stamp.
         */
        TimeZone currentTimeZone = TimeZone.getDefault();

        /*
         * The getOffset method returns the number of milliseconds to add to UTC time to get the
         * elapsed time since the epoch for our current time zone. We pass the current UTC time
         * into this method so it can determine changes to account for daylight savings time.
         */
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);

        /*
         * Depending on our time zone, this variable represents the number of milliseconds since
         * January 1, 1970 (GMT) time.
         */
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;

        // This method simply converts milliseconds to days, disregarding any fractional days
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);

        return TimeUnit.DAYS.toMillis(daysSinceEpochLocal);
    }

    public static Date getNormalizedUtcDateForToday() {
        long normalizedMilli = getNormalizedUtcMsForToday();
        return new Date(normalizedMilli);
    }

    /**
     * This method returns the number of days since the epoch (January 01, 1970, 12:00 Midnight UTC)
     * in UTC time from the current date.
     *
     * @param utcDate A date in milliseconds in UTC time.
     *
     * @return The number of days from the epoch to the date argument.
     */
    private static long elapsedDaysSinceEpoch(long utcDate) {
        return TimeUnit.MILLISECONDS.toDays(utcDate);
    }

    /**
     * Normalizes a date (in milliseconds). For example, given the time representing
     *
     *     Friday, 9/16/2016, 17:45:15 GMT-4:00 DST (1474062315000)
     *
     * this method would return the number of milliseconds (since the epoch) that represents
     *
     *     Friday, 9/16/2016, 00:00:00 GMT (1473984000000)
     *
     * @param date The date (in milliseconds) to normalize
     *
     * @return The UTC date at 12 midnight of the date
     */
    public static long normalizeDate(long date) {
        long daysSinceEpoch = elapsedDaysSinceEpoch(date);
        return daysSinceEpoch * DAY_IN_MILLIS;
    }

    /**
     * In order to ensure consistent inserts into WeatherProvider, we check that dates have been
     * normalized before they are inserted.
     *
     * @param millisSinceEpoch Milliseconds since January 1, 1970 at midnight
     *
     * @return true if the date represents the beginning of a day in Unix time, false otherwise
     */
    public static boolean isDateNormalized(long millisSinceEpoch) {
        boolean isDateNormalized = false;
        if (millisSinceEpoch % DAY_IN_MILLIS == 0) {
            isDateNormalized = true;
        }

        return isDateNormalized;
    }

    /**
     * This method will return UTC date from the local time after adjusting the time zone difference
     *
     * @param localizedTime normalized local time comes from the database
     *
     * @return The UTC date from local date
     */
    private static long getUtcDateFromLocalMidnight(long localizedTime) {
        // The timeZone object will provide us the current user's time zone offset
        TimeZone timeZone = TimeZone.getDefault();
        // This offset, in milliseconds, when added to a UTC date time, will produce the local time.
        // But, we may subtract it to adjust it for subsequent daysFromEpochToToday
        long gmtOffset = timeZone.getOffset(localizedTime);

        if(isSubtractionNeeded())
            return localizedTime - gmtOffset;
        else return localizedTime + gmtOffset;
    }

    /**
     * helper method to decide whether we are in between midnight and (midnight + GMT) or not
     *
     * @return true if current local time is in between midnight and (midnight + GMT)
     */
    private static boolean isSubtractionNeeded() {
        long normalizedTime = getNormalizedUtcMsForToday();
        TimeZone currentTimeZone = TimeZone.getDefault();
        long currentTime = System.currentTimeMillis();
        long gmtOffsetMillis = currentTimeZone.getOffset(currentTime);
        long localTime = currentTime + gmtOffsetMillis;
        return (localTime >= normalizedTime && localTime < normalizedTime + gmtOffsetMillis);
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.
     *
     * The day string for forecast uses the following logic:
     * For today: "Today, Jan 7"
     * For tomorrow:  "Tomorrow
     * For the next 5 days: "Wednesday" (just the day name)
     * For all days after that: "Mon, Jan 7"
     *
     * @param context               Context to use for resource localization
     * @param localizedTime The date in milliseconds (UTC midnight)
     * @param showFullDate          Used to show a fuller-version of the date, which always
     *                              contains either the day of the week, today, or tomorrow, in
     *                              addition to the date.
     *
     * @return A user-friendly representation of the date such as "Today, Jan 7", "Tomorrow",
     * "Wednesday" or "Mon, Jan 7"
     */
    public static String getFriendlyDateString(Context context, long localizedTime, boolean showFullDate) {

        /*
         * Since we normalized the date when we inserted it into the database, we need to take
         * that normalized date and produce a date (in UTC time) that represents the local time
         * zone at midnight.
         */
        long localDate = getUtcDateFromLocalMidnight(localizedTime);
        /*
         * In order to determine which day of the week we are creating a date string for, we need
         * to compare the number of days that have passed since the epoch (January 1, 1970 at
         * 00:00 GMT)
         */
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(localDate);

        /*
         * As a basis for comparison, we use the number of days that have passed from the epoch
         * until today.
         */
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());

        if (daysFromEpochToProvidedDate == daysFromEpochToToday || showFullDate) {
            // If the date we're building the String for is today's date, the format
            String dayName = getDayName(context, localDate);
            String readableDate = getReadableDateString(context, localDate);
            if (daysFromEpochToProvidedDate - daysFromEpochToToday < 2) {
                String localizedDayName = new SimpleDateFormat("EEEE").format(localDate);
                return readableDate.replace(localizedDayName, dayName);
            } else {
                return readableDate;
            }
        } else if (daysFromEpochToProvidedDate < daysFromEpochToToday + 7) {
            // If the input date is less than a week in the future, just return the day name
            return getDayName(context, localDate);
        } else {
            int flags = DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_NO_YEAR
                    | DateUtils.FORMAT_ABBREV_ALL
                    | DateUtils.FORMAT_SHOW_WEEKDAY;

            return DateUtils.formatDateTime(context, localDate, flags);
        }
    }

    /**
     * Returns a date string in the format specified, which shows an abbreviated date without a
     * year.
     *
     * @param context      Used by DateUtils to format the date in the current locale
     * @param timeInMillis Time in milliseconds since the epoch (local time)
     *
     * @return The formatted date string
     */
    private static String getReadableDateString(Context context, long timeInMillis) {
        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY;

        return DateUtils.formatDateTime(context, timeInMillis, flags);
    }

    /**
     * Given a day, returns just the name to use for that day.
     *   E.g "today", "tomorrow", "Wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds (UTC time)
     *
     * @return the string day of the week
     */
    private static String getDayName(Context context, long dateInMillis) {
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(dateInMillis);
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());

        int daysAfterToday = (int) (daysFromEpochToProvidedDate - daysFromEpochToToday);

        switch (daysAfterToday) {
            case 0:
                return context.getString(R.string.today);
            case 1:
                return context.getString(R.string.tomorrow);

            default:
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
        }
    }
}
