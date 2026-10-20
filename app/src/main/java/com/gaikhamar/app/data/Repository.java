package com.gaikhamar.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.gaikhamar.app.data.dao.AnimalDao;
import com.gaikhamar.app.data.dao.UserDao;
import com.gaikhamar.app.data.entity.Animal;
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

    private final UserDao userDao;
    private final AnimalDao animalDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private Repository(Context context) {
        AppDatabase db = AppDatabase.get(context);
        userDao = db.userDao();
        animalDao = db.animalDao();
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
}