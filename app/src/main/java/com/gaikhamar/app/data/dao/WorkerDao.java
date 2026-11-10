package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.Attendance;
import com.gaikhamar.app.data.entity.Worker;
import com.gaikhamar.app.data.entity.WorkerDays;

import java.util.List;

@Dao
public interface WorkerDao {
    @Insert long insert(Worker w);

    @Query("SELECT * FROM workers ORDER BY name")
    LiveData<List<Worker>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setAttendance(Attendance a);

    @Query("SELECT workerId AS workerId, SUM(present) AS days FROM attendance "
            + "WHERE date BETWEEN :from AND :to GROUP BY workerId")
    LiveData<List<WorkerDays>> days(String from, String to);

    @Query("SELECT workerId FROM attendance WHERE date = :date AND present = 1")
    LiveData<List<Long>> presentOn(String date);
}
