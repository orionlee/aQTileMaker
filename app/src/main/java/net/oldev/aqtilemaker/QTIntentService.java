package net.oldev.aqtilemaker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;
import android.util.Log;

import static net.oldev.aqtilemaker.QTIntentTileSettingsModel.TileKeys;

@TargetApi(Build.VERSION_CODES.N)
public abstract class QTIntentService extends TileService {

    protected abstract @NonNull @TileKeys String getSettingsKey();

    private static class QTIntentTileSettings {
        private boolean SupportLockscreen = false;
        private String pkgName;
        private String className;
        private String label;

        public boolean isSupportLockscreen() {
            return SupportLockscreen;
        }

        public String getPkgName() {
            return pkgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    // NonNull after #onStartListening
    private QTIntentTileSettingsModel.TileSettings mSettings = null;
    
    public QTIntentService() {
    }

    @Override
    public void onStartListening() {
        Log.d("QTIS", "onStartListening()...");
        
        // Get tile settings from backend
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        mSettings = model.getTileSettings(getSettingsKey());

        Tile tile = getQsTile();
        tile.setLabel(mSettings.getLabel());
        tile.updateTile();
    }

    @Override
    public void onClick() {
        // lock screen check (to make tile unavailable) removed.
        // Now when a user clicks the tile on lockscreen,
        // he/she will typically be prompted to unlock the screen to see the activity launched
        // Reasons for removing lock screen check:
        // 1. supporting the old lock screen check with active tile will require additional
        //    screen lock/unlock broadcast receivers to update the UI state, complicating the codes.
        // 2. the new flow (prompt user to unlock screen) is also arguably more natural.

        Log.d("QTIS", "onClick started.");

        // encapsulate the main logic in a runnable
        // to support lock screen case
        Runnable doOnClick = () -> {
            try {
                Intent intent = new Intent();
                if (!mSettings.isEmpty()) {
                    intent.setClassName(mSettings.getPkgName().toString(), mSettings.getClassName().toString());
                } else {
                    // launch the Preference UI to define the tile

                    // Somehow this does not work
                    ///intent.setPackage(getApplicationContext().getPackageName()).setAction(TileService.ACTION_QS_TILE_PREFERENCES);
                    ///intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

                    // Workaround: specify the activity explicitly
                    intent.setClassName(getApplicationContext(), MainActivity.class.getName());
                    intent.setAction(TileService.ACTION_QS_TILE_PREFERENCES);

                    // signify it is from this specific tile to the activity;
                    intent.putExtra("tileKey", getSettingsKey());

                }

                startActivityAndCollapse(intent);

            } catch (Throwable t) {
                Log.e("QTIS", "onClick error", t);
            }
        };

        // trigger the main logic
        if (!isLocked()) {
            doOnClick.run();
        } else {
            unlockAndRun(doOnClick);
        }
    }

}
