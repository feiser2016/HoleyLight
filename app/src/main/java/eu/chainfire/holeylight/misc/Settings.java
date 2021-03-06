/*
 * Copyright (C) 2019 Jorrit "Chainfire" Jongma
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package eu.chainfire.holeylight.misc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    public interface OnSettingsChangedListener {
        void onSettingsChanged();
    }

    public static final String ENABLED_MASTER = "enabled_master";
    private static final boolean ENABLED_MASTER_DEFAULT = true;

    public static final String ENABLED_SCREEN_OFF_CHARGING = "enabled_screen_off_charging";
    private static final boolean ENABLED_SCREEN_OFF_CHARGING_DEFAULT = false;

    public static final String ENABLED_SCREEN_OFF_BATTERY = "enabled_screen_off_battery";
    private static final boolean ENABLED_SCREEN_OFF_BATTERY_DEFAULT = false;
    
    private static final String CUTOUT_AREA_LEFT = "cutout_area_left";
    private static final String CUTOUT_AREA_TOP = "cutout_area_top";
    private static final String CUTOUT_AREA_RIGHT = "cutout_area_right";
    private static final String CUTOUT_AREA_BOTTOM = "cutout_area_bottom";
    private static final String DP_ADD_SCALE_BASE = "dp_add_scale_base";
    private static final String DP_ADD_SCALE_HORIZONTAL = "dp_add_scale_horizontal";
    private static final String DP_SHIFT_VERTICAL = "dp_shift_vertical";
    private static final String DP_SHIFT_HORIZONTAL = "dp_shift_horizontal";

    private static Settings instance;
    public static Settings getInstance(Context context) {
        synchronized (Settings.class) {
            if (instance == null) {
                instance = new Settings(context);
            }
            return instance;
        }
    }

    private final List<OnSettingsChangedListener> listeners = new ArrayList<>();
    private final SharedPreferences prefs;
    private volatile SharedPreferences.Editor editor = null;
    private volatile int ref = 0;

    private Settings(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.finalize();
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ref == 0) notifyListeners();
    }

    public synchronized void registerOnSettingsChangedListener(OnSettingsChangedListener onSettingsChangedListener) {
        if (!listeners.contains(onSettingsChangedListener)) {
            listeners.add(onSettingsChangedListener);
        }
    }

    public synchronized void unregisterOnSettingsChangedListener(OnSettingsChangedListener onSettingsChangedListener) {
        listeners.remove(onSettingsChangedListener);
    }

    private synchronized void notifyListeners() {
        for (OnSettingsChangedListener listener : listeners) {
            listener.onSettingsChanged();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public synchronized Settings edit() {
        if (editor == null) {
            editor = prefs.edit();
            ref = 0;
        }
        ref++;
        return this;
    }

    public synchronized void save(boolean immediately) {
        ref--;
        if (ref < 0) ref = 0;
        if (ref == 0) {
            if (immediately) {
                editor.commit();
            } else {
                editor.apply();
            }
            notifyListeners();
            editor = null;
        }
    }

    public Rect getCutoutAreaRect() {
        return new Rect(
            prefs.getInt(CUTOUT_AREA_LEFT, -1),
            prefs.getInt(CUTOUT_AREA_TOP, -1),
            prefs.getInt(CUTOUT_AREA_RIGHT, -1),
            prefs.getInt(CUTOUT_AREA_BOTTOM, -1)
        );
    }

    public Settings setCutoutAreaRect(Rect rect) {
        edit();
        try {
            editor.putInt(CUTOUT_AREA_LEFT, rect.left);
            editor.putInt(CUTOUT_AREA_TOP, rect.top);
            editor.putInt(CUTOUT_AREA_RIGHT, rect.right);
            editor.putInt(CUTOUT_AREA_BOTTOM, rect.bottom);
        } finally {
            save(true);
        }
        return this;
    }

    public int getDpAddScaleBase(int defaultValue) {
        return prefs.getInt(DP_ADD_SCALE_BASE, defaultValue);
    }

    public void setDpAddScaleBase(int value) {
        edit();
        try {
            editor.putInt(DP_ADD_SCALE_BASE, value);
        } finally {
            save(true);
        }
    }

    public int getDpAddScaleHorizontal(int defaultValue) {
        return prefs.getInt(DP_ADD_SCALE_HORIZONTAL, defaultValue);
    }

    public void setDpAddScaleHorizontal(int value) {
        edit();
        try {
            editor.putInt(DP_ADD_SCALE_HORIZONTAL, value);
        } finally {
            save(true);
        }
    }

    public int getDpShiftVertical(int defaultValue) {
        return prefs.getInt(DP_SHIFT_VERTICAL, defaultValue);
    }

    public void setDpShiftVertical(int value) {
        edit();
        try {
            editor.putInt(DP_SHIFT_VERTICAL, value);
        } finally {
            save(true);
        }
    }

    public int getDpShiftHorizontal(int defaultValue) {
        return prefs.getInt(DP_SHIFT_HORIZONTAL, defaultValue);
    }

    public void setDpShiftHorizontal(int value) {
        edit();
        try {
            editor.putInt(DP_SHIFT_HORIZONTAL, value);
        } finally {
            save(true);
        }
    }

    public boolean isEnabled() {
        return prefs.getBoolean(ENABLED_MASTER, ENABLED_MASTER_DEFAULT);
    }

    public void setEnabled(boolean enabled) {
        edit();
        try {
            editor.putBoolean(ENABLED_MASTER, enabled);
        } finally {
            save(true);
        }
    }

    public boolean isEnabledWhileScreenOffCharging() {
        return isEnabled() && prefs.getBoolean(ENABLED_SCREEN_OFF_CHARGING, ENABLED_SCREEN_OFF_CHARGING_DEFAULT);
    }

    public boolean isEnabledWhileScreenOffBattery() {
        return isEnabled() && false;
    }
}
