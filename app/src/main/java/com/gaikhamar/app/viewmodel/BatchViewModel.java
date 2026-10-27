package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.data.entity.BatchSummary;

import java.util.List;

public class BatchViewModel extends AndroidViewModel {

    public final LiveData<List<Batch>> batches;
    public final LiveData<List<BatchSummary>> summaries;

    public BatchViewModel(@NonNull Application app) {
        super(app);
        Repository repo = Repository.get(app);
        batches = repo.batches();
        summaries = repo.batchSummaries();
    }
}