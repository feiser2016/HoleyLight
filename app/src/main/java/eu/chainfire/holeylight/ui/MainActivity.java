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

package eu.chainfire.holeylight.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import eu.chainfire.holeylight.BuildConfig;
import eu.chainfire.holeylight.R;
import eu.chainfire.holeylight.misc.Settings;

import android.annotation.SuppressLint;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements Settings.OnSettingsChangedListener {
    private Handler handler = null;
    private Settings settings = null;
    private SwitchCompat switchMaster = null;

    private boolean checkPermissionsOnResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        settings = Settings.getInstance(this);
        settings.registerOnSettingsChangedListener(this);

        startActivity(new Intent(this, DetectCutoutActivity.class));
    }

    @Override
    protected void onDestroy() {
        settings.unregisterOnSettingsChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onSettingsChanged() {
        if (switchMaster != null) {
            boolean enabled = settings.isEnabled();
            if (enabled != switchMaster.isChecked()) {
                switchMaster.setChecked(enabled);
            }
        }
    }

    private void checkPermissions() {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            (new AlertDialog.Builder(this))
                    .setTitle(getString(R.string.permission_required) + " 1/4")
                    .setMessage(R.string.permission_overlay)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(intent);
                    })
                    .show();
        } else if (((CompanionDeviceManager)getSystemService(COMPANION_DEVICE_SERVICE)).getAssociations().size() == 0) {
            (new AlertDialog.Builder(this))
                    .setTitle(getString(R.string.permission_required) + " 2/4")
                    .setMessage(R.string.permission_associate)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        CompanionDeviceManager companionDeviceManager = (CompanionDeviceManager)getSystemService(COMPANION_DEVICE_SERVICE);
                        companionDeviceManager.associate((new AssociationRequest.Builder()).build(), new CompanionDeviceManager.Callback() {
                            @Override
                            public void onDeviceFound(IntentSender chooserLauncher) {
                                try {
                                    startIntentSenderForResult(chooserLauncher, 0, null, 0, 0, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override public void onFailure(CharSequence error) { }
                        }, handler);
                    })
                    .show();
        } else if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
            (new AlertDialog.Builder(this))
                    .setTitle(getString(R.string.permission_required) + " 3/4")
                    .setMessage(R.string.permission_notifications)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivity(intent);
                    })
                    .show();
/* //TODO temporarily disabled, we might not actually need this ?
        } else if (!((PowerManager)getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
            (new AlertDialog.Builder(this))
                    .setTitle(getString(R.string.permission_required) + " 4/4")
                    .setMessage(R.string.permission_battery)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        checkPermissionsOnResume = true;
                        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(intent);
                    })
                    .show();
*/
        } else {
            //TODO temp startActivity(new Intent(this, DebugActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissionsOnResume) {
            checkPermissionsOnResume = false;
            checkPermissions();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkPermissions();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        @SuppressLint("InflateParams") View layout = getLayoutInflater().inflate(R.layout.toolbar_switch, null);
        switchMaster = layout.findViewById(R.id.toolbar_switch);
        switchMaster.setChecked(settings.isEnabled());
        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> settings.setEnabled(isChecked));

        MenuItem item = menu.add("");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(layout);
        return true;
    }
}
