package com.gaikhamar.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gaikhamar.app.data.dao.AnimalDao;
import com.gaikhamar.app.data.dao.FeedLogDao;
import com.gaikhamar.app.data.dao.FeedPurchaseDao;
import com.gaikhamar.app.data.dao.FeedTypeDao;
import com.gaikhamar.app.data.dao.UserDao;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.FeedLog;
import com.gaikhamar.app.data.entity.FeedPurchase;
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.data.entity.User;

@Database(entities = {User.class, Animal.class, FeedType.class, FeedLog.class,
        FeedPurchase.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract AnimalDao animalDao();
    public abstract FeedTypeDao feedTypeDao();
    public abstract FeedLogDao feedLogDao();
    public abstract FeedPurchaseDao feedPurchaseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "gai_khamar.db")
                            .fallbackToDestructiveMigration() // dev only: resets data on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}