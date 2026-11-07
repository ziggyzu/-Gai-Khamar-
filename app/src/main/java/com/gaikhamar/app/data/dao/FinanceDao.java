package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.AnimalSum;
import com.gaikhamar.app.data.entity.CategoryTotal;
import com.gaikhamar.app.data.entity.FinanceRecord;

import java.util.List;

@Dao
public interface FinanceDao {
    @Insert long insert(FinanceRecord r);

    @Query("SELECT * FROM finance_records WHERE date BETWEEN :from AND :to ORDER BY date DESC, id DESC")
    LiveData<List<FinanceRecord>> between(String from, String to);

    @Query("SELECT * FROM finance_records WHERE date BETWEEN :from AND :to ORDER BY date, id")
    List<FinanceRecord> betweenSync(String from, String to);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM finance_records WHERE type = :type AND date BETWEEN :from AND :to")
    LiveData<Double> sum(String type, String from, String to);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM finance_records WHERE type = :type AND date BETWEEN :from AND :to")
    double sumSync(String type, String from, String to);

    @Query("SELECT category AS category, SUM(amount) AS total FROM finance_records "
            + "WHERE type = :type AND date BETWEEN :from AND :to GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> categoryTotalsSync(String type, String from, String to);

    @Query("SELECT animalId AS animalId, type AS type, SUM(amount) AS total FROM finance_records "
            + "WHERE animalId > 0 GROUP BY animalId, type")
    List<AnimalSum> animalSumsSync();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM finance_records "
            + "WHERE type = 'INCOME' AND category = 'MILK' AND animalId = 0")
    double milkIncomeSync();
}