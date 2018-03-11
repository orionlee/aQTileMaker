package net.oldev.aqtilemaker;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainActivity extends AppCompatActivity {

    private final Map<String, Integer> mTileKeyToViewId = new HashMap();

    static class QTIntentServiceManager {

        // Initialized by an immediately-invoked function expression Java approximate
        // Supplier is an @FunctionalInterface that returns an object of specified type
        private static final Map<String, String> msTileKey2ClassName = ((Supplier<Map<String, String>>)() -> {
            Map<String, String> map = new HashMap();
            map.put(QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1, QTIntentService1.class.getName());
            map.put(QTIntentTileSettingsModel.PREFERENCES_KEY_TILE2, QTIntentService2.class.getName());
            map.put(QTIntentTileSettingsModel.PREFERENCES_KEY_TILE3, QTIntentService3.class.getName());

            return Collections.unmodifiableMap(map);
        }).get();

        private static final Collection<String> msTileKeys =
                Collections.unmodifiableCollection(msTileKey2ClassName.keySet());


        private final @NonNull Context mCtx;

        public QTIntentServiceManager(Context ctx) {
            mCtx = ctx;
        }
        
        public void setTileServiceEnabledSetting(@NonNull String tileKey,
                                                 @NonNull QTIntentTileSettingsModel.TileSettings settings) {
            boolean enabled = !settings.isEmpty();
            setTileServiceEnabledSetting(tileKey, enabled);
        }

        private void setTileServiceEnabledSetting(@NonNull String tileKey, boolean enabled) {
            ComponentName cmpName = new ComponentName(mCtx.getPackageName(),
                                                      msTileKey2ClassName.get(tileKey));
            int newState = enabled ?
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            mCtx.getPackageManager().setComponentEnabledSetting(cmpName, newState, 0);
        }

        public void initAllTileServices(QTIntentTileSettingsModel model) {
            msTileKeys.forEach( (tileKey) -> {
                QTIntentTileSettingsModel.TileSettings tileSettings = model.getTileSettings(tileKey);
                setTileServiceEnabledSetting(tileKey, model.getTileSettings(tileKey));
            });
        }
    }

    // PENDING Put it in Activity for the time being while it is more
    // Specifically, #initAllTileServices() also needs to be done at boot time in addition to here
    private QTIntentServiceManager appInit() {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());

        QTIntentServiceManager mgr = new QTIntentServiceManager(getApplicationContext());
        mgr.initAllTileServices(model);
        return mgr;
    }

    private QTIntentServiceManager mQTIntentServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // backend initialization
        mQTIntentServiceManager = appInit();

        // UI initialization

        setContentView(R.layout.activity_main);

        initTileLabelView(R.id.tile1Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1);
        initTileLabelView(R.id.tile2Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE2);
        initTileLabelView(R.id.tile3Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE3);

        Log.d("QTIA", "intent: " + getIntent().toString() + " ; tileKey(if any): " + getIntent().getStringExtra("tileKey"));

        // Launch the settings of the tile, if specified in the intent
        final Intent intent = getIntent();
        if (TileService.ACTION_QS_TILE_PREFERENCES.equals(intent.getAction())) {
            String tileKey = intent.getStringExtra("tileKey");
            if (tileKey != null) {
                int viewId = mTileKeyToViewId.get(tileKey);
                final TextView tileLabel = (TextView)findViewById(viewId);
                tileLabel.performClick();
            }
        }
    }

    private void initTileLabelView(@IdRes int id, @NonNull String tileKey) {
        // UI binding
        final TextView tileLabel = (TextView)findViewById(id);
        tileLabel.setOnClickListener(new TileSettingsOnClickListener(tileKey));

        mTileKeyToViewId.put(tileKey, id);

        // PENDING: data binding
        setTileLabel(tileLabel,
                     new QTIntentTileSettingsModel(getApplicationContext())
                             .getTileSettings(tileKey));
        
    }

    // PENDING: data binding
    private void setTileLabel(@NonNull TextView tileLabel,
                              @NonNull QTIntentTileSettingsModel.TileSettings settings) {
        if (!settings.isEmpty()) {
            tileLabel.setText(settings.getLabel());
            tileLabel.setTextColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
        } else {
            tileLabel.setText("[Tile not set]");
            tileLabel.setTextColor(getResources().getColor(R.color.colorDisabled, getTheme()));
        }
    }

    private class TileSettingsOnClickListener implements OnClickListener {
        private final @NonNull String tileKey;

        public TileSettingsOnClickListener(@NonNull String tileKey) {
            this.tileKey = tileKey;
        }

        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);
            builder.setTitle("Set the application / activity to launch");
            final View dialogView = MainActivity.this.getLayoutInflater().inflate(R.layout.tile_settings, null);
            builder.setView(dialogView);

            loadFromModel(tileKey, dialogView);

            builder.setPositiveButton("OK", (dialog, which) -> {
//                Toast.makeText(getApplicationContext(), "Classname: " +
//                                       ((EditText)dialogView.findViewById(R.id.classNameInput)).getText(),
//                               Toast.LENGTH_SHORT).show();
                QTIntentTileSettingsModel.TileSettings settings = saveToModel(tileKey, dialogView);
                // PENDING: data binding - UI update should rely on listening to changes in underlying SharedPreference
                setTileLabel(((TextView)view), settings);

                // PENDING: data binding - backend service update should also rely on listening to changes in underlying SharedPreference
                mQTIntentServiceManager.setTileServiceEnabledSetting(tileKey, settings);

                // PENDING: data binding - backend service UI state
                // the current logic does not work well for the case a tile is first defined:
                // 1. tile details are entered
                // 2. user opens edit tile panel from the top
                // 3. the tile shown will NOT have the updated label yet.
                //    he/she might be confused.
                // 4. once the tile is added, the tile label will be up-to-date.
                // Possible solutions:
                // a. Active tile might work.
                // b. somehow update <service android:label=""> 's equivalent programmatically

                // PENDING: data binding related -
                // make QTIntentService an active one, so that UI will be more responsive
                // (the default passive mode making tile clicking lagging intermittently),
                // where we can proactively update QsTile with TileService#requestListeningState()

            });

            builder.setNegativeButton("Cancel",(dialog, which) -> {});

            Dialog dialog = builder.show();

        }
    }


    // View helpers
    private static CharSequence getViewTextById(@NonNull View view, @IdRes int id) {
        return ((TextView)view.findViewById(id)).getText();
    }

    private static void setViewTextById(@NonNull View view, @IdRes int id, CharSequence text) {
        ((EditText)view.findViewById(id)).setText(text);
    }

    // Data binding
    private @NonNull QTIntentTileSettingsModel.TileSettings saveToModel(@NonNull String tileKey,
                                                                        @NonNull View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings =
                new QTIntentTileSettingsModel.TileSettings(getViewTextById(dataView, R.id.labelInput),
                                                           getViewTextById(dataView, R.id.pkgNameInput),
                                                           getViewTextById(dataView, R.id.classNameInput));

        model.setTileSettings(tileKey, settings);

        return settings;
    }

    private void loadFromModel(@NonNull String tileKey, View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings = model.getTileSettings(tileKey);

        setViewTextById(dataView, R.id.labelInput, settings.getLabel());
        setViewTextById(dataView, R.id.pkgNameInput, settings.getPkgName());
        setViewTextById(dataView, R.id.classNameInput, settings.getClassName());
    }
}