package net.oldev.aqtilemaker;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The manager knows all QTIntentServices, and provides methods to initialize / update them.
 */
public class QTIntentServiceManager {

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

    /**
     * Create an instance of the manager and initializes all managed tile services
     */
    public static @NonNull QTIntentServiceManager createAndInit(@NonNull Context ctx) {
        QTIntentTileSettingsModel model = new QTIntentTileSettingsModel(ctx.getApplicationContext());

        QTIntentServiceManager mgr = new QTIntentServiceManager(ctx.getApplicationContext());
        mgr.initAllTileServices(model);
        return mgr;
    }

    public QTIntentServiceManager(@NonNull Context ctx) {
        mCtx = ctx;
    }

    public void setTileServiceEnabledSetting(@NonNull @QTIntentTileSettingsModel.TileKeys String tileKey,
                                             @NonNull QTIntentTileSettingsModel.TileSettings settings) {
        boolean enabled = !settings.isEmpty();
        setTileServiceEnabledSetting(tileKey, enabled);
    }

    private void setTileServiceEnabledSetting(@NonNull @QTIntentTileSettingsModel.TileKeys String tileKey, boolean enabled) {
        ComponentName cmpName = toComponentName(tileKey);
        int newState = enabled ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        mCtx.getPackageManager().setComponentEnabledSetting(cmpName, newState, 0);
    }

    private ComponentName toComponentName(@NonNull @QTIntentTileSettingsModel.TileKeys String tileKey) {
        return new ComponentName(mCtx.getPackageName(),
                                 msTileKey2ClassName.get(tileKey));
    }

    public void triggerTileUIUpdate(@NonNull @QTIntentTileSettingsModel.TileKeys String tileKey) {
        ComponentName cmpName = toComponentName(tileKey);
        TileService.requestListeningState(mCtx.getApplicationContext(), cmpName);
    }

    public void initAllTileServices(QTIntentTileSettingsModel model) {
        msTileKeys.forEach( (tileKey) -> {
            QTIntentTileSettingsModel.TileSettings tileSettings = model.getTileSettings(tileKey);
            setTileServiceEnabledSetting(tileKey, model.getTileSettings(tileKey));
            triggerTileUIUpdate(tileKey);
        });
    }

}
