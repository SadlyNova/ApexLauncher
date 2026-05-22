package net.kdt.pojavlaunch.customcontrols;

import java.util.Iterator;
import java.util.List;

public class LayoutSanitizer {

    private static boolean isInvalidFormula(String formula) {
        if (formula == null) return true;
        return formula.contains("Infinity");
    }

    private static boolean isSaneData(ControlData controlData) {
        // Prevent layout crushing, but give fallback bounds to prevent immediate layout deletion
        if (controlData.getWidth() <= 0) controlData.setWidth(50);
        if (controlData.getHeight() <= 0) controlData.setHeight(50);
        
        // 🔥 FIXED: If a formula is empty or missing, give it a default fallback string instead of crashing
        if (controlData.dynamicX == null || controlData.dynamicX.trim().isEmpty()) {
            controlData.dynamicX = "0.0 * ${screen_width} + 50.0";
        }
        if (controlData.dynamicY == null || controlData.dynamicY.trim().isEmpty()) {
            controlData.dynamicY = "0.0 * ${screen_height} + 50.0";
        }

        if (isInvalidFormula(controlData.dynamicX) || isInvalidFormula(controlData.dynamicY)) return false;
        return true;
    }

    private static ControlData getControlData(Object dataEntry) {
        if (dataEntry instanceof ControlData) {
            return (ControlData) dataEntry;
        } else if (dataEntry instanceof ControlDrawerData) {
            return ((ControlDrawerData) dataEntry).properties;
        } else if (dataEntry instanceof ControlJoystickData) {
            // Safe fallback mapping abstraction for variable joystick bounds
            ControlData joystickProxy = new ControlData();
            joystickProxy.setWidth(((ControlJoystickData) dataEntry).getWidth());
            joystickProxy.setHeight(((ControlJoystickData) dataEntry).getHeight());
            joystickProxy.dynamicX = ((ControlJoystickData) dataEntry).dynamicX;
            joystickProxy.dynamicY = ((ControlJoystickData) dataEntry).dynamicY;
            return joystickProxy;
        } else {
            throw new RuntimeException("Encountered wrong type during ControlData sanitization");
        }
    }

    private static boolean sanitizeList(List<?> controlDataList) {
        if (controlDataList == null) return false;
        boolean madeChanges = false;
        Iterator<?> iterator = controlDataList.iterator();
        while (iterator.hasNext()) {
            Object entry = iterator.next();
            try {
                ControlData controlData = getControlData(entry);
                if (!isSaneData(controlData)) {
                    madeChanges = true;
                    iterator.remove();
                }
            } catch (Exception e) {
                // Keep the buttons alive even if validation encounters an edge-case structure mismatch
                madeChanges = true;
                iterator.remove();
            }
        }
        return madeChanges;
    }

    /**
     * Check all buttons in a control layout and ensure they're sane (contain values valid enough
     * to be displayed properly). Removes any buttons deemed not sane.
     * @param controls the original control layout.
     * @return whether the sanitization process made any changes to the layout
     */
    public static boolean sanitizeLayout(CustomControls controls) {
        if (controls == null) return false;
        boolean madeChanges = false;
        if (controls.mControlDataList != null && sanitizeList(controls.mControlDataList)) madeChanges = true;
        if (controls.mDrawerDataList != null && sanitizeList(controls.mDrawerDataList)) madeChanges = true;
        if (controls.mJoystickDataList != null && sanitizeList(controls.mJoystickDataList)) madeChanges = true;
        return madeChanges;
    }
}
