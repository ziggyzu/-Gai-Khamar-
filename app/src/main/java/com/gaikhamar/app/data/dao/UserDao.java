package com.gaikhamar.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Update
    void update(User user);

    // The "phone" column holds the login id: an email or phone digits
    @Query("SELECT * FROM users WHERE phone = :loginId LIMIT 1")
    User findByPhone(String loginId);
}