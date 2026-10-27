package com.gaikhamar.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

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
import com.gaikhamar.app.util.IdGenerator;
import com.gaikhamar.app.util.PasswordUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {

    public interface Callback<T> {
        void onResult(T result);
    }

    private static volatile Repository INSTANCE;

    public static Repository get(Context context) {
        if (INSTANCE == null) {
            synchronized (Repository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private final AppDatabase db;
    private final UserDao userDao;
    private final AnimalDao animalDao;
    private final FeedTypeDao feedTypeDao;
    private final FeedLogDao feedLogDao;
    private final FeedPurchaseDao feedPurchaseDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private Repository(Context context) {
        db = AppDatabase.get(context);
        userDao = db.userDao();
        animalDao = db.animalDao();
        feedTypeDao = db.feedTypeDao();
        feedLogDao = db.feedLogDao();
        feedPurchaseDao = db.feedPurchaseDao();
    }

    // ---------- Users ----------

    /** Returns the saved user, or null if the phone number is already registered. */
    public void signup(User user, Callback<User> cb) {
        io.execute(() -> {
            User result = null;
            if (userDao.findByPhone(user.phone) == null) {
                user.id = userDao.insert(user);
                result = user;
            }
            User finalResult = result;
            main.post(() -> cb.onResult(finalResult));
        });
    }

    /** Returns the user, or null if phone/password is wrong. */
    public void login(String phone, String password, Callback<User> cb) {
        io.execute(() -> {
            User u = userDao.findByPhone(phone);
            User result = (u != null && PasswordUtil.verify(password, u.passwordHash)) ? u : null;
            main.post(() -> cb.onResult(result));
        });
    }

    // ---------- Animals ----------

    public void addAnimal(Animal a, String farmCode, Callback<Animal> cb) {
        io.execute(() -> {
            a.systemId = UUID.randomUUID().toString();
            a.farmCattleId = a.systemId; // temporary unique value
            a.createdAt = System.currentTimeMillis();
            long id = animalDao.insert(a);
            a.id = id;
            a.farmCattleId = IdGenerator.make(farmCode, a.category, id);
            animalDao.update(a);
            main.post(() -> cb.onResult(a));
        });
    }

    public LiveData<List<Animal>> filter(String category, String query) {
        return animalDao.filter(category, query);
    }

    public LiveData<Animal> getAnimal(long id) { return animalDao.getById(id); }
    public LiveData<Integer> countAll() { return animalDao.countAll(); }
    public LiveData<Integer> countByCategory(String c) { return animalDao.countByCategory(c); }
    public LiveData<Integer> countSick() { return animalDao.countSick(); }
    public LiveData<Integer> countByDairyStatus(String s) { return animalDao.countByDairyStatus(s); }

    // ---------- Feed ----------

    public LiveData<List<FeedType>> feedTypes() { return feedTypeDao.getAll(); }
    public LiveData<Integer> countLowStock() { return feedTypeDao.countLowStock(); }
    public LiveData<List<FeedLog>> logsByDate(String date) { return feedLogDao.getByDate(date); }
    public LiveData<Double> totalKg(String date) { return feedLogDao.totalKg(date); }
    public LiveData<Double> totalCost(String date) { return feedLogDao.totalCost(date); }

    /** Adds one or many feed types. cb may be null. */
    public void addFeedTypes(List<FeedType> list, Callback<Boolean> cb) {
        io.execute(() -> {
            feedTypeDao.insertAll(list);
            if (cb != null) main.post(() -> cb.onResult(true));
        });
    }

    /** Adds purchased stock and records the purchase. cb may be null. */
    public void addStock(long feedTypeId, double qtyKg, double pricePerKg,
                         String date, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                FeedType t = feedTypeDao.findById(feedTypeId);
                if (t == null) return;
                t.stockKg += qtyKg;
                if (pricePerKg > 0) t.costPerKg = pricePerKg;
                feedTypeDao.update(t);

                FeedPurchase p = new FeedPurchase();
                p.feedTypeId = feedTypeId;
                p.quantityKg = qtyKg;
                p.pricePerKg = pricePerKg;
                p.totalCost = qtyKg * pricePerKg;
                p.date = date;
                p.createdAt = System.currentTimeMillis();
                feedPurchaseDao.insert(p);
            });
            if (cb != null) main.post(() -> cb.onResult(true));
        });
    }

    /** Result: 0 = saved, 1 = not enough stock, 2 = feed type not found. */
    public void logFeed(long feedTypeId, String groupCode, double qtyKg,
                        String date, Callback<Integer> cb) {
        io.execute(() -> {
            final int[] result = {0};
            db.runInTransaction(() -> {
                FeedType t = feedTypeDao.findById(feedTypeId);
                if (t == null) {
                    result[0] = 2;
                    return;
                }
                if (qtyKg > t.stockKg + 0.0001) {
                    result[0] = 1;
                    return;
                }
                t.stockKg = Math.max(0, t.stockKg - qtyKg);
                feedTypeDao.update(t);

                FeedLog l = new FeedLog();
                l.date = date;
                l.feedTypeId = t.id;
                l.feedName = t.name;
                l.groupCode = groupCode;
                l.quantityKg = qtyKg;
                l.costPerKg = t.costPerKg;
                l.totalCost = qtyKg * t.costPerKg;
                l.createdAt = System.currentTimeMillis();
                feedLogDao.insert(l);
            });
            main.post(() -> cb.onResult(result[0]));
        });
    }
}