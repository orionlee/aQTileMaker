package net.oldev.aqtilemaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class QTIntentTileSettingsModel {

    public static class TileSettings {
        private CharSequence pkgName;
        private CharSequence className;
        private CharSequence label;

        public TileSettings(CharSequence label, CharSequence pkgName, CharSequence className) {
            this.label = label;
            this.pkgName = pkgName;
            this.className = className;
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


        public boolean isEmpty() {
            return isEmptyStr(label) && isEmptyStr(className) && isEmptyStr(pkgName);
        }

        private static boolean isEmptyStr(@Nullable CharSequence str) {
            return str == null || str.length() == 0;
        }
    }

    private static final String PREFERENCES_KEY_PREFIX = "net.oldev.aqtilemaker.";

    // keys to a tile
    public static final String PREFERENCES_KEY_TILE1 = PREFERENCES_KEY_PREFIX + "tile1";
    public static final String PREFERENCES_KEY_TILE2 = PREFERENCES_KEY_PREFIX + "tile2";
    public static final String PREFERENCES_KEY_TILE3 = PREFERENCES_KEY_PREFIX + "tile3";

    @StringDef({PREFERENCES_KEY_TILE1, PREFERENCES_KEY_TILE2, PREFERENCES_KEY_TILE3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TileKeys {}

    // for each tile's settings
    private static final String PREFS_T_LABEL = "label";
    private static final String PREFS_T_PKGNAME = "pkgName";
    private static final String PREFS_T_CLASSNAME = "className";

    @NonNull
    private final Context mContext; // To get access to SharedPreference instance

    public QTIntentTileSettingsModel(@NonNull Context context) {
        mContext = context;
    }

    private @NonNull SharedPreferences getPrefs(@NonNull @TileKeys String tileKey) {
        SharedPreferences prefs =
                mContext.getSharedPreferences(tileKey,
                                              Context.MODE_PRIVATE);
        return prefs;
    }

    public @NonNull TileSettings getTileSettings(@NonNull @TileKeys String tileKey) {
        SharedPreferences p = getPrefs(tileKey);
        return new TileSettings(p.getString(PREFS_T_LABEL, ""),
            p.getString(PREFS_T_PKGNAME, ""),
            p.getString(PREFS_T_CLASSNAME, ""));
    }


    public void setTileSettings(@NonNull @TileKeys String tileKey, @NonNull TileSettings settings) {
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
