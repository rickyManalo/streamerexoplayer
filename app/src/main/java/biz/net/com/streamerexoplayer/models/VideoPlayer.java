package biz.net.com.streamerexoplayer.models;

public class VideoPlayer {
    public static final int STREAM = 1;
    public static final int LOCAL_FILE = 2;

    private int playMode;
    private VideoAd currentVideoAd;

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public VideoAd getCurrentVideoAd() {
        return currentVideoAd;
    }

    public void setCurrentVideoAd(VideoAd currentVideoAd) {
        this.currentVideoAd = currentVideoAd;
    }
}
