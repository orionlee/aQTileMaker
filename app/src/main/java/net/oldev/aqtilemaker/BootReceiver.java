package net.oldev.aqtilemaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // PENDING: to  be consolidated with MainActivity#appInit()
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(context.getApplicationContext());

        MainActivity.QTIntentServiceManager mgr = new MainActivity.QTIntentServiceManager(context.getApplicationContext());
        mgr.initAllTileServices(model);
    }
}
