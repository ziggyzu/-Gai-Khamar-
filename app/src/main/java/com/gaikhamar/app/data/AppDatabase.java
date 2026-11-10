package com.gaikhamar.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gaikhamar.app.data.dao.*;
import com.gaikhamar.app.data.entity.*;

@Database(entities = {User.class, Animal.class, FeedType.class, FeedLog.class, FeedPurchase.class,
        WeightRecord.class, Batch.class, MilkRecord.class, BreedingRecord.class, CalvingRecord.class,
        HealthRecord.class, Vaccination.class, FarmContact.class, FinanceRecord.class,
        Customer.class, LedgerEntry.class, Loan.class, Worker.class, Attendance.class},
        version = 5, exportSchema = false)
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
    public abstract HealthDao healthDao();
    public abstract VaccinationDao vaccinationDao();
    public abstract ContactDao contactDao();
    public abstract FinanceDao financeDao();
    public abstract CustomerDao customerDao();
    public abstract LoanDao loanDao();
    public abstract WorkerDao workerDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "gai_khamar.db")
                            .fallbackToDestructiveMigration() // dev only: resets data on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}