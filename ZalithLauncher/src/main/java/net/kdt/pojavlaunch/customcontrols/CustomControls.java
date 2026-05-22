package net.kdt.pojavlaunch.customcontrols;

import androidx.annotation.Keep;

import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Keep
public class CustomControls {
	public int version = 8; 
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

		// --- TOP UTILITY BAR BUTTONS ---
		mControlDataList.add(buildDefaultBtn("ESC", 256, "0.0 * ${screen_width} + 10.0", "0.0 * ${screen_height} + 10.0", 60, 40, true));
		mControlDataList.add(buildDefaultBtn("DEBUG", 69, "0.0 * ${screen_width} + 10.0", "0.0 * ${screen_height} + 55.0", 60, 40, true));
		mControlDataList.add(buildDefaultBtn("CHAT", 84, "0.0 * ${screen_width} + 75.0", "0.0 * ${screen_height} + 10.0", 60, 40, true));
		mControlDataList.add(buildDefaultBtn("KEYBOARD", 0, "0.0 * ${screen_width} + 140.0", "0.0 * ${screen_height} + 10.0", 85, 40, true));
		mControlDataList.add(buildDefaultBtn("TAB", 258, "0.0 * ${screen_width} + 230.0", "0.0 * ${screen_height} + 10.0", 60, 40, true));
		mControlDataList.add(buildDefaultBtn("MOUSE", -5, "1.0 * ${screen_width} - 70.0", "0.0 * ${screen_height} + 10.0", 60, 40, true));

		// --- BOTTOM LEFT GRID BUTTONS (D-PAD GRID) ---
		// Row 1
		mControlDataList.add(buildDefaultBtn("PRI", -3, "0.0 * ${screen_width} + 15.0", "1.0 * ${screen_height} - 145.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▲", 87, "0.0 * ${screen_width} + 75.0", "1.0 * ${screen_height} - 145.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("SEC", -4, "0.0 * ${screen_width} + 135.0", "1.0 * ${screen_height} - 145.0", 55, 45, false));
		
		// Row 2
		mControlDataList.add(buildDefaultBtn("◀", 65, "0.0 * ${screen_width} + 15.0", "1.0 * ${screen_height} - 95.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("♢", 0, "0.0 * ${screen_width} + 75.0", "1.0 * ${screen_height} - 95.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▶", 68, "0.0 * ${screen_width} + 135.0", "1.0 * ${screen_height} - 95.0", 55, 45, false));
		
		// Row 3
		mControlDataList.add(buildDefaultBtn("GUI", -2, "0.0 * ${screen_width} + 15.0", "1.0 * ${screen_height} - 45.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("▼", 83, "0.0 * ${screen_width} + 75.0", "1.0 * ${screen_height} - 45.0", 55, 45, false));
		mControlDataList.add(buildDefaultBtn("INV", 69, "0.0 * ${screen_width} + 135.0", "1.0 * ${screen_height} - 45.0", 55, 45, false));
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
		
		// 🔥 FIXED: Using public setters instead of direct private access variable mutation
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
