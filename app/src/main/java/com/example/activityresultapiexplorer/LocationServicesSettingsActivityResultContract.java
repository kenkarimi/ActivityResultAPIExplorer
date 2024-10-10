package com.example.activityresultapiexplorer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationServicesSettingsActivityResultContract extends ActivityResultContract<PendingIntent, Integer> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, PendingIntent pendingIntent) { //Expects a variable of type Intent as its return type, but all we have is PendingIntent and there's no way to cast it. Also accepts null, but that won't work.
        return null;
    }

    @Override
    public Integer parseResult(int resultCode, @Nullable Intent intent) {
        return  resultCode;
    }
}
