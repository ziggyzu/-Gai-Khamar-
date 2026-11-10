package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.Loan;

import java.util.List;

@Dao
public interface LoanDao {
    @Insert long insert(Loan l);
    @Update void update(Loan l);

    @Query("SELECT * FROM loans ORDER BY balance = 0, nextDueDate")
    LiveData<List<Loan>> getAll();

    @Query("SELECT * FROM loans WHERE id = :id")
    Loan findSync(long id);
}
