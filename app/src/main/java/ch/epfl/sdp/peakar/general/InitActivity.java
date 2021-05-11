package ch.epfl.sdp.peakar.general;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.user.services.AuthService;


public class InitActivity extends AppCompatActivity {

    private MultiplePermissionsListener allPermissionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        
        createPermissionListeners();

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ).withListener(allPermissionsListener)
                .check();



        FirebaseApp.initializeApp(this);

        ProgressBar progressBar = findViewById(R.id.progressBarInitActivity);
        progressBar.setVisibility(View.VISIBLE);

        initApp();

        //launchApp();
    }

    private void createPermissionListeners() {
        allPermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                new ComputePOIPoints(getApplicationContext());
                launchApp();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                showPermissionRationale(permissionToken);
            }
        };
    }

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
                .setOnDismissListener(dialog -> {
                    permissionToken.cancelPermissionRequest();
                })
                .show();
    }

    /**
     * Launches the application if the camera permission is granted
     * TODO replace activity with CameraActivity if permission is given
     * TODO replace activity with another activity then CameraActivity if permission is given
     */
    public void launchApp(){
        if(hasCameraPermission()){
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Init application global stuff before opening the main menu
     */
    private synchronized void initApp(){
        if(AuthService.getInstance().getAuthAccount() != null) AuthService.getInstance().getAuthAccount().init();
        //Request and compute the POIPoints
        if(hasLocationPermission()){
            new ComputePOIPoints(this);
        }
    }






    /**
     * Checks if the camera permission was already granted
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the camera permission was already granted
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }
}
