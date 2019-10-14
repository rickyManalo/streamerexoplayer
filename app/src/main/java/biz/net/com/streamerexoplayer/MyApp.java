package biz.net.com.streamerexoplayer;

import android.app.Application;
import android.content.pm.ApplicationInfo;

public class MyApp extends Application {

    //returns app name
    public String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }
}
