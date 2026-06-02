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
        
        // STORAGE RESOURCE LINKER with secure bitmap renderer
        storagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent dataIntent = result.getData();
                    Uri selectedFileUri = dataIntent.getData();
                    
                    if (selectedFileUri != null) {
                        try {
                            final int takeFlags = dataIntent.getFlags() 
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            requireContext().getContentResolver().takePersistableUriPermission(selectedFileUri, takeFlags);
                        } catch (Exception e) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception ignored) {}
                        }

                        String finalUriString = selectedFileUri.toString();

                        if (isPickingSkin) {
                            if (skinPathInput != null) skinPathInput.setText(finalUriString);
                            Settings.Manager.put("custom_skin_path", finalUriString);
                            
                            // Live Preview render machine trigger
                            renderSelectedImageToPreview(selectedFileUri);
                            Toast.makeText(requireContext(), "Skin URI registered successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (capePathInput != null) capePathInput.setText(finalUriString);
                            Settings.Manager.put("custom_cape_path", finalUriString);
                            Toast.makeText(requireContext(), "Cape URI registered successfully!", Toast.LENGTH_SHORT).show();
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

        // 👑 ASYNCHRONOUS AUTO-LOAD ENGINE: Delay rendering slightly for system storage providers to wake up safely
        if (view != null) {
            view.postDelayed(() -> {
                try {
                    String savedSkin = Settings.Manager.get("custom_skin_path", "");
                    String savedCape = Settings.Manager.get("custom_cape_path", "");
                    
                    if (skinPathInput != null) skinPathInput.setText(savedSkin);
                    if (capePathInput != null) capePathInput.setText(savedCape);
                    
                    if (!savedSkin.isEmpty()) {
                        renderSelectedImageToPreview(Uri.parse(savedSkin));
                    }
                } catch (Exception ignored) {}
            }, 250); // Safe 250ms hardware hand-shake interval
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
                Toast.makeText(requireContext(), "Texture profiles successfully deployed!", Toast.LENGTH_SHORT).show();
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

    // LIVE BITMAP DRAW ENGINE WITH FORCED VIEW INVALIDATION MATRIX
    private void renderSelectedImageToPreview(Uri imageUri) {
        if (skinRenderView == null) return;
        try (InputStream imageStream = requireContext().getContentResolver().openInputStream(imageUri)) {
            Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);
            if (selectedBitmap != null) {
                // Draw pixels layout explicitly
                skinRenderView.setImageBitmap(selectedBitmap);
                
                // 👑 FORCE REFRESH SIGNAL: Compels the Android hardware layer to re-draw layout pixels immediately
                skinRenderView.invalidate();
                skinRenderView.requestLayout();
            }
        } catch (Exception e) {
            // Soft reset fallback
            skinRenderView.setImageResource(android.R.color.transparent);
        }
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
