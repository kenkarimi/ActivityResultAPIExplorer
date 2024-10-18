package com.example.activityresultapiexplorer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GalleryActivityResultContract extends ActivityResultContract<Intent, Uri> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent intent) {
        return intent;
    }

    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        Uri selectedImageUri = intent != null ? intent.getData() : null; //To avoid a null pointer exception for trying to invoke getData() on a null object reference.
        return selectedImageUri;
    }
}
