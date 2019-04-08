package honors.testing.test;

import android.app.Application;

import io.mapwize.mapwizeformapbox.AccountManager;

public class ManualIndoorLocationProviderDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountManager.start(this, "24125f579ae01643755f77870eb983ed");// PASTE YOU MAPWIZE API KEY HERE !!! This is a demo key, giving you access to the demo building. It is not allowed to use it for production. The key might change at any time without notice. Get your key by signin up at mapwize.io
    }

}
