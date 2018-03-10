package net.oldev.aqtilemaker;

import android.support.annotation.NonNull;

public class QTIntentService1 extends QTIntentService {
    protected @NonNull String getSettingsKey() {
        return QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1;
    }
}
