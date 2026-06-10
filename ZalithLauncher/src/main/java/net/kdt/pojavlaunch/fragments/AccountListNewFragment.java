package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.movtery.zalithlauncher.R;

public class AccountListNewFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load the XML design we just created
        return inflater.inflate(R.layout.fragment_account_list_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind the buttons
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        ImageButton btnHome = view.findViewById(R.id.btn_home);
        MaterialButton btnAddAccount = view.findViewById(R.id.btn_add_account);

        // Setup click actions
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        if (btnAddAccount != null) {
            btnAddAccount.setOnClickListener(v -> {
                // Here we will trigger your account login flow later!
                Toast.makeText(requireContext(), "Opening Login Menu...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
