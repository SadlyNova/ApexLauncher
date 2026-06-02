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

    // 👑 Dynamic file picker launcher activity registry channel
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String filePath = selectedFileUri.getPath();
                        if (isPickingSkin) {
                            if (skinPathInput != null) skinPathInput.setText(filePath);
                            // Store locally to preferences registry
                            Settings.Manager.put("custom_skin_path", filePath);
                            if (skinRenderView != null) skinRenderView.setBackgroundColor(0xFF9D4EDD); // Changes preview color status dynamically
                        } else {
                            if (capePathInput != null) capePathInput.setText(filePath);
                            Settings.Manager.put("custom_cape_path", filePath);
                        }
                        Toast.makeText(requireContext(), "Texture texture linked successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

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

        // Load existing active values if any
        if (skinPathInput != null) skinPathInput.setText(Settings.Manager.get("custom_skin_path", ""));
        if (capePathInput != null) capePathInput.setText(Settings.Manager.get("custom_cape_path", ""));
        if (skinRenderView != null && !Settings.Manager.get("custom_skin_path", "").isEmpty()) {
            skinRenderView.setBackgroundColor(0xFF9D4EDD); // Render active purple container block placeholder
        }

        // 📁 FOLDER BUTTON 1: Open storage picker for custom character skin
        if (pickSkinBtn != null) {
            pickSkinBtn.setOnClickListener(v -> {
                isPickingSkin = true;
                openStorageFilePicker();
            });
        }

        // 📁 FOLDER BUTTON 2: Open storage picker for cape texture
        if (pickCapeBtn != null) {
            pickCapeBtn.setOnClickListener(v -> {
                isPickingSkin = false;
                openStorageFilePicker();
            });
        }

        // 💾 SAVE BUTTON INTERACTION MAP
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Custom layout configurations applied permanently!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // ❌ CANCEL BUTTON RETURN CHANNEL
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void openStorageFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/png"); // Restrict selection solely towards Minecraft standard asset png texture sheets
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Custom Texture Sheet"));
    }
}
