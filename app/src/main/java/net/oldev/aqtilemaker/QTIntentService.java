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
            if (!mSettings.isEmpty()) {
                intent.setClassName(mSettings.getPkgName().toString(), mSettings.getClassName().toString());
            } else {
                // launch the UI to specify it.

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
    }

}
