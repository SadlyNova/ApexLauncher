package net.kdt.pojavlaunch.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.setting.Settings;

public class CapesSkinsFragment extends Fragment {

    private EditText skinPathInput;
    private EditText capePathInput;
    private View skinRenderView;
    private boolean isPickingSkin = true;
    private ActivityResultLauncher<Intent> storagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 📁 UNIVERSAL FILE PICKER: Android 11 to 16 native storage manager trigger pipeline
        storagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent dataIntent = result.getData();
                    Uri selectedFileUri = dataIntent.getData();
                    if (selectedFileUri != null) {
                        // Persist URI reading permissions dynamically for scoped storage bypass
                        try {
                            int takeFlags = dataIntent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            requireContext().getContentResolver().takePersistableUriPermission(selectedFileUri, takeFlags);
                        } catch (Exception ignored) {}

                        String finalPath = selectedFileUri.toString();

                        if (isPickingSkin) {
                            if (skinPathInput != null) skinPathInput.setText(finalPath);
                            Settings.Manager.put("custom_skin_path", finalPath);
                            // 👑 LIVE STATUS ACTIVE LOOK: Translucent block updates to solid accent state
                            if (skinRenderView != null) {
                                skinRenderView.setBackgroundColor(0xFF9D4EDD); 
                            }
                            Toast.makeText(requireContext(), "Character Skin texture linked!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (capePathInput != null) capePathInput.setText(finalPath);
                            Settings.Manager.put("custom_cape_path", finalPath);
                            Toast.makeText(requireContext(), "Custom Cape texture linked!", Toast.LENGTH_SHORT).show();
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

        // Load pre-existing saved configuration states if any
        try {
            String savedSkin = Settings.Manager.get("custom_skin_path", "");
            String savedCape = Settings.Manager.get("custom_cape_path", "");
            if (skinPathInput != null) skinPathInput.setText(savedSkin);
            if (capePathInput != null) capePathInput.setText(savedCape);
            if (skinRenderView != null && !savedSkin.isEmpty()) {
                skinRenderView.setBackgroundColor(0xFF9D4EDD); // Maintain premium violet state on load
            }
        } catch (Exception ignored) {}

        // 📁 BUTTON 1 CLICK: Select Character Skin (.png)
        if (pickSkinBtn != null) {
            pickSkinBtn.setOnClickListener(v -> {
                isPickingSkin = true;
                triggerSystemFilePicker();
            });
        }

        // 📁 BUTTON 2 CLICK: Select Custom Cape (.png)
        if (pickCapeBtn != null) {
            pickCapeBtn.setOnClickListener(v -> {
                isPickingSkin = false;
                triggerSystemFilePicker();
            });
        }

        // 💾 APPLY/SAVE CONFIGURATIONS ACTION
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Texture profiles applied successfully!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // ❌ CANCEL / GO BACK TRIGGER ACTION
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void triggerSystemFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png"); // Dynamic filter restriction solely for skin sheets
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            storagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "System File Manager not found!", Toast.LENGTH_SHORT).show();
        }
    }
}
