package com.gaikhamar.app.ui;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.databinding.ActivityAnimalPhotoBinding;
import com.gaikhamar.app.util.PhotoHelper;

import java.io.File;

/**
 * Shows the photo for an animal and lets the user take or retake it.
 * Receives the animal row ID via Intent extra EXTRA_ID.
 */
public class AnimalPhotoActivity extends BaseActivity {

    public static final String EXTRA_ID = "animal_id";

    private ActivityAnimalPhotoBinding b;
    private Repository repo;
    private long animalId;
    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<Uri> takePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), saved -> {
                if (saved && photoFile != null && photoFile.exists() && photoFile.length() > 0) {
                    showPhoto();
                    repo.savePhotoPath(animalId, photoFile.getAbsolutePath(),
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, R.string.camera_denied, Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnimalPhotoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        animalId = getIntent().getLongExtra(EXTRA_ID, -1);

        b.btnTake.setOnClickListener(v -> {
            String perm = android.Manifest.permission.CAMERA;
            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermission.launch(perm);
            }
        });

        b.btnBack.setOnClickListener(v -> finish());

        showPhoto();
    }

    private void launchCamera() {
        try {
            photoFile = PhotoHelper.file(this, animalId);
            photoUri  = PhotoHelper.uri(this, animalId);
            takePicture.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhoto() {
        boolean loaded = PhotoHelper.load(this, animalId, b.ivPhoto);
        if (loaded) {
            b.ivPhoto.setVisibility(View.VISIBLE);
            b.tvNoPhoto.setVisibility(View.GONE);
        } else {
            b.ivPhoto.setVisibility(View.GONE);
            b.tvNoPhoto.setVisibility(View.VISIBLE);
        }
    }
}
