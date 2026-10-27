package com.gaikhamar.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gaikhamar.app.data.dao.AnimalDao;
import com.gaikhamar.app.data.dao.BatchDao;
import com.gaikhamar.app.data.dao.BreedingDao;
import com.gaikhamar.app.data.dao.CalvingDao;
import com.gaikhamar.app.data.dao.FeedLogDao;
import com.gaikhamar.app.data.dao.FeedPurchaseDao;
import com.gaikhamar.app.data.dao.FeedTypeDao;
import com.gaikhamar.app.data.dao.MilkDao;
import com.gaikhamar.app.data.dao.UserDao;
import com.gaikhamar.app.data.dao.WeightDao;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.data.entity.BreedingRecord;
import com.gaikhamar.app.data.entity.CalvingRecord;
import com.gaikhamar.app.data.entity.FeedLog;
import com.gaikhamar.app.data.entity.FeedPurchase;
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.data.entity.MilkRecord;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.data.entity.WeightRecord;

@Database(entities = {User.class, Animal.class, FeedType.class, FeedLog.class,
        FeedPurchase.class, WeightRecord.class, Batch.class, MilkRecord.class,
        BreedingRecord.class, CalvingRecord.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract AnimalDao animalDao();
    public abstract FeedTypeDao feedTypeDao();
    public abstract FeedLogDao feedLogDao();
    public abstract FeedPurchaseDao feedPurchaseDao();
    public abstract WeightDao weightDao();
    public abstract BatchDao batchDao();
    public abstract MilkDao milkDao();
    public abstract BreedingDao breedingDao();
    public abstract CalvingDao calvingDao();

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