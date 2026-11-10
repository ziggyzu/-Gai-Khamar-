package com.gaikhamar.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.gaikhamar.app.data.dao.*;
import com.gaikhamar.app.data.entity.*;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.PasswordUtil;
import com.gaikhamar.app.util.LoginIds;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtraRepository {

    private static volatile ExtraRepository INSTANCE;

    public static ExtraRepository get(Context context) {
        if (INSTANCE == null) {
            synchronized (ExtraRepository.class) {
                if (INSTANCE == null) INSTANCE = new ExtraRepository(context.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    private final AppDatabase db;
    private final LoanDao loanDao;
    private final WorkerDao workerDao;
    private final FinanceDao financeDao;
    private final UserDao userDao;
    private final AnimalDao animalDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private ExtraRepository(Context c) {
        db = AppDatabase.get(c);
        loanDao = db.loanDao();
        workerDao = db.workerDao();
        financeDao = db.financeDao();
        userDao = db.userDao();
        animalDao = db.animalDao();
    }

    private void done(Repository.Callback<Boolean> cb) {
        if (cb != null) main.post(() -> cb.onResult(true));
    }

    private void insertExpense(String category, double amount, String date, String note) {
        FinanceRecord r = new FinanceRecord();
        r.type = "EXPENSE";
        r.category = category;
        r.amount = amount;
        r.date = date;
        r.note = note;
        r.createdAt = System.currentTimeMillis();
        financeDao.insert(r);
    }

    // ---------- QR lookup ----------

    /** Returns the animal id, or null if no animal has this farm ID. */
    public void findAnimalIdByCode(String code, Repository.Callback<Long> cb) {
        io.execute(() -> {
            Long id = animalDao.idByCode(code);
            main.post(() -> cb.onResult(id));
        });
    }

    // ---------- Loans ----------

    public LiveData<List<Loan>> loans() { return loanDao.getAll(); }

    public void addLoan(Loan l, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            l.createdAt = System.currentTimeMillis();
            loanDao.insert(l);
            done(cb);
        });
    }

    /** Lowers the balance, moves the next due date by 30 days, and records the expense. */
    public void payLoan(long id, double amount, String date, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                Loan l = loanDao.findSync(id);
                if (l == null) return;
                l.balance = Math.max(0, l.balance - amount);
                l.nextDueDate = l.balance > 0 ? DateUtil.plusDays(date, 30) : "";
                loanDao.update(l);
                insertExpense("OTHER_EXPENSE", amount, date, l.lender);
            });
            done(cb);
        });
    }

    // ---------- Workers ----------

    public LiveData<List<Worker>> workers() { return workerDao.getAll(); }
    public LiveData<List<WorkerDays>> workerDays(String from, String to) { return workerDao.days(from, to); }
    public LiveData<List<Long>> presentOn(String date) { return workerDao.presentOn(date); }

    /** Result: 0 = saved, 1 = the login phone is already registered. */
    /** Result: 0 = saved, 1 = the login phone is missing or already registered. */
    public void addWorker(Worker w, String password, String farmCode, String farmName,
                          Repository.Callback<Integer> cb) {
        io.execute(() -> {
            final int[] result = {0};
            final String loginId = LoginIds.normalize(w.phone);
            db.runInTransaction(() -> {
                if (!password.isEmpty()) {
                    if (loginId.isEmpty() || userDao.findByPhone(loginId) != null) { result[0] = 1; return; }
                    User u = new User();
                    u.name = w.name;
                    u.phone = loginId;
                    u.farmName = farmName;
                    u.farmCode = farmCode;
                    u.passwordHash = PasswordUtil.hash(password);
                    u.role = w.role;
                    u.createdAt = System.currentTimeMillis();
                    userDao.insert(u);
                }
                w.createdAt = System.currentTimeMillis();
                workerDao.insert(w);
            });
            main.post(() -> cb.onResult(result[0]));
        });
    }

    public void setAttendance(long workerId, String date, boolean present, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            Attendance a = new Attendance();
            a.workerId = workerId;
            a.date = date;
            a.present = present ? 1 : 0;
            workerDao.setAttendance(a);
            done(cb);
        });
    }

    public void payWages(String workerName, double amount, String date, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            insertExpense("LABOR", amount, date, workerName);
            done(cb);
        });
    }
    // ---------- Accounts ----------

    public void findUser(String loginId, Repository.Callback<User> cb) {
        io.execute(() -> {
            User u = userDao.findByPhone(loginId);
            main.post(() -> cb.onResult(u));
        });
    }

    /** Creates the user, or updates the saved name and password hash when it already exists. */
    public void saveAdmin(User u, Repository.Callback<User> cb) {
        io.execute(() -> {
            User existing = userDao.findByPhone(u.phone);
            User result;
            if (existing != null) {
                if (!u.passwordHash.isEmpty()) existing.passwordHash = u.passwordHash;
                if (!u.name.isEmpty()) existing.name = u.name;
                userDao.update(existing);
                result = existing;
            } else {
                u.id = userDao.insert(u);
                result = u;
            }
            User out = result;
            main.post(() -> cb.onResult(out));
        });
    }

    // ---------- Animal edits ----------

    public void updateAnimal(long id, String earTag, String name, String breed, String notes,
                             double targetKg, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(id);
            if (a != null) {
                a.earTag = earTag;
                a.name = name;
                a.breed = breed;
                a.notes = notes;
                if (targetKg > 0) a.targetWeightKg = targetKg;
                animalDao.update(a);
            }
            done(cb);
        });
    }

    /** Archives the animal as DEAD and adds the note to its history. */
    public void markDead(long id, String note, Repository.Callback<Boolean> cb) {
        io.execute(() -> {
            Animal a = animalDao.findById(id);
            if (a != null) {
                a.status = "DEAD";
                a.notes = (a.notes == null || a.notes.isEmpty()) ? note : a.notes + "\n" + note;
                animalDao.update(a);
            }
            done(cb);
        });
    }
}
