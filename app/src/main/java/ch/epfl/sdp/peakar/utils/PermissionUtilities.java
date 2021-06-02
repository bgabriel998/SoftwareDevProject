package ch.epfl.sdp.peakar.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.UserPoint;

/**
 * Utility class for the permissions
 */
public final class PermissionUtilities {
    /**
     * Checks if the camera permission was already granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the location permission was already granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the write permission was already granted
     */
    public static boolean hasWritePermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the read permission was already granted
     */
    public static boolean hasReadPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Creates the permission listener for the requested permissions.
     * After checking the permissions it computes the POIPoints and launches the application
     */
    public static MultiplePermissionsListener createAllPermissionListener(Context context, ConstraintLayout container) {
        return new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if(hasCameraPermission(context)) {
                    container.findViewById(R.id.permissionRequestLayout).setVisibility(View.GONE);
                }
                if(hasLocationPermission(context)){
                    UserPoint.getInstance(context).updateGPSTracker(context);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                showPermissionRationale(permissionToken, context);
            }
        };
    }

    /**
     * Shows a dialog on why the permissions are needed
     * @param permissionToken permissionToken of the rational request
     */
    private static void showPermissionRationale(PermissionToken permissionToken,Context context) {
        new AlertDialog.Builder(context).setTitle(R.string.permission_rationale_title)
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
