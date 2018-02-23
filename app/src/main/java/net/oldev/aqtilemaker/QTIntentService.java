package net.oldev.aqtilemaker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.N)
public class QTIntentService extends TileService {

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
        mSettings = model.getTileSettings(QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1);

        int activeState = this.isLocked() && !mSettings.isSupportLockscreen() ?
                Tile.STATE_UNAVAILABLE : Tile.STATE_ACTIVE;
        Tile tile = getQsTile();
        tile.setLabel(mSettings.getLabel());
        tile.setState(activeState);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        if (this.isLocked() && !mSettings.isSupportLockscreen()) {
            Log.d("QTIS", "onClick on lockscreen. NO-OP.");
            return;
        }

        Log.d("QTIS", "onClick started.");
        try {
            Intent intent = new Intent();
            intent.setClassName(mSettings.getPkgName(), mSettings.getClassName());
            startActivityAndCollapse(intent);
        } catch (Throwable t) {
            Log.e("QTIS", "onClick error", t);
        }
    }

}
