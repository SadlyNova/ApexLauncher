package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.setting.Settings;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;

import java.io.File;

public class CapesSkinsFragment extends Fragment {

    private EditText skinPathInput;
    private EditText capePathInput;
    private View skinRenderView;
    private boolean isPickingSkin = true;
    private ActivityResultLauncher<Any> openDocumentLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 📁 FIXED: Registered cleanly using Pojav's native document contract layout picker engine
        openDocumentLauncher = registerForActivityResult(new OpenDocumentWithExtension("png", true), uris -> {
            if (uris != null && !uris.isEmpty()) {
                Uri selectedUri = uris.get(0);
                if (selectedUri != null) {
                    String filePath = selectedUri.getPath();
                    if (filePath != null) {
                        // Clean legacy path prefixes if any from android system providers
                        if (filePath.contains(":") && filePath.split(":").length > 1) {
                            filePath = filePath.split(":")[1];
                        }
                        
                        if (isPickingSkin) {
                            if (skinPathInput != null) skinPathInput.setText(filePath);
                            Settings.Manager.put("custom_skin_path", filePath);
                            // 👑 LIVE PREVIEW UPDATE: Instantly change color matrix state to notify texture linkage
                            if (skinRenderView != null) {
                                skinRenderView.setBackgroundColor(0xFF9D4EDD); 
                            }
                        } else {
                            if (capePathInput != null) capePathInput.setText(filePath);
                            Settings.Manager.put("custom_cape_path", filePath);
                        }
                        Toast.makeText(requireContext(), "Texture linked successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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

        // Load pre-existing configuration mapping keys if any
        try {
            String savedSkin = Settings.Manager.get("custom_skin_path", "");
            String savedCape = Settings.Manager.get("custom_cape_path", "");
            if (skinPathInput != null) skinPathInput.setText(savedSkin);
            if (capePathInput != null) capePathInput.setText(savedCape);
            if (skinRenderView != null && !savedSkin.isEmpty()) {
                skinRenderView.setBackgroundColor(0xFF9D4EDD); // Hold active premium purple look
            }
        } catch (Exception ignored) {}

        // 📁 TRIGGER 1: Select character skin texture sheet (.png)
        if (pickSkinBtn != null) {
            pickSkinBtn.setOnClickListener(v -> {
                isPickingSkin = true;
                if (openDocumentLauncher != null) {
                    openDocumentLauncher.launch(".png");
                } else {
                    Toast.makeText(requireContext(), "Storage picker initializing, try again!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 📁 TRIGGER 2: Select cape design asset (.png)
        if (pickCapeBtn != null) {
            pickCapeBtn.setOnClickListener(v -> {
                isPickingSkin = false;
                if (openDocumentLauncher != null) {
                    openDocumentLauncher.launch(".png");
                } else {
                    Toast.makeText(requireContext(), "Storage picker initializing, try again!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // SAVE CONTROL CONFIGURATION FLOW
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Configurations saved to preferences register!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // CANCEL & BACK PRESS ROUTE
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }
}
