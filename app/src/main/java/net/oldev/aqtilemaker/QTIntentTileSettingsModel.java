package net.oldev.aqtilemaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class QTIntentTileSettingsModel {

    public static class TileSettings {
        private boolean SupportLockscreen = false;
        private CharSequence pkgName;
        private CharSequence className;
        private CharSequence label;

        public TileSettings(CharSequence label, CharSequence pkgName, CharSequence className) {
            this.label = label;
            this.pkgName = pkgName;
            this.className = className;
        }

        public boolean isSupportLockscreen() {
            return SupportLockscreen;
        }

        public CharSequence getPkgName() {
            return pkgName;
        }

        public CharSequence getClassName() {
            return className;
        }

        public CharSequence getLabel() {
            return label;
        }

    }

    private static final String PREFERENCES_KEY_PREFIX = "net.oldev.aqtilemaker.";

    // keys to a tile
    public static final String PREFERENCES_KEY_TILE1 = PREFERENCES_KEY_PREFIX + "tile1";

    // for each tile's settings
    private static final String PREFS_T_LABEL = "label";
    private static final String PREFS_T_PKGNAME = "pkgName";
    private static final String PREFS_T_CLASSNAME = "className";

    @NonNull
    private final Context mContext; // To get access to SharedPreference instance

    public QTIntentTileSettingsModel(@NonNull Context context) {
        mContext = context;
    }

    private @NonNull SharedPreferences getPrefs(@NonNull String tileKey) {
        SharedPreferences prefs =
                mContext.getSharedPreferences(tileKey,
                                              Context.MODE_PRIVATE);
        return prefs;
    }

    public @NonNull TileSettings getTileSettings(@NonNull String tileKey) {
        SharedPreferences p = getPrefs(tileKey);
        // TODO: remove defaults
        return new TileSettings(p.getString(PREFS_T_LABEL, "quick Keep"),
            p.getString(PREFS_T_PKGNAME, "com.google.android.keep"),
            p.getString(PREFS_T_CLASSNAME, "com.google.android.keep.activities.ShareReceiverActivity"));
    }


    public void setTileSettings(@NonNull String tileKey, @NonNull TileSettings settings) {
        SharedPreferences.Editor editor = getPrefs(tileKey).edit();
        editor.putString(PREFS_T_LABEL, settings.getLabel().toString());
        editor.putString(PREFS_T_PKGNAME, settings.getPkgName().toString());
        editor.putString(PREFS_T_CLASSNAME, settings.getClassName().toString());

        boolean success = editor.commit();
        if (!success) {
            throw new RuntimeException("Unexpected failure in committing tile settings");
        }
    }

}
