package com.aamsharif.weathernews.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by A. A. M. Sharif on 28-Jan-19.
 */
@Database(entities = {WeatherEntry.class}, version = 1)
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
