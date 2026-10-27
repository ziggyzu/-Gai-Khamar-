package com.gaikhamar.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

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
import com.gaikhamar.app.data.entity.BatchSummary;
import com.gaikhamar.app.data.entity.BreedingRecord;
import com.gaikhamar.app.data.entity.CalvingRecord;
import com.gaikhamar.app.data.entity.FeedLog;
import com.gaikhamar.app.data.entity.FeedPurchase;
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.data.entity.MilkRecord;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.data.entity.WeightRecord;
import com.gaikhamar.app.util.DateUtil;
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

    private static final int GESTATION_DAYS = 283;

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
    private final WeightDao weightDao;
    private final BatchDao batchDao;
    private final MilkDao milkDao;
    private final BreedingDao breedingDao;
    private final CalvingDao calvingDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private Repository(Context context) {
        db = AppDatabase.get(context);
        userDao = db.userDao();
        animalDao = db.animalDao();
        feedTypeDao = db.feedTypeDao();
        feedLogDao = db.feedLogDao();
        feedPurchaseDao = db.feedPurchaseDao();
        weightDao = db.weightDao();
        batchDao = db.batchDao();
        milkDao = db.milkDao();
        breedingDao = db.breedingDao();
        calvingDao = db.calvingDao();
    }

    private void done(Callback<Boolean> cb) {
        if (cb != null) main.post(() -> cb.onResult(true));
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

    /** Must be called on the background thread. Saves the animal and gives it its unique ID. */
    private void insertAnimalSync(Animal a, String farmCode) {
        a.systemId = UUID.randomUUID().toString();
        a.farmCattleId = a.systemId; // temporary unique value
        a.createdAt = System.currentTimeMillis();
        long id = animalDao.insert(a);
        a.id = id;
        a.farmCattleId = IdGenerator.make(farmCode, a.category, id);
        animalDao.update(a);
    }

    public void addAnimal(Animal a, String farmCode, Callback<Animal> cb) {
        io.execute(() -> {
            insertAnimalSync(a, farmCode);
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
    public LiveData<Integer> countPregnant() { return animalDao.countPregnant(); }
    public LiveData<Integer> countCalvingBetween(String from, String to) {
        return animalDao.countCalvingBetween(from, to);
    }
    public LiveData<List<Animal>> milkingCows() { return animalDao.milkingCows(); }

    // ---------- Feed ----------

    public LiveData<List<FeedType>> feedTypes() { return feedTypeDao.getAll(); }
    public LiveData<Integer> countLowStock() { return feedTypeDao.countLowStock(); }
    public LiveData<List<FeedLog>> logsByDate(String date) { return feedLogDao.getByDate(date); }
    public LiveData<Double> totalKg(String date) { return feedLogDao.totalKg(date); }
    public LiveData<Double> totalCost(String date) { return feedLogDao.totalCost(date); }

    public void addFeedTypes(List<FeedType> list, Callback<Boolean> cb) {
        io.execute(() -> {
            feedTypeDao.insertAll(list);
            done(cb);
        });
    }

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
            done(cb);
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

    // ---------- Weight, price, batches (beef) ----------

    public LiveData<List<WeightRecord>> weights(long animalId) { return weightDao.byAnimal(animalId); }
    public LiveData<List<Batch>> batches() { return batchDao.getAll(); }
    public LiveData<List<BatchSummary>> batchSummaries() { return batchDao.summaries(); }

    public void addWeight(long animalId, String date, double kg, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                WeightRecord r = new WeightRecord();
                r.animalId = animalId;
                r.date = date;
                r.weightKg = kg;
                r.createdAt = System.currentTimeMillis();
                weightDao.insert(r);

                Animal a = animalDao.findById(animalId);
                WeightRecord latest = weightDao.latest(animalId);
                if (a != null && latest != null) {
                    a.currentWeightKg = latest.weightKg;
                    animalDao.update(a);
                }
            });
            done(cb);
        });
    }

    public void setExpectedPrice(long animalId, double pricePerKg, Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            if (a != null) {
                a.expectedPricePerKg = pricePerKg;
                animalDao.update(a);
            }
            done(cb);
        });
    }

    public void assignBatch(long animalId, long batchId, Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            if (a != null) {
                a.batchId = batchId;
                animalDao.update(a);
            }
            done(cb);
        });
    }

    public void addBatch(Batch batch, Callback<Boolean> cb) {
        io.execute(() -> {
            batch.createdAt = System.currentTimeMillis();
            batchDao.insert(batch);
            done(cb);
        });
    }

    // ---------- Milk, breeding, calving (dairy) ----------

    public LiveData<List<MilkRecord>> milkByDate(String date) { return milkDao.byDate(date); }
    public LiveData<Double> milkTotal(String date) { return milkDao.total(date); }
    public LiveData<Double> milkSum(long animalId, String from, String to) {
        return milkDao.sumRange(animalId, from, to);
    }
    public LiveData<List<BreedingRecord>> breeding(long animalId) { return breedingDao.byAnimal(animalId); }

    public void addMilk(long animalId, String date, String session, double liters,
                        Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            MilkRecord r = new MilkRecord();
            r.animalId = animalId;
            r.animalCode = a == null ? "" : a.farmCattleId;
            r.date = date;
            r.session = session;
            r.liters = liters;
            r.createdAt = System.currentTimeMillis();
            milkDao.insert(r);
            done(cb);
        });
    }

    /**
     * type: HEAT, INSEMINATION, PREGNANCY_POSITIVE, PREGNANCY_NEGATIVE.
     * Result: 0 = saved, 1 = saved but no insemination date, so no calving date.
     */
    public void addBreeding(long animalId, String type, String date, String notes,
                            Callback<Integer> cb) {
        io.execute(() -> {
            final int[] result = {0};
            db.runInTransaction(() -> {
                Animal a = animalDao.findById(animalId);
                if (a == null) return;

                BreedingRecord r = new BreedingRecord();
                r.animalId = animalId;
                r.type = type;
                r.date = date;
                r.notes = notes;
                r.createdAt = System.currentTimeMillis();
                breedingDao.insert(r);

                switch (type) {
                    case "HEAT":
                        a.lastHeatDate = date;
                        break;
                    case "INSEMINATION":
                        a.lastInseminationDate = date;
                        break;
                    case "PREGNANCY_POSITIVE":
                        if (a.lastInseminationDate == null || a.lastInseminationDate.isEmpty()) {
                            result[0] = 1;
                        } else {
                            a.expectedCalvingDate =
                                    DateUtil.plusDays(a.lastInseminationDate, GESTATION_DAYS);
                        }
                        break;
                    case "PREGNANCY_NEGATIVE":
                        a.expectedCalvingDate = null;
                        break;
                    default:
                        break;
                }
                animalDao.update(a);
            });
            main.post(() -> cb.onResult(result[0]));
        });
    }

    /** Creates the calf profile (with its own unique ID) and starts the mother's lactation. */
    public void recordCalving(long motherId, String farmCode, String date, String calfGender,
                              double birthWeightKg, String calfTag, Callback<Animal> cb) {
        io.execute(() -> {
            final Animal[] holder = new Animal[1];
            db.runInTransaction(() -> {
                Animal mother = animalDao.findById(motherId);
                if (mother == null) return;

                Animal calf = new Animal();
                calf.category = "CALF";
                calf.gender = calfGender;
                calf.breed = mother.breed;
                calf.dateOfBirth = date;
                calf.currentWeightKg = birthWeightKg;
                calf.earTag = calfTag;
                calf.motherId = mother.farmCattleId;
                insertAnimalSync(calf, farmCode);

                if (birthWeightKg > 0) {
                    WeightRecord w = new WeightRecord();
                    w.animalId = calf.id;
                    w.date = date;
                    w.weightKg = birthWeightKg;
                    w.createdAt = System.currentTimeMillis();
                    weightDao.insert(w);
                }

                CalvingRecord c = new CalvingRecord();
                c.motherId = mother.id;
                c.calfId = calf.id;
                c.date = date;
                c.calfGender = calfGender;
                c.birthWeightKg = birthWeightKg;
                c.createdAt = System.currentTimeMillis();
                calvingDao.insert(c);

                mother.dairyStatus = "MILKING";
                mother.lactationStartDate = date;
                mother.dryOffDate = null;
                mother.expectedCalvingDate = null;
                animalDao.update(mother);

                holder[0] = calf;
            });
            main.post(() -> cb.onResult(holder[0]));
        });
    }

    public void dryOff(long animalId, String date, Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            if (a != null) {
                a.dairyStatus = "DRY";
                a.dryOffDate = date;
                animalDao.update(a);
            }
            done(cb);
        });
    }
}