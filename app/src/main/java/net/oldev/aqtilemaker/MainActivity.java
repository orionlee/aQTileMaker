package net.oldev.aqtilemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import static net.oldev.aqtilemaker.QTIntentTileSettingsModel.TileKeys;

public class MainActivity extends Activity {

    private final Map<String, Integer> mTileKeyToViewId = new HashMap();

    private QTIntentServiceManager mQTIntentServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // backend initialization
        mQTIntentServiceManager = QTIntentServiceManager.createAndInit(this);

        // UI initialization

        setContentView(R.layout.activity_main);

        initTileLabelView(R.id.tile1Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1);
        initTileLabelView(R.id.tile2Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE2);
        initTileLabelView(R.id.tile3Label, QTIntentTileSettingsModel.PREFERENCES_KEY_TILE3);

        Log.d("QTIA", "intent: " + getIntent().toString() + " ; tileKey(if any): " + getIntent().getStringExtra("tileKey"));

        // Launch the settings of the tile, if specified in the intent
        final Intent intent = getIntent();
        if (TileService.ACTION_QS_TILE_PREFERENCES.equals(intent.getAction())) {
            @TileKeys String tileKey = intent.getStringExtra("tileKey");
            if (tileKey != null) {
                int viewId = mTileKeyToViewId.get(tileKey);
                final TextView tileLabel = (TextView)findViewById(viewId);
                tileLabel.performClick();
            }
        }
    }

    private void initTileLabelView(@IdRes int id, @NonNull @TileKeys String tileKey) {
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
        private final @NonNull @TileKeys
        String tileKey;

        public TileSettingsOnClickListener(@NonNull @TileKeys String tileKey) {
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
                mQTIntentServiceManager.updateTile(tileKey, settings);

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
                // c. change UI flow and follow Nougat Quick Settings app (it.simonesestito.ntiles),
                //    so that the on activity screen the user
                //    i) enables / disables the tile only, and he/she
                //    ii) has to add the (yet-to-defined) tile, then defines it by clicking the tile.

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
    private @NonNull QTIntentTileSettingsModel.TileSettings saveToModel(@NonNull @TileKeys String tileKey,
                                                                        @NonNull View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings =
                new QTIntentTileSettingsModel.TileSettings(getViewTextById(dataView, R.id.labelInput),
                                                           getViewTextById(dataView, R.id.pkgNameInput),
                                                           getViewTextById(dataView, R.id.classNameInput));

        model.setTileSettings(tileKey, settings);

        return settings;
    }

    private void loadFromModel(@NonNull @TileKeys String tileKey, View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings = model.getTileSettings(tileKey);

        setViewTextById(dataView, R.id.labelInput, settings.getLabel());
        setViewTextById(dataView, R.id.pkgNameInput, settings.getPkgName());
        setViewTextById(dataView, R.id.classNameInput, settings.getClassName());
    }
}