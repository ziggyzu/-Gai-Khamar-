package com.gaikhamar.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gaikhamar.app.data.dao.AnimalDao;
import com.gaikhamar.app.data.dao.UserDao;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.User;

@Database(entities = {User.class, Animal.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract AnimalDao animalDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "gai_khamar.db").build();
                }
            }
        }
        return INSTANCE;
    }
}