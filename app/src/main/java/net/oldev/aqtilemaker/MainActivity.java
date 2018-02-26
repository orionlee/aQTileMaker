package net.oldev.aqtilemaker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tile1 = (TextView)findViewById(R.id.tile1Label);
        tile1.setOnClickListener(new TileSettingsOnClickListener());

        // PENDING: data binding
        tile1.setText(new QTIntentTileSettingsModel(getApplicationContext())
                        .getTileSettings(QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1)
                        .getLabel());
    }

    private class TileSettingsOnClickListener implements OnClickListener {
        public void onClick(View view) {

            final String tileKey = QTIntentTileSettingsModel.PREFERENCES_KEY_TILE1; // PENDING: should not be hardcoded

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);
            builder.setTitle("Set the application / activity to launch");
            final View dialogView = MainActivity.this.getLayoutInflater().inflate(R.layout.tile_settings, null);
            builder.setView(dialogView);

            loadFromModel(tileKey, dialogView);

            builder.setPositiveButton("OK", (dialog, which) -> {
//                Toast.makeText(getApplicationContext(), "Classname: " +
//                                       ((EditText)dialogView.findViewById(R.id.classNameInput)).getText(),
//                               Toast.LENGTH_SHORT).show();
                saveToModel(tileKey, dialogView);
                // PENDING: UI update should rely on listening to changes in underlying SharedPreference
                ((TextView)view).setText(((TextView)dialogView.findViewById(R.id.labelInput)).getText());
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
    private void saveToModel(@NonNull String tileKey, View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings =
                new QTIntentTileSettingsModel.TileSettings(getViewTextById(dataView, R.id.labelInput),
                                                           getViewTextById(dataView, R.id.pkgNameInput),
                                                           getViewTextById(dataView, R.id.classNameInput));

        model.setTileSettings(tileKey, settings);
    }

    private void loadFromModel(@NonNull String tileKey, View dataView) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(getApplicationContext());
        QTIntentTileSettingsModel.TileSettings settings = model.getTileSettings(tileKey);

        setViewTextById(dataView, R.id.labelInput, settings.getLabel());
        setViewTextById(dataView, R.id.pkgNameInput, settings.getPkgName());
        setViewTextById(dataView, R.id.classNameInput, settings.getClassName());
    }

}
