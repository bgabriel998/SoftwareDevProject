package ch.epfl.sdp.peakar.general;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.osmdroid.config.Configuration;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.SettingsUtilities;

import static ch.epfl.sdp.peakar.utils.PermissionUtilities.hasLocationPermission;

/**
 * Initialises the application:
 * - Requests location, read and write and camera permissions
 * - Initialises firebase
 * - Computes the POIPoints
 * - Downloads the user's data if a user is already authenticated
 */
public class InitActivity extends AppCompatActivity {

    private MultiplePermissionsListener allPermissionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_init);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SettingsUtilities.checkForLanguage(this);
        }

        FirebaseApp.initializeApp(this);

        Database.init(this);

        Log.d("InitActivity", "onCreate: online ? " + Database.getInstance().isOnline());

        createPermissionListener();

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                //        Manifest.permission.READ_EXTERNAL_STORAGE,
                //        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ).withListener(allPermissionsListener)
                .check();

        ProgressBar progressBar = findViewById(R.id.progressBarInitActivity);
        progressBar.setVisibility(View.VISIBLE);

    }

    /**
     * Download the authenticated account data, if present
     */
    private void loadAccount() {
        if (AuthService.getInstance().getAuthAccount() != null) {
            AuthService.getInstance().getAuthAccount().init();
        }
    }

    /**
     * Launches the application
     */
    public void launchApp(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    /**
     * Creates the permission listener for the requested permissions.
     * After checking the permissions it computes the POIPoints and launches the application
     */
    private void createPermissionListener() {
        allPermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if(hasLocationPermission(getApplicationContext())){
                    ComputePOIPoints.getInstance(getApplicationContext());
                }
                new Thread(() -> {
                    Log.d("InitActivity", "onPermissionsChecked: online ? " + Database.getInstance().isOnline());
                    // If user is online, retrieve data
                    if(Database.getInstance().isOnline()) {
                        Log.d("InitActivity", ": loading account");
                        loadAccount();
                    }
                    else {  // Otherwise, try to download data but in another non blocking thread
                        Log.d("InitActivity", ": user offline");
                        new Thread(() -> {
                            try {
                                loadAccount();
                                Log.d("InitActivity", ": successful download of data");
                            } catch(Exception e) {
                                Log.d("InitActivity", ": failed download of data");
                            }

                        }).start();
                    }
                    runOnUiThread(() -> launchApp());
                }).start();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                showPermissionRationale(permissionToken);
            }
        };
    }

    /**
     * Shows a dialog on why the permissions are needed
     * @param permissionToken permissionToken of the rational request
     */
    private void showPermissionRationale(PermissionToken permissionToken) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    permissionToken.cancelPermissionRequest();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    permissionToken.continuePermissionRequest();
                })
                .setOnDismissListener(dialog -> permissionToken.cancelPermissionRequest())
                .show();
    }
}
