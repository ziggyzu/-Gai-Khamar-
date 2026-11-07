package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.Customer;
import com.gaikhamar.app.data.entity.CustomerBalance;
import com.gaikhamar.app.data.entity.LedgerEntry;

import java.util.List;

@Dao
public interface CustomerDao {
    @Insert long insertCustomer(Customer c);
    @Insert long insertEntry(LedgerEntry e);

    @Query("SELECT c.id AS id, c.name AS name, c.phone AS phone, c.ratePerLiter AS ratePerLiter, "
            + "IFNULL(SUM(CASE WHEN l.type = 'DELIVERY' THEN l.amount "
            + "WHEN l.type = 'PAYMENT' THEN -l.amount ELSE 0 END), 0) AS balance "
            + "FROM customers c LEFT JOIN ledger_entries l ON l.customerId = c.id "
            + "GROUP BY c.id ORDER BY c.name")
    LiveData<List<CustomerBalance>> balances();

    @Query("SELECT * FROM customers WHERE id = :id")
    LiveData<Customer> customer(long id);

    @Query("SELECT * FROM customers WHERE id = :id")
    Customer findSync(long id);

    @Query("SELECT * FROM ledger_entries WHERE customerId = :id ORDER BY date DESC, id DESC")
    LiveData<List<LedgerEntry>> entries(long id);

    @Query("SELECT IFNULL(SUM(CASE WHEN type = 'DELIVERY' THEN amount ELSE -amount END), 0) "
            + "FROM ledger_entries WHERE customerId = :id")
    LiveData<Double> balance(long id);
}