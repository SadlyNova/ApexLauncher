package net.kdt.pojavlaunch.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.movtery.zalithlauncher.setting.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CapesSkinsFragment extends Fragment {

    private EditText skinPathInput;
    private EditText capePathInput;
    private ImageView skinRenderView; 
    private boolean isPickingSkin = true;
    private ActivityResultLauncher<Intent> storagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 📁 INTERNAL CACHE LAYER STORAGE REGISTER
        storagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent dataIntent = result.getData();
                    Uri selectedFileUri = dataIntent.getData();
                    
                    if (selectedFileUri != null) {
                        // 👑 BEYOND SCOPED STORAGE: Save image stream into private absolute cache block
                        String savedLocalPath = copyFileToInternalCache(selectedFileUri, isPickingSkin ? "custom_skin.png" : "custom_cape.png");

                        if (savedLocalPath != null) {
                            if (isPickingSkin) {
                                if (skinPathInput != null) skinPathInput.setText(savedLocalPath);
                                Settings.Manager.put("custom_skin_path", savedLocalPath);
                                
                                // Instantly trigger crisp render canvas from our secure cache directory
                                renderCacheImageToPreview(savedLocalPath);
                                Toast.makeText(requireContext(), "Character Skin applied successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                if (capePathInput != null) capePathInput.setText(savedLocalPath);
                                Settings.Manager.put("custom_cape_path", savedLocalPath);
                                Toast.makeText(requireContext(), "Custom Cape applied successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to cache texture asset file!", Toast.LENGTH_SHORT).show();
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

        // 👑 AUTO-LOAD MECHANISM: Reads direct non-restricted absolute files on fragment wake up
        if (view != null) {
            view.postDelayed(() -> {
                try {
                    String savedSkinPath = Settings.Manager.get("custom_skin_path", "");
                    String savedCapePath = Settings.Manager.get("custom_cape_path", "");
                    
                    if (skinPathInput != null) skinPathInput.setText(savedSkinPath);
                    if (capePathInput != null) capePathInput.setText(savedCapePath);
                    
                    if (!savedSkinPath.isEmpty()) {
                        renderCacheImageToPreview(savedSkinPath);
                    }
                } catch (Exception ignored) {}
            }, 150); // Fast 150ms direct stream loading
        }

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
                Toast.makeText(requireContext(), "Texture profiles applied successfully!", Toast.LENGTH_SHORT).show();
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

    // 👑 BITMAP CACHE PIPELINE ENGINE: Copies content providers streams to absolute file paths
    private String copyFileToInternalCache(Uri sourceUri, String outputName) {
        try {
            File cacheFile = new File(requireContext().getCacheDir(), outputName);
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
                 FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                
                byte[] dataBuffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
                    outputStream.write(dataBuffer, 0, bytesRead);
                }
                return cacheFile.getAbsolutePath(); // Returns non-restricted raw local path
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 👑 UNRESTRICTED CRISP PREVIEW RENDER ENGINE
    private void renderCacheImageToPreview(String absoluteFilePath) {
        if (skinRenderView == null || absoluteFilePath == null || absoluteFilePath.isEmpty()) return;
        try {
            File textureFile = new File(absoluteFilePath);
            if (textureFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false; // Prevent auto-scaling artifacts
                Bitmap cachedBitmap = BitmapFactory.decodeFile(textureFile.getAbsolutePath(), options);
                
                if (cachedBitmap != null) {
                    // 👑 PIXEL-PERFECT UPSCALING: Enlarge 64x64 tiny image by 15x
                    // The 'false' filter flag ensures the pixel art stays razor sharp instead of getting blurred!
                    int enlargedWidth = cachedBitmap.getWidth() * 15;
                    int enlargedHeight = cachedBitmap.getHeight() * 15;
                    
                    Bitmap crispBitmap = Bitmap.createScaledBitmap(cachedBitmap, enlargedWidth, enlargedHeight, false);
                    
                    skinRenderView.setImageBitmap(crispBitmap);
                    skinRenderView.invalidate();
                    skinRenderView.requestLayout();
                    return;
                }
            }
        } catch (Exception ignored) {}
        
        // Fallback tint layout state if path file parsing meets internal delays
        skinRenderView.setBackgroundColor(0x339D4EDD);
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
            Toast.makeText(requireContext(), "Storage Manager access failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
