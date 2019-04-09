package honors.testing.test;
/**
 * Daryl McAllister
 * S1222204
 * Indoor Positioning System
 * Honors Project
 * Iain Lambie
 */
import android.app.Application;

import io.mapwize.mapwizeformapbox.AccountManager;

/**
 * Class used by the Mapwize framework
 * https://www.mapwize.io/
 */
public class ManualIndoorLocationProviderDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountManager.start(this, "24125f579ae01643755f77870eb983ed");
    }

}
