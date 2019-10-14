package biz.net.com.streamerexoplayer;

import java.util.EventListener;
import java.util.HashMap;

public interface DownloadListener extends EventListener {
    public void onDownloadFinish(HashMap<String, String> downloadDetails);
}
