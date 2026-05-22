package net.kdt.pojavlaunch.customcontrols;

import androidx.annotation.Keep;

import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Keep
public class CustomControls {
	public int version = 8; // Force version 8 structural engine compliance
    public float scaledAt;
	public List<ControlData> mControlDataList;
	public List<ControlDrawerData> mDrawerDataList;
	public List<ControlJoystickData> mJoystickDataList;
	public ControlInfoData mControlInfoDataList;

	public CustomControls() {
		this.mControlDataList = new ArrayList<>();
		this.mDrawerDataList = new ArrayList<>();
		this.mJoystickDataList = new ArrayList<>();
		this.mControlInfoDataList = new ControlInfoData();
		this.scaledAt = 100f;

		mControlInfoDataList.name = "Nova Default Controls";
		mControlInfoDataList.desc = "Clean Grid Control Layout Map";
		mControlInfoDataList.author = "NovaDev";
		mControlInfoDataList.version = "1.0";

		// --- 🛠️ TOP UTILITY BAR (Fixed dynamic scaling separation strings) ---
		mControlDataList.add(buildDefaultBtn("ESC", 256, "0.0 * ${screen_width} + (0.01 * ${screen_width})", "0.0 * ${screen_height} + (0.02 * ${screen_height})", 65, 35, true));
		mControlDataList.add(buildDefaultBtn("DEBUG", 69, "0.0 * ${screen_width} + (0.01 * ${screen_width})", "0.0 * ${screen_height} + (0.12 * ${screen_height})", 65, 35, true));
		mControlDataList.add(buildDefaultBtn("CHAT", 84, "0.0 * ${screen_width} + (0.10 * ${screen_width})", "0.0 * ${screen_height} + (0.02 * ${screen_height})", 65, 35, true));
		mControlDataList.add(buildDefaultBtn("KEYBOARD", 0, "0.0 * ${screen_width} + (0.19 * ${screen_width})", "0.0 * ${screen_height} + (0.02 * ${screen_height})", 85, 35, true));
		mControlDataList.add(buildDefaultBtn("TAB", 258, "0.0 * ${screen_width} + (0.31 * ${screen_width})", "0.0 * ${screen_height} + (0.02 * ${screen_height})", 65, 35, true));
		mControlDataList.add(buildDefaultBtn("MOUSE", -5, "1.0 * ${screen_width} - (0.10 * ${screen_width})", "0.0 * ${screen_height} + (0.02 * ${screen_height})", 65, 35, true));

		// --- 🕹️ BOTTOM LEFT D-PAD (Anchored precisely relative to the screen height) ---
		// Row 1 (PRI | ▲ | SEC)
		mControlDataList.add(buildDefaultBtn("PRI", -3, "0.02 * ${screen_width}", "1.0 * ${screen_height} - (px(150.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▲", 87, "0.02 * ${screen_width} + (px(60.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(150.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("SEC", -4, "0.02 * ${screen_width} + (px(120.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(150.0) / 100 * ${preferred_scale})", 55, 45, false));
		
		// Row 2 (◀ | ♢ | ▶)
		mControlDataList.add(buildDefaultBtn("◀", 65, "0.02 * ${screen_width}", "1.0 * ${screen_height} - (px(100.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("♢", 0, "0.02 * ${screen_width} + (px(60.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(100.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▶", 68, "0.02 * ${screen_width} + (px(120.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(100.0) / 100 * ${preferred_scale})", 55, 45, false));
		
		// Row 3 (GUI | ▼ | INV)
		mControlDataList.add(buildDefaultBtn("GUI", -2, "0.02 * ${screen_width}", "1.0 * ${screen_height} - (px(50.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▼", 83, "0.02 * ${screen_width} + (px(60.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(50.0) / 100 * ${preferred_scale})", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("INV", 69, "0.02 * ${screen_width} + (px(120.0) / 100 * ${preferred_scale})", "1.0 * ${screen_height} - (px(50.0) / 100 * ${preferred_scale})", 55, 45, false));
	}

	public CustomControls(List<ControlData> mControlDataList, List<ControlDrawerData> mDrawerDataList, List<ControlJoystickData> mJoystickDataList, ControlInfoData mControlInfoDataList) {
		this.mControlDataList = mControlDataList;
		this.mDrawerDataList = mDrawerDataList;
		this.mJoystickDataList = mJoystickDataList;
		this.mControlInfoDataList = mControlInfoDataList;
		this.scaledAt = 100f;
	}

	private ControlData buildDefaultBtn(String name, int keycode, String dx, String dy, float w, float h, boolean dispInMenu) {
		ControlData data = new ControlData();
		data.name = name;
		data.keycodes = new int[]{keycode, 0, 0, 0};
		data.dynamicX = dx;
		data.dynamicY = dy;
		
		data.setWidth(w);
		data.setHeight(h);
		
		data.opacity = 1.0f;
		data.bgColor = 1291845632;
		data.strokeColor = -1;
		data.strokeWidth = 0.0f;
		data.cornerRadius = 0.0f;
		data.displayInGame = true;
		data.displayInMenu = dispInMenu;
		data.isSwipeable = !dispInMenu; 
		data.isToggle = false;
		data.passThruEnabled = false;
		return data;
	}

	public void save(String path) throws IOException {
		version = 8;
		Tools.write(path, Tools.GLOBAL_GSON.toJson(this));
	}
}
