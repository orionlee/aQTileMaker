package net.oldev.aqtilemaker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tile1 = (TextView)findViewById(R.id.tile1Label);
        tile1.setOnClickListener(new TileSettingsOnClickListener());
    }

    private class TileSettingsOnClickListener implements OnClickListener {
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);
            builder.setTitle("Set the application / activity to launch");
            final View dialogView = MainActivity.this.getLayoutInflater().inflate(R.layout.tile_settings, null);
            builder.setView(dialogView);

            builder.setPositiveButton("OK", (dialog, which) -> {
                Toast.makeText(getApplicationContext(), "Classname: " +
                                       ((EditText)dialogView.findViewById(R.id.classNameInput)).getText(),
                               Toast.LENGTH_SHORT).show();
                // PENDING: UI update should rely on listening to changes in underlying SharedPreference
                ((TextView)view).setText(((EditText)dialogView.findViewById(R.id.labelInput)).getText());
            });

            builder.setNegativeButton("Cancel",(dialog, which) -> {});

            Dialog dialog = builder.show();

        }
    }
}
