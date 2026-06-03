package net.kdt.pojavlaunch.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.zalithlauncher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CapesSkinsFragment extends Fragment {

    private EditText skinPathInput;
    private EditText capePathInput;
    private ImageView skinRenderView; 
    private boolean isPickingSkin = true;
    private ActivityResultLauncher<Intent> storagePickerLauncher;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable autoLoadRunnable;

    // 👑 SECURE PREFERENCES HELPER: Replaces the faulty Settings.Manager
    private void savePref(String key, String value) {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("ApexSkinsConfig", Context.MODE_PRIVATE);
            prefs.edit().putString(key, value).apply();
        }
    }

    private String getPref(String key) {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("ApexSkinsConfig", Context.MODE_PRIVATE);
            return prefs.getString(key, "");
        }
        return "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        storagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent dataIntent = result.getData();
                    Uri selectedFileUri = dataIntent.getData();
                    
                    if (selectedFileUri != null) {
                        String savedLocalPath = copyFileToInternalCache(selectedFileUri, isPickingSkin ? "custom_skin.png" : "custom_cape.png");

                        if (savedLocalPath != null) {
                            if (isPickingSkin) {
                                if (skinPathInput != null) skinPathInput.setText(savedLocalPath);
                                savePref("custom_skin_path", savedLocalPath); // 👑 FIXED
                                renderCacheImageToPreview(savedLocalPath);
                                Toast.makeText(requireContext(), "Skin updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                if (capePathInput != null) capePathInput.setText(savedLocalPath);
                                savePref("custom_cape_path", savedLocalPath); // 👑 FIXED
                                Toast.makeText(requireContext(), "Cape updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to copy image to cache!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capes_skins, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        skinPathInput = view.findViewById(R.id.skin_path_input);
        capePathInput = view.findViewById(R.id.cape_path_input);
        skinRenderView = view.findViewById(R.id.skin_render_view);
        
        ImageButton pickSkinBtn = view.findViewById(R.id.btn_pick_skin);
        ImageButton pickCapeBtn = view.findViewById(R.id.btn_pick_cape);
        View btnSave = view.findViewById(R.id.btn_save_skin_flow);
        View btnCancel = view.findViewById(R.id.btn_cancel_skin_flow);

        autoLoadRunnable = () -> {
            if (!isAdded() || getContext() == null) return;
            try {
                // 👑 FIXED: Standard retrieval
                String savedSkinPath = getPref("custom_skin_path");
                String savedCapePath = getPref("custom_cape_path");
                
                if (skinPathInput != null) skinPathInput.setText(savedSkinPath);
                if (capePathInput != null) capePathInput.setText(savedCapePath);
                
                if (!savedSkinPath.isEmpty()) {
                    renderCacheImageToPreview(savedSkinPath);
                }
            } catch (Exception ignored) {}
        };
        mainHandler.postDelayed(autoLoadRunnable, 150);

        if (pickSkinBtn != null) {
            pickSkinBtn.setOnClickListener(v -> {
                isPickingSkin = true;
                triggerSystemFilePicker();
            });
        }

        if (pickCapeBtn != null) {
            pickCapeBtn.setOnClickListener(v -> {
                isPickingSkin = false;
                triggerSystemFilePicker();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Configurations applied!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private String copyFileToInternalCache(Uri sourceUri, String outputName) {
        if (getContext() == null) return null;
        try {
            File cacheFile = new File(requireContext().getCacheDir(), outputName);
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
                 FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                
                byte[] dataBuffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
                    outputStream.write(dataBuffer, 0, bytesRead);
                }
                return cacheFile.getAbsolutePath();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void renderCacheImageToPreview(String absoluteFilePath) {
        if (skinRenderView == null || absoluteFilePath == null || absoluteFilePath.isEmpty()) return;
        try {
            File textureFile = new File(absoluteFilePath);
            if (textureFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false; 
                Bitmap cachedBitmap = BitmapFactory.decodeFile(textureFile.getAbsolutePath(), options);
                
                if (cachedBitmap != null) {
                    int enlargedWidth = cachedBitmap.getWidth() * 15;
                    int enlargedHeight = cachedBitmap.getHeight() * 15;
                    
                    if (enlargedWidth > 0 && enlargedHeight > 0 && enlargedWidth < 4000) {
                        Bitmap crispBitmap = Bitmap.createScaledBitmap(cachedBitmap, enlargedWidth, enlargedHeight, false);
                        if (crispBitmap != null) {
                            skinRenderView.setImageBitmap(crispBitmap);
                            skinRenderView.invalidate();
                            skinRenderView.requestLayout();
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        
        if (skinRenderView != null) skinRenderView.setImageResource(android.R.color.transparent);
    }

    private void triggerSystemFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png"); 
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION 
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION 
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            storagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "System Storage picker failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (mainHandler != null && autoLoadRunnable != null) {
            mainHandler.removeCallbacks(autoLoadRunnable);
        }
        super.onDestroyView();
    }
}
