package de.techlung.repeatable.backup;


import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.lang.ref.WeakReference;

import de.techlung.repeatable.MainActivity;

class GoogleDriveBackup implements GoogleApiClient.OnConnectionFailedListener {
    @Nullable
    private GoogleApiClient googleApiClient;

    @Nullable
    private WeakReference<Activity> activityRef;

    public void init(@NonNull final Activity activity) {
        this.activityRef = new WeakReference<>(activity);

        googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(MainActivity.getInstance(), this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

    }

    public GoogleApiClient getClient() {
        return googleApiClient;
    }

    public void start() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }

    public void stop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }


    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult result) {
        Log.i("GoogleDriveBackup", "GoogleApiClient connection failed: " + result.toString());

        if (result.hasResolution() && activityRef != null && activityRef.get() != null) {
            Activity a = activityRef.get();
            // show the localized error dialog.
            try {
                result.startResolutionForResult(a, 1);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                GoogleApiAvailability.getInstance().getErrorDialog(a, result.getErrorCode(), 0).show();
            }
        } else {
            Log.d("GoogleDriveBackup", "cannot resolve connection issue");
        }
    }
}