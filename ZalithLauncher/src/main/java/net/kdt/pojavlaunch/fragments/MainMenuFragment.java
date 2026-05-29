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
import androidx.fragment.app.Fragment;

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
import com.movtery.zalithlauncher.ui.fragment.FilesFragment;
import com.movtery.zalithlauncher.ui.fragment.FragmentWithAnim;
import com.movtery.zalithlauncher.ui.fragment.VersionManagerFragment;
import com.movtery.zalithlauncher.ui.fragment.VersionsListFragment;
import com.movtery.zalithlauncher.ui.subassembly.account.AccountViewWrapper;
import com.movtery.zalithlauncher.utils.path.PathManager;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainMenuFragment extends FragmentWithAnim implements View.OnClickListener {
    public static final String TAG = "MainMenuFragment";
    private FragmentLauncherBinding binding;
    private AccountViewWrapper accountViewWrapper;
    private ViewTreeObserver.OnWindowFocusChangeListener focusChangeListener;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        accountViewWrapper = new AccountViewWrapper(this, binding.viewAccount);
        accountViewWrapper.refreshAccountInfo();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            focusChangeListener = hasFocus -> {
                View activityRoot = getActivity().findViewById(android.R.id.content);
                if (activityRoot != null) {
                    activityRoot.postDelayed(() -> {
                        try {
                            java.util.ArrayList<View> dialogButtons = new java.util.ArrayList<>();
                            activityRoot.findViewsWithText(dialogButtons, "OK", View.FIND_VIEWS_WITH_TEXT);
                            for (View btn : dialogButtons) {
                                if (btn.isShown()) {
                                    btn.performClick();
                                }
                            }
                        } catch (Exception ignored) {}
                    }, 50);
                }
            };
            view.getViewTreeObserver().addOnWindowFocusChangeListener(focusChangeListener);
        }

        if (binding.aboutText != null) {
            binding.aboutText.setText(InfoCenter.replaceName(requireActivity(), R.string.about_tab));
        }
        if (binding.aboutButton != null) {
            binding.aboutButton.setVisibility(View.GONE); 
        }

        View activityRoot = requireActivity().findViewById(android.R.id.content);
        if (activityRoot != null) {
            ImageView topGamingBtn = activityRoot.findViewById(R.id.btn_override_gaming);
            ImageView topFolderBtn = activityRoot.findViewById(R.id.btn_override_folder);
            ImageView topJavaBtn = activityRoot.findViewById(R.id.btn_override_java);
            ImageView topShareBtn = activityRoot.findViewById(R.id.btn_override_share);
            TextView topTitleView = activityRoot.findViewById(R.id.txt_override_title);

            if (topTitleView != null) {
                topTitleView.setText("Apex Launcher"); 
                topTitleView.setVisibility(View.VISIBLE);
            }

            if (topGamingBtn != null) topGamingBtn.setOnClickListener(this);
            if (topFolderBtn != null) topFolderBtn.setOnClickListener(this);
            if (topJavaBtn != null) {
                topJavaBtn.setOnClickListener(this);
                topJavaBtn.setOnLongClickListener(v -> {
                    runInstallerWithConfirmation(true);
                    return true;
                });
            }
            if (topShareBtn != null) topShareBtn.setOnClickListener(this);
        }

        // Left Navigation Menu Deck Listeners Hook
        View rootLayout = binding.getRoot();
        View tabHome = rootLayout.findViewById(R.id.tab_home);
        if (tabHome != null) tabHome.setOnClickListener(this);

        View tabSettings = rootLayout.findViewById(R.id.tab_settings);
        if (tabSettings != null) tabSettings.setOnClickListener(this);

        View tabMods = rootLayout.findViewById(R.id.tab_mods);
        if (tabMods != null) tabMods.setOnClickListener(this);

        View aboutApexTab = rootLayout.findViewById(R.id.about_apex_tab);
        if (aboutApexTab != null) aboutApexTab.setOnClickListener(this);

        // Standard Icon Trigger Actions Setup
        if (binding.customControlButton != null) binding.customControlButton.setOnClickListener(this);
        if (binding.openMainDirButton != null) binding.openMainDirButton.setOnClickListener(this);
        if (binding.installJarButton != null) {
            binding.installJarButton.setOnClickListener(this);
            binding.installJarButton.setOnLongClickListener(v -> {
                runInstallerWithConfirmation(true);
                return true;
            });
        }
        if (binding.shareLogsButton != null) binding.shareLogsButton.setOnClickListener(this);

        binding.version.setOnClickListener(this);
        if (binding.editSettingsButton != null) binding.editSettingsButton.setOnClickListener(this);
        if (binding.novaDiscord != null) binding.novaDiscord.setOnClickListener(this);
        if (binding.novaWebsite != null) binding.novaWebsite.setOnClickListener(this);
        if (binding.novaGithub != null) binding.novaGithub.setOnClickListener(this);
        if (binding.managerProfileButton != null) binding.managerProfileButton.setOnClickListener(this);
        binding.playButton.setOnClickListener(this);

        binding.versionName.setSelected(true);
        binding.versionInfo.setSelected(true);

        refreshCurrentVersion();
    }

    // 🌟 DYNAMIC DASHBOARD FRAGMENT TRANSITION MATRIX METHOD
    private void switchDashboardFeature(Fragment featureFragment) {
        if (binding != null) {
            // Hero Welcome Layout elements ko safe hide karo taaki view clear ho sake
            if (binding.heroWelcomeArea != null) {
                binding.heroWelcomeArea.setVisibility(View.GONE);
            }
            // ChildFragmentManager ke standard framework transactions call karo
            getChildFragmentManager().beginTransaction()
                .replace(R.id.dashboard_content_container, featureFragment)
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        }
    }

    // 🌟 FULLY LOADED CORE CLICK LOGIC HANDLING REDIRECTIONS
    @Override
    public void onClick(View v) {
        View activityRoot = requireActivity().findViewById(android.R.id.content);
        View topGamingBtn = activityRoot != null ? activityRoot.findViewById(R.id.btn_override_gaming) : null;
        View topFolderBtn = activityRoot != null ? activityRoot.findViewById(R.id.btn_override_folder) : null;
        View topJavaBtn = activityRoot != null ? activityRoot.findViewById(R.id.btn_override_java) : null;
        View topShareBtn = activityRoot != null ? activityRoot.findViewById(R.id.btn_override_share) : null;

        View rootLayout = binding.getRoot();
        View tabHome = rootLayout.findViewById(R.id.tab_home);
        View tabSettings = rootLayout.findViewById(R.id.tab_settings);
        View tabMods = rootLayout.findViewById(R.id.tab_mods);
        View aboutApexTab = rootLayout.findViewById(R.id.about_apex_tab);

        if (v == binding.playButton) {
            EventBus.getDefault().post(new LaunchGameEvent());
        } 
        else if (v == tabHome) {
            ViewAnimUtils.setViewAnim(tabHome, Animations.Pulse);
            // Re-inflate default state wrapper logic
            if (binding.heroWelcomeArea != null) {
                // Pehle active custom fragment layer ko hatao
                Fragment activeFragment = getChildFragmentManager().findFragmentById(R.id.dashboard_content_container);
                if (activeFragment != null) {
                    getChildFragmentManager().beginTransaction().remove(activeFragment).commit();
                }
                binding.heroWelcomeArea.setVisibility(View.VISIBLE);
            }
        } 
        else if (v == tabSettings || v == binding.editSettingsButton || v == binding.managerProfileButton) {
            if (!isTaskRunning()) {
                if (v == binding.editSettingsButton) ViewAnimUtils.setViewAnim(binding.editSettingsButton, Animations.Pulse);
                switchDashboardFeature(new VersionManagerFragment());
            } else {
                TaskExecutors.runInUIThread(() -> Toast.makeText(requireContext(), R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        } 
        else if (v == tabMods || v == binding.customControlButton || v == topGamingBtn) {
            switchDashboardFeature(new ControlButtonFragment());
        } 
        else if (v == aboutApexTab) {
            switchDashboardFeature(new AboutFragment());
        } 
        else if (v == binding.openMainDirButton || v == topFolderBtn) {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathManager.DIR_GAME_HOME);
            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        } 
        else if (v == binding.installJarButton || v == topJavaBtn) {
            runInstallerWithConfirmation(false);
        } 
        else if (v == binding.shareLogsButton || v == topShareBtn) {
            ZHTools.shareLogs(requireActivity());
        } 
        else if (v == binding.version) {
            if (!isTaskRunning()) {
                ZHTools.swapFragmentWithAnim(this, VersionsListFragment.class, VersionsListFragment.TAG, null);
            }
        } 
        else if (v == binding.novaDiscord) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://discord.gg/xFpfUufXg3"));
            startActivity(intent);
        } 
        else if (v == binding.novaWebsite) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/SadlyNova/NovaLauncher-"));
            startActivity(intent);
        } 
        else if (v == binding.novaGithub) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/SadlyNova"));
            startActivity(intent);
        }
    }

    private void refreshCurrentVersion() {
        Version version = VersionsManager.INSTANCE.getCurrentVersion();
        int versionInfoVisibility;
        if (version != null) {
            binding.versionName.setText(version.getVersionName());
            VersionInfo versionInfo = version.getVersionInfo();
            if (versionInfo != null) {
                binding.versionInfo.setText(versionInfo.getInfoString());
                versionInfoVisibility = View.VISIBLE;
            } else versionInfoVisibility = View.GONE;

            new VersionIconUtils(version).start(binding.versionIcon);
            if (binding.managerProfileButton != null) binding.managerProfileButton.setVisibility(View.VISIBLE);
        } else {
            binding.versionName.setText(R.string.version_no_versions);
            if (binding.managerProfileButton != null) binding.managerProfileButton.setVisibility(View.GONE);
            versionInfoVisibility = View.GONE;
        }
        binding.versionInfo.setVisibility(versionInfoVisibility);
    }

    @Subscribe()
    public void event(RefreshVersionsEvent event) {
        if (event.getMode() == END) {
            TaskExecutors.runInUIThread(this::refreshCurrentVersion);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(AccountUpdateEvent event) {
        if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
    }

    @Override
    public void onDestroyView() {
        if (getView() != null && focusChangeListener != null) {
            getView().getViewTreeObserver().removeOnWindowFocusChangeListener(focusChangeListener);
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() { super.onStart(); EventBus.getDefault().register(this); }
    @Override
    public void onStop() { super.onStop(); EventBus.getDefault().unregister(this); }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        if (binding.centeredLogosContainer != null) animPlayer.apply(new AnimPlayer.Entry(binding.centeredLogosContainer, Animations.BounceInDown));
        animPlayer.apply(new AnimPlayer.Entry(binding.playLayout, Animations.BounceInLeft));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        if (binding.centeredLogosContainer != null) animPlayer.apply(new AnimPlayer.Entry(binding.centeredLogosContainer, Animations.FadeOutUp));
        animPlayer.apply(new AnimPlayer.Entry(binding.playLayout, Animations.FadeOutRight));
    }
}
