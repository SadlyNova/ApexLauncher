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

public class MainMenuFragment extends FragmentWithAnim {
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

        // 🌟 FIX: Branding "About Nova" se badal kar "About Apex" runtime reflection layout layout se configure kiya
        if (binding != null) {
            View rootLayout = binding.getRoot();
            int aboutNovaId = rootLayout.getResources().getIdentifier("about_nova", "id", rootLayout.getContext().getPackageName());
            if (aboutNovaId != 0) {
                View aboutView = rootLayout.findViewById(aboutNovaId);
                if (aboutView instanceof TextView) {
                    ((TextView) aboutView).setText("About Apex");
                }
            }
        }

        View activityRoot = requireActivity().findViewById(android.R.id.content);
        if (activityRoot != null) {
            ImageView topGamingBtn = activityRoot.findViewById(R.id.btn_override_gaming);
            ImageView topFolderBtn = activityRoot.findViewById(R.id.btn_override_folder);
            ImageView topJavaBtn = activityRoot.findViewById(R.id.btn_override_java);
            ImageView topShareBtn = activityRoot.findViewById(R.id.btn_override_share);
            TextView topTitleView = activityRoot.findViewById(R.id.txt_override_title);

            if (topTitleView != null) {
                topTitleView.setText("Apex Launcher"); // Branding update
                topTitleView.setVisibility(View.VISIBLE);
            }

            if (topGamingBtn != null) {
                topGamingBtn.setOnClickListener(v -> {
                    ViewAnimUtils.setViewAnim(topGamingBtn, Animations.Pulse);
                    ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null);
                });
            }

            if (topFolderBtn != null) {
                topFolderBtn.setOnClickListener(v -> {
                    ViewAnimUtils.setViewAnim(topFolderBtn, Animations.Pulse);
                    Bundle bundle = new Bundle();
                    bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathManager.DIR_GAME_HOME);
                    ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
                });
            }

            if (topJavaBtn != null) {
                topJavaBtn.setOnClickListener(v -> {
                    ViewAnimUtils.setViewAnim(topJavaBtn, Animations.Pulse);
                    runInstallerWithConfirmation(false);
                });
                topJavaBtn.setOnLongClickListener(v -> {
                    runInstallerWithConfirmation(true);
                    return true;
                });
            }

            if (topShareBtn != null) {
                topShareBtn.setOnClickListener(v -> {
                    ViewAnimUtils.setViewAnim(topShareBtn, Animations.Pulse);
                    ZHTools.shareLogs(requireActivity());
                });
            }
        }

        if (binding.customControlButton != null) {
            binding.customControlButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        }
        if (binding.openMainDirButton != null) {
            binding.openMainDirButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathManager.DIR_GAME_HOME);
                ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
            });
        }
        if (binding.installJarButton != null) {
            binding.installJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
            binding.installJarButton.setOnLongClickListener(v -> {
                runInstallerWithConfirmation(true);
                return true;
            });
        }
        if (binding.shareLogsButton != null) {
            binding.shareLogsButton.setOnClickListener(v -> ZHTools.shareLogs(requireActivity()));
        }

        binding.version.setOnClickListener(v -> {
            if (!isTaskRunning()) {
                ZHTools.swapFragmentWithAnim(this, VersionsListFragment.class, VersionsListFragment.TAG, null);
            } else {
                ViewAnimUtils.setViewAnim(binding.version, Animations.Shake);
                TaskExecutors.runInUIThread(() -> Toast.makeText(requireContext(), R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        });
        
        if (binding.editSettingsButton != null) {
            binding.editSettingsButton.setOnClickListener(v -> {
                if (!isTaskRunning()) {
                    ViewAnimUtils.setViewAnim(binding.editSettingsButton, Animations.Pulse);
                    ZHTools.swapFragmentWithAnim(this, VersionManagerFragment.class, VersionManagerFragment.TAG, null);
                } else {
                    ViewAnimUtils.setViewAnim(binding.editSettingsButton, Animations.Shake);
                    TaskExecutors.runInUIThread(() -> Toast.makeText(requireContext(), R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show());
                }
            });
        }

        if (binding.novaDiscord != null) {
            binding.novaDiscord.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://discord.gg/xFpfUufXg3"));
                startActivity(intent);
            });
        }
        if (binding.novaWebsite != null) {
            binding.novaWebsite.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://YOUR_WEBSITE.com"));
                startActivity(intent);
            });
        }
        if (binding.novaGithub != null) {
            binding.novaGithub.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/SadlyNova"));
                startActivity(intent);
            });
        }

        binding.managerProfileButton.setOnClickListener(v -> {
            if (!isTaskRunning()) {
                ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Pulse);
                ZHTools.swapFragmentWithAnim(this, VersionManagerFragment.class, VersionManagerFragment.TAG, null);
            } else {
                ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Shake);
                TaskExecutors.runInUIThread(() -> Toast.makeText(requireContext(), R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        });

        binding.playButton.setOnClickListener(v -> EventBus.getDefault().post(new LaunchGameEvent()));

        if (binding.playButtonsLayout != null) {
            binding.playButtonsLayout.setVisibility(View.VISIBLE);
        }
        if (binding.playButton != null) {
            binding.playButton.setVisibility(View.VISIBLE);
        }

        binding.versionName.setSelected(true);
        binding.versionInfo.setSelected(true);

        refreshCurrentVersion();
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
            binding.managerProfileButton.setVisibility(View.VISIBLE);
        } else {
            binding.versionName.setText(R.string.version_no_versions);
            binding.managerProfileButton.setVisibility(View.GONE);
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
        animPlayer.apply(new AnimPlayer.Entry(binding.playLayout, Animations.BounceInLeft));
        if (binding.playButtonsLayout != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceEnlarge));
        }
        if (binding.editSettingsButton != null) {
            animPlayer.apply(new AnimPlayer.Entry(binding.editSettingsButton, Animations.BounceInLeft));
        }
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
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
