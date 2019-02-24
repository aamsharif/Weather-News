package com.aamsharif.weathernews.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

/**
 * Created by A. A. M. Sharif on 28-Jan-19.
 */
@Database(entities = {WeatherEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class WeatherNewsDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "weather";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile WeatherNewsDatabase sInstance;

    public static WeatherNewsDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            WeatherNewsDatabase.class, WeatherNewsDatabase.DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

    public abstract WeatherDao weatherDao();
}
