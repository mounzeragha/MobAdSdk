package com.oqunet.mobad_sdk.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;

public class AdJobCreator implements com.evernote.android.job.JobCreator {
    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SyncAdJob.TAG:
                return new SyncAdJob();
            default:
                return null;
        }
    }
}
