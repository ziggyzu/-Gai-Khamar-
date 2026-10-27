package com.gaikhamar.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.gaikhamar.app.data.entity.FeedPurchase;

@Dao
public interface FeedPurchaseDao {
    @Insert
    long insert(FeedPurchase purchase);
}