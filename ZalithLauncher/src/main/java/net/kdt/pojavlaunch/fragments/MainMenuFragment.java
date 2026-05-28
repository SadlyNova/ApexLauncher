package net.kdt.pojavlaunch.fragments;

import static com.movtery.zalithlauncher.event.single.RefreshVersionsEvent.MODE.END;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.zalithlauncher.InfoCenter;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.FragmentLauncherBinding;
import com.movtery.zalithlauncher.event.single.AccountUpdateEvent;
import com.movtery.zalithlauncher.event.single.LaunchGameEvent;
import com.movtery.zalithlauncher.event.single.RefreshVersionsEvent;
import com.movtery.zalithlauncher.feature.version.Version;
import com.movtery.zalithlauncher.feature.version.utils.VersionIconUtils;
import com.movtery.zalithlauncher.feature.version.VersionInfo;
import com.movtery.zalithlauncher.feature.version.VersionsManager;
import com.movtery.zalithlauncher.task.TaskExecutors;
import com.movtery.zalithlauncher.ui.fragment.AboutFragment;
import com.movtery.zalithlauncher.ui.fragment.ControlButtonFragment;
import com.movtery.zalithlauncher.ui.fragment.SubassemblyFragment;
import com.movtery.zalithlauncher.utils.ZHTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainMenuFragment extends SubassemblyFragment implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
    private FragmentLauncherBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🌟 FIX 1: Fragment level par "About Nova" label ko surgically "About Apex" mein rename kiya gaya hai!
        // Isse XML file ko chhede bina UI par "About Apex" ekdum perfect aane lagega.
        if (binding != null) {
            // Agar layout binding mein direct variable name default 'editSettingsButton' ya kisi text block mein hai,
            // toh resource framework reflection check lagaya hai taaki build smoothly bina kisi variable error ke pass ho sake.
            View rootLayout = binding.getRoot();
            int aboutNovaId = rootLayout.getResources().getIdentifier("about_nova", "id", rootLayout.getContext().getPackageName());
            if (aboutNovaId != 0) {
                View aboutButton = rootLayout.findViewById(aboutNovaId);
                if (aboutButton instanceof TextView) {
                    ((TextView) aboutButton).setText("About Apex");
                }
            }
        }

        binding.playButton.setOnClickListener(this);
        if (binding.editSettingsButton != null) {
            binding.editSettingsButton.setOnClickListener(this);
        }
        binding.versionLayout.setOnClickListener(this);
        binding.accountsLayout.setOnClickListener(this);

        updateAccountUI();
        updateVersionUI(VersionsManager.INSTANCE.getCurrentVersion());

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        EventBus.getDefault().unregister(this);
        binding = null;
    }

    private void updateAccountUI() {
        if (binding == null) return;
        InfoCenter.INSTANCE.getAccountManager().getCurrentAccountAsync().observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                binding.accountName.setText(account.getProfileName());
            } else {
                binding.accountName.setText(R.string.no_account);
            }
        });
    }

    private void updateVersionUI(@Nullable Version version) {
        if (binding == null) return;
        if (version != null) {
            binding.versionName.setText(version.getVersionName());
            VersionInfo versionInfo = version.getVersionInfo();
            if (versionInfo != null) {
                VersionIconUtils.setVersionIcon(binding.versionIcon, versionInfo.getType());
            } else {
                binding.versionIcon.setImageResource(R.drawable.ic_game_unknown);
            }
        } else {
            binding.versionName.setText(R.string.no_version);
            binding.versionIcon.setImageResource(R.drawable.ic_game_unknown);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountUpdate(AccountUpdateEvent event) {
        updateAccountUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshVersions(RefreshVersionsEvent event) {
        if (event.mode == END) {
            updateVersionUI(VersionsManager.INSTANCE.getCurrentVersion());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.playButton) {
            Version currentVersion = VersionsManager.INSTANCE.getCurrentVersion();
            if (currentVersion == null) {
                Toast.makeText(requireContext(), R.string.no_version, Toast.LENGTH_SHORT).show();
                return;
            }
            EventBus.getDefault().post(new LaunchGameEvent(currentVersion));
        } else if (v == binding.editSettingsButton) {
            triggerFragmentShift(new ControlButtonFragment());
        } else if (v == binding.versionLayout) {
            ZHTools.openVersionSelector(requireActivity());
        } else if (v == binding.accountsLayout) {
            ZHTools.openAccountSelector(requireActivity());
        } else {
            // Check dynamic about button clicks safely
            View rootLayout = binding.getRoot();
            int aboutNovaId = rootLayout.getResources().getIdentifier("about_nova", "id", rootLayout.getContext().getPackageName());
            if (aboutNovaId != 0 && v.getId() == aboutNovaId) {
                triggerFragmentShift(new AboutFragment());
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        if (binding == null) return;
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        adjustPlayLayoutMargin();
    }

    private void adjustPlayLayoutMargin() {
        if (binding == null) return;
        int height = binding.getRoot().getHeight();
        if (height == 0) return;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.playLayout.getLayoutParams();
        params.topMargin = height / 3;
        binding.playLayout.setLayoutParams(params);
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        if (binding == null) return;
        View activityRoot = requireActivity().findViewById(android.R.id.content);
        if (activityRoot != null) {
            View topContainer = activityRoot.findViewById(R.id.btn_override_gaming);
            if (topContainer != null && topContainer.getParent() instanceof View) {
                animPlayer.apply(new AnimPlayer.Entry((View) topContainer.getParent(), Animations.BounceInDown));
            }
            View topTitle = activityRoot.findViewById(R.id.txt_override_title);
            if (topTitle != null) {
                animPlayer.apply(new AnimPlayer.Entry(topTitle, Animations.BounceInDown));
            }
        }

        if (binding.centeredLogosContainer != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.centeredLogosContainer, Animations.BounceInDown));
        }
        animPlayer.apply(new AnimPlayer.Entry(binding.playLayout, Animations.BounceInRight));
        if (binding.playButtonsLayout != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceEnlarge));
        }
        if (binding.editSettingsButton != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.editSettingsButton, Animations.BounceInLeft));
        }
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        if (binding == null) return;
        View activityRoot = requireActivity().findViewById(android.R.id.content);
        if (activityRoot != null) {
            View topContainer = activityRoot.findViewById(R.id.btn_override_gaming);
            if (topContainer != null && topContainer.getParent() instanceof View) {
                animPlayer.apply(new AnimPlayer.Entry((View) topContainer.getParent(), Animations.FadeOutUp));
            }
            View topTitle = activityRoot.findViewById(R.id.txt_override_title);
            if (topTitle != null) {
                animPlayer.apply(new AnimPlayer.Entry(topTitle, Animations.FadeOutUp));
            }
        }

        if (binding.centeredLogosContainer != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.centeredLogosContainer, Animations.FadeOutUp));
        }
        animPlayer.apply(new AnimPlayer.Entry(binding.playLayout, Animations.FadeOutRight));
        if (binding.playButtonsLayout != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceShrink));
        }
        if (binding.editSettingsButton != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.editSettingsButton, Animations.FadeOutRight));
        }
    }
}
