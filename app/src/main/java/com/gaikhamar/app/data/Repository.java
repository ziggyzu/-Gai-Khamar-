package com.gaikhamar.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.gaikhamar.app.data.dao.*;
import com.gaikhamar.app.data.entity.*;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.IdGenerator;
import com.gaikhamar.app.util.PasswordUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                if (INSTANCE == null) INSTANCE = new Repository(context.getApplicationContext());
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
    private final HealthDao healthDao;
    private final VaccinationDao vaccinationDao;
    private final ContactDao contactDao;
    private final FinanceDao financeDao;
    private final CustomerDao customerDao;
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
        healthDao = db.healthDao();
        vaccinationDao = db.vaccinationDao();
        contactDao = db.contactDao();
        financeDao = db.financeDao();
        customerDao = db.customerDao();
    }

    private void done(Callback<Boolean> cb) {
        if (cb != null) main.post(() -> cb.onResult(true));
    }

    private void insertFinanceSync(String type, String category, double amount, String date,
                                   String note, long animalId) {
        FinanceRecord r = new FinanceRecord();
        r.type = type;
        r.category = category;
        r.amount = amount;
        r.date = date;
        r.note = note;
        r.animalId = animalId;
        r.createdAt = System.currentTimeMillis();
        financeDao.insert(r);
    }

    // ---------- Users ----------

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

    public void login(String phone, String password, Callback<User> cb) {
        io.execute(() -> {
            User u = userDao.findByPhone(phone);
            User result = (u != null && PasswordUtil.verify(password, u.passwordHash)) ? u : null;
            main.post(() -> cb.onResult(result));
        });
    }

    // ---------- Animals ----------

    private void insertAnimalSync(Animal a, String farmCode) {
        a.systemId = UUID.randomUUID().toString();
        a.farmCattleId = a.systemId;
        a.createdAt = System.currentTimeMillis();
        long id = animalDao.insert(a);
        a.id = id;
        a.farmCattleId = IdGenerator.make(farmCode, a.category, id);
        animalDao.update(a);
    }

    public void addAnimal(Animal a, String farmCode, Callback<Animal> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                insertAnimalSync(a, farmCode);
                if (a.purchasePrice > 0) {
                    String d = a.purchaseDate == null || a.purchaseDate.isEmpty()
                            ? DateUtil.today() : a.purchaseDate;
                    insertFinanceSync("EXPENSE", "ANIMAL_PURCHASE", a.purchasePrice, d, a.farmCattleId, a.id);
                }
            });
            main.post(() -> cb.onResult(a));
        });
    }

    public LiveData<List<Animal>> filter(String category, String query) { return animalDao.filter(category, query); }
    public LiveData<Animal> getAnimal(long id) { return animalDao.getById(id); }
    public LiveData<Integer> countAll() { return animalDao.countAll(); }
    public LiveData<Integer> countByCategory(String c) { return animalDao.countByCategory(c); }
    public LiveData<Integer> countSick() { return animalDao.countSick(); }
    public LiveData<Integer> countByDairyStatus(String s) { return animalDao.countByDairyStatus(s); }
    public LiveData<Integer> countPregnant() { return animalDao.countPregnant(); }
    public LiveData<Integer> countCalvingBetween(String from, String to) { return animalDao.countCalvingBetween(from, to); }
    public LiveData<List<Animal>> milkingCows() { return animalDao.milkingCows(); }

    public void sellAnimal(long animalId, double price, String buyer, String date, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                Animal a = animalDao.findById(animalId);
                if (a == null) return;
                a.status = "SOLD";
                animalDao.update(a);
                insertFinanceSync("INCOME", "CALF".equals(a.category) ? "CALF_SALE" : "ANIMAL_SALE",
                        price, date, buyer, animalId);
            });
            done(cb);
        });
    }

    /** Stores the photo file path in the animal's notes field (prefixed with "photo:"). */
    public void savePhotoPath(long animalId, String path, Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            if (a != null) {
                a.notes = "photo:" + path;
                animalDao.update(a);
            }
            done(cb);
        });
    }

    public Animal getAnimalSync(long id) { return animalDao.findById(id); }

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

    public void addStock(long feedTypeId, double qtyKg, double pricePerKg, String date, Callback<Boolean> cb) {
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

                if (pricePerKg > 0) insertFinanceSync("EXPENSE", "FEED", qtyKg * pricePerKg, date, t.name, 0);
            });
            done(cb);
        });
    }

    /** Result: 0 = saved, 1 = not enough stock, 2 = feed type not found. */
    public void logFeed(long feedTypeId, String groupCode, double qtyKg, String date, Callback<Integer> cb) {
        io.execute(() -> {
            final int[] result = {0};
            db.runInTransaction(() -> {
                FeedType t = feedTypeDao.findById(feedTypeId);
                if (t == null) { result[0] = 2; return; }
                if (qtyKg > t.stockKg + 0.0001) { result[0] = 1; return; }
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

    // ---------- Weight, price, batches ----------

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
            if (a != null) { a.expectedPricePerKg = pricePerKg; animalDao.update(a); }
            done(cb);
        });
    }

    public void assignBatch(long animalId, long batchId, Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(animalId);
            if (a != null) { a.batchId = batchId; animalDao.update(a); }
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

    // ---------- Milk, breeding, calving ----------

    public LiveData<List<MilkRecord>> milkByDate(String date) { return milkDao.byDate(date); }
    public LiveData<Double> milkTotal(String date) { return milkDao.total(date); }
    public LiveData<Double> milkSum(long animalId, String from, String to) { return milkDao.sumRange(animalId, from, to); }
    public LiveData<List<BreedingRecord>> breeding(long animalId) { return breedingDao.byAnimal(animalId); }

    public void addMilk(long animalId, String date, String session, double liters, Callback<Boolean> cb) {
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

    /** Result: 0 = saved, 1 = saved but no insemination date, so no calving date. */
    public void addBreeding(long animalId, String type, String date, String notes, Callback<Integer> cb) {
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
                            a.expectedCalvingDate = DateUtil.plusDays(a.lastInseminationDate, GESTATION_DAYS);
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
            if (a != null) { a.dairyStatus = "DRY"; a.dryOffDate = date; animalDao.update(a); }
            done(cb);
        });
    }

    // ---------- Health ----------

    public LiveData<List<HealthRecord>> healthRecords(long animalId) { return healthDao.byAnimal(animalId); }

    public void addHealthRecord(long animalId, String date, String condition, String symptoms,
                                double temp, String treatment, String vet, double cost,
                                int withdrawalDays, String followUp, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                Animal a = animalDao.findById(animalId);
                if (a == null) return;
                HealthRecord r = new HealthRecord();
                r.animalId = animalId;
                r.animalCode = a.farmCattleId;
                r.date = date;
                r.condition = condition;
                r.symptoms = symptoms;
                r.temperatureC = temp;
                r.treatment = treatment;
                r.vetName = vet;
                r.cost = cost;
                r.withdrawalDays = withdrawalDays;
                r.followUpDate = followUp;
                r.status = "ONGOING";
                r.createdAt = System.currentTimeMillis();
                healthDao.insert(r);

                if (!"QUARANTINE".equals(a.healthStatus)) a.healthStatus = "TREATMENT";
                if (withdrawalDays > 0) {
                    String until = DateUtil.plusDays(date, withdrawalDays);
                    if (a.withdrawalUntil == null || a.withdrawalUntil.compareTo(until) < 0) {
                        a.withdrawalUntil = until;
                    }
                }
                animalDao.update(a);
                if (cost > 0) insertFinanceSync("EXPENSE", "MEDICINE", cost, date, a.farmCattleId, animalId);
            });
            done(cb);
        });
    }

    public void markRecovered(long animalId, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                for (HealthRecord r : healthDao.ongoingSync(animalId)) {
                    r.status = "RECOVERED";
                    healthDao.update(r);
                }
                Animal a = animalDao.findById(animalId);
                if (a != null) { a.healthStatus = "HEALTHY"; animalDao.update(a); }
            });
            done(cb);
        });
    }

    public void toggleQuarantine(long animalId, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                Animal a = animalDao.findById(animalId);
                if (a == null) return;
                if ("QUARANTINE".equals(a.healthStatus)) {
                    a.healthStatus = healthDao.ongoingSync(animalId).isEmpty() ? "HEALTHY" : "TREATMENT";
                } else {
                    a.healthStatus = "QUARANTINE";
                }
                animalDao.update(a);
            });
            done(cb);
        });
    }

    // ---------- Vaccinations ----------

    public LiveData<List<Vaccination>> vaccinations() { return vaccinationDao.getAll(); }
    public LiveData<Integer> vaccinesDueCount(String until) { return vaccinationDao.dueCount(until); }

    public void addVaccinations(List<Vaccination> list, Callback<Boolean> cb) {
        io.execute(() -> {
            vaccinationDao.insertAll(list);
            done(cb);
        });
    }

    public void markVaccinationGiven(long id, String date, double cost, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                Vaccination v = vaccinationDao.findById(id);
                if (v == null) return;
                v.lastDate = date;
                v.nextDueDate = DateUtil.plusDays(date, v.intervalDays);
                vaccinationDao.update(v);
                if (cost > 0) insertFinanceSync("EXPENSE", "MEDICINE", cost, date, v.name, 0);
            });
            done(cb);
        });
    }

    // ---------- Contacts ----------

    public LiveData<List<FarmContact>> contacts() { return contactDao.getAll(); }

    public void addContact(FarmContact c, Callback<Boolean> cb) {
        io.execute(() -> {
            c.createdAt = System.currentTimeMillis();
            contactDao.insert(c);
            done(cb);
        });
    }

    public void deleteContact(FarmContact c, Callback<Boolean> cb) {
        io.execute(() -> {
            contactDao.delete(c);
            done(cb);
        });
    }

    // ---------- Reminders (call on a background thread) ----------

    public ReminderCounts reminderCountsSync() {
        String today = DateUtil.today();
        ReminderCounts rc = new ReminderCounts();
        rc.vaccinesDue = vaccinationDao.dueCountSync(DateUtil.plusDays(today, 7));
        rc.calvingSoon = animalDao.calvingBetweenSync(today, DateUtil.plusDays(today, 14));
        rc.lowStock = feedTypeDao.lowStockCountSync();
        rc.followUps = healthDao.followUpDueSync(today);
        rc.withdrawals = animalDao.withdrawalCountSync(today);
        return rc;
    }

    // ---------- Finance ----------

    public LiveData<List<FinanceRecord>> financeBetween(String from, String to) { return financeDao.between(from, to); }
    public LiveData<Double> financeSum(String type, String from, String to) { return financeDao.sum(type, from, to); }

    public void addFinance(String type, String category, double amount, String date,
                           String note, long animalId, Callback<Boolean> cb) {
        io.execute(() -> {
            insertFinanceSync(type, category, amount, date, note, animalId);
            done(cb);
        });
    }

    // Used for exports: call on a background thread
    public List<FinanceRecord> financeBetweenSync(String from, String to) { return financeDao.betweenSync(from, to); }
    public double sumSync(String type, String from, String to) { return financeDao.sumSync(type, from, to); }
    public List<CategoryTotal> categoryTotalsSync(String type, String from, String to) {
        return financeDao.categoryTotalsSync(type, from, to);
    }

    /** Result: [0] = income per month, [1] = expense per month, for the given "yyyy-MM" keys. */
    public void monthlySeries(String[] keys, Callback<double[][]> cb) {
        io.execute(() -> {
            double[][] out = new double[2][keys.length];
            for (int i = 0; i < keys.length; i++) {
                out[0][i] = financeDao.sumSync("INCOME", keys[i] + "-01", keys[i] + "-31");
                out[1][i] = financeDao.sumSync("EXPENSE", keys[i] + "-01", keys[i] + "-31");
            }
            main.post(() -> cb.onResult(out));
        });
    }

    // ---------- Profit report (estimate) ----------

    private String feedGroupOf(Animal a) {
        if ("CALF".equals(a.category)) return "CALF";
        if ("DAIRY".equals(a.category)) return "MILKING".equals(a.dairyStatus) ? "MILKING" : "DRY";
        return "BEEF";
    }

    public void loadProfit(Callback<ProfitReport> cb) {
        io.execute(() -> {
            List<Animal> animals = animalDao.allSync();

            Map<Long, double[]> direct = new HashMap<>();   // [income, cost]
            for (AnimalSum s : financeDao.animalSumsSync()) {
                double[] v = direct.get(s.animalId);
                if (v == null) { v = new double[2]; direct.put(s.animalId, v); }
                if ("INCOME".equals(s.type)) v[0] += s.total; else v[1] += s.total;
            }

            double milkIncome = financeDao.milkIncomeSync();
            double totalLiters = 0;
            Map<Long, Double> liters = new HashMap<>();
            for (AnimalLiters l : milkDao.litersByAnimalSync()) {
                liters.put(l.animalId, l.liters);
                totalLiters += l.liters;
            }

            Map<String, Double> feedByGroup = new HashMap<>();
            for (CategoryTotal c : feedLogDao.costByGroupSync()) feedByGroup.put(c.category, c.total);

            Map<String, Integer> groupCount = new HashMap<>();
            for (Animal a : animals) {
                if (!"ACTIVE".equals(a.status)) continue;
                String g = feedGroupOf(a);
                Integer n = groupCount.get(g);
                groupCount.put(g, n == null ? 1 : n + 1);
            }

            Map<Long, String> batchNames = new HashMap<>();
            for (Batch b : batchDao.allSync()) batchNames.put(b.id, b.name);

            ProfitReport rep = new ProfitReport();
            Map<Long, ProfitRow> batchRows = new LinkedHashMap<>();
            Map<String, ProfitRow> catRows = new LinkedHashMap<>();
            long catId = 1;

            for (Animal a : animals) {
                double[] d = direct.get(a.id);
                double income = d == null ? 0 : d[0];
                double cost = d == null ? 0 : d[1];

                Double lit = liters.get(a.id);
                if (lit != null && totalLiters > 0) income += milkIncome * lit / totalLiters;

                if ("ACTIVE".equals(a.status)) {
                    String g = feedGroupOf(a);
                    Integer n = groupCount.get(g);
                    Double fc = feedByGroup.get(g);
                    if (n != null && n > 0 && fc != null) cost += fc / n;
                }

                rep.animals.add(new ProfitRow(a.id, a.farmCattleId, a.category, income, cost));

                ProfitRow br = batchRows.get(a.batchId);
                if (br == null) {
                    String name = batchNames.get(a.batchId);
                    br = new ProfitRow(a.batchId, name == null ? "" : name, "", 0, 0);
                    batchRows.put(a.batchId, br);
                }
                br.income += income;
                br.cost += cost;

                ProfitRow cr = catRows.get(a.category);
                if (cr == null) {
                    cr = new ProfitRow(catId++, a.category, a.category, 0, 0);
                    catRows.put(a.category, cr);
                }
                cr.income += income;
                cr.cost += cost;
            }
            Collections.sort(rep.animals, (x, y) -> Double.compare(x.profit(), y.profit()));
            rep.batches.addAll(batchRows.values());
            rep.categories.addAll(catRows.values());
            main.post(() -> cb.onResult(rep));
        });
    }

    // ---------- Milk customers ----------

    public LiveData<List<CustomerBalance>> customerBalances() { return customerDao.balances(); }
    public LiveData<Customer> customer(long id) { return customerDao.customer(id); }
    public LiveData<List<LedgerEntry>> ledger(long id) { return customerDao.entries(id); }
    public LiveData<Double> customerBalance(long id) { return customerDao.balance(id); }

    public void addCustomer(Customer c, Callback<Boolean> cb) {
        io.execute(() -> {
            c.createdAt = System.currentTimeMillis();
            customerDao.insertCustomer(c);
            done(cb);
        });
    }

    public void addDelivery(long customerId, String date, double liters, double rate, Callback<Boolean> cb) {
        io.execute(() -> {
            LedgerEntry e = new LedgerEntry();
            e.customerId = customerId;
            e.date = date;
            e.type = "DELIVERY";
            e.liters = liters;
            e.amount = liters * rate;
            e.createdAt = System.currentTimeMillis();
            customerDao.insertEntry(e);
            done(cb);
        });
    }

    /** Income is recorded when money is received. */
    public void receivePayment(long customerId, String date, double amount, Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                LedgerEntry e = new LedgerEntry();
                e.customerId = customerId;
                e.date = date;
                e.type = "PAYMENT";
                e.amount = amount;
                e.createdAt = System.currentTimeMillis();
                customerDao.insertEntry(e);
                Customer c = customerDao.findSync(customerId);
                insertFinanceSync("INCOME", "MILK", amount, date, c == null ? "" : c.name, 0);
            });
            done(cb);
        });
    }
}