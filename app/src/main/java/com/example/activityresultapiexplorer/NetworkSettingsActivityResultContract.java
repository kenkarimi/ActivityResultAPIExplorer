package com.example.activityresultapiexplorer;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NetworkSettingsActivityResultContract extends ActivityResultContract<Intent, Void> {
    //Creates an intent that can be used for android.app.Activity.startActivityForResult.
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent intent) {
        return intent;
    }

    @Override
    public Void parseResult(int resultCode, @Nullable Intent intent) {
        /**
         * Returns null because the android.provider.Settings.ACTION_SETTINGS intent opens your phone Settings but a result is not expected or needed.
         * As such, to check whether the network is now available/connected, we have to use the NetworkCapabilities class again. You'll find that the approach is different with the LocationServicesSettingsActivityResultContract.
         */
        return null;
    }
}
