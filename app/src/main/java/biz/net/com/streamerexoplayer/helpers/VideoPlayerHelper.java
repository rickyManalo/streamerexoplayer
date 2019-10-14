package biz.net.com.streamerexoplayer.helpers;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

import biz.net.com.streamerexoplayer.MainActivity;
import biz.net.com.streamerexoplayer.MyApp;
import biz.net.com.streamerexoplayer.R;
import biz.net.com.streamerexoplayer.models.VideoAd;
import biz.net.com.streamerexoplayer.models.VideoPlayer;

import static java.lang.Long.parseLong;

public class VideoPlayerHelper {
    private SimpleExoPlayer exoPlayer;
    private DefaultBandwidthMeter bandwidthMeter;
    private Context ctx;
    private AdaptiveTrackSelection.Factory trackSelectionFactory;
    private VideoPlayer vp;
    private DefaultTrackSelector trackSelector;
    private String appName = "";
    private PlayerView mPlayerView;
    private int toPlayVidIndex = 0;
    private int playedVidCount = 0;
    private ArrayList<VideoAd> vidArrLst;
    private static final int PLAY_DEFAULT_AFTER_COUNT = 7;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private long playbackPosition;
    private boolean playWhenReady;
    private int currentWindow = 0;
    private static FileHelper fileHelper;
    private String TAG = "vidStreamV3";
    private boolean isAsset = false;

    public SimpleExoPlayer getExoPlayer(){
        if(exoPlayer == null){
            exoPlayer = ExoPlayerFactory.newSimpleInstance(ctx, new DefaultTrackSelector((TrackSelection.Factory) null), new DefaultLoadControl());
        }
        return exoPlayer;
    }

    public VideoPlayerHelper(Context context, PlayerView playerView){
        ctx = context;
        appName = ((MyApp) ctx.getApplicationContext()).getApplicationName();
        vidArrLst = new ArrayList<>();
        mPlayerView = playerView;

        fileHelper = new FileHelper(ctx);
        initializePlayer();
    }

    //init player
    public void initializePlayer() {
        if (exoPlayer == null) {
            bandwidthMeter = new DefaultBandwidthMeter();
            trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();

            exoPlayer = ExoPlayerFactory.newSimpleInstance(ctx, trackSelector, loadControl);
            mPlayerView.setPlayer(exoPlayer);
            mPlayerView.setShutterBackgroundColor(Color.TRANSPARENT);

            exoPlayer.addListener(exoPlayEventListener);

            vp = new VideoPlayer();
            Log.d(TAG, "initPlayer");
        }
    }

    //handles video play sequence
    public void playLoopVideo() {
        releasePlayer();
        if(exoPlayer == null){
            initializePlayer();
        }

        VideoAd vidToPlay = new VideoAd();

        if(toPlayVidIndex >= vidArrLst.size()){
            toPlayVidIndex = 0;
        }

        ((MainActivity) ctx).setCreateVidLstLocked();
        if(playedVidCount == PLAY_DEFAULT_AFTER_COUNT){
            vidToPlay = ((MainActivity) ctx).defaultVidAd;
            playedVidCount = 0;
            if(toPlayVidIndex >= 1){
                toPlayVidIndex--;
            }
        }else{
            vidToPlay = vidArrLst.get(toPlayVidIndex);
        }

        String vidToPlayLocalPath = ((MainActivity) ctx).dbHelper.getVideoAdLocalPath(vidToPlay);
        vidToPlay.setLocalPath(vidToPlayLocalPath);

        ((MainActivity) ctx).addLog("playedVidCount: "+playedVidCount+" toPlayIndex: "+toPlayVidIndex);
        if(vidToPlayLocalPath != null && !vidToPlayLocalPath.equals("")){
            startPlayFromFile(vidToPlay);
        }else{
            startStream(vidToPlay);
        }

        playedVidCount++;
        toPlayVidIndex++;
        ((MainActivity) ctx).setCreateVidLstUnlocked();
    }

    //handles video streaming
    public void startStream(VideoAd vAd){
        if(vAd.getUrl().isEmpty()){
            ((MainActivity) ctx).addLog("stream url empty, play next video");
            playLoopVideo();
            return;
        }

        String streamLink = vAd.getUrl();
        vp.setCurrentVideoAd(vAd);
        vp.setPlayMode(VideoPlayer.STREAM);

        if(((MainActivity) ctx).indicatorVisible){
            ((MainActivity) ctx).vIndicator.setVisibility(View.VISIBLE);
            ((MainActivity)ctx).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ((MainActivity) ctx).vIndicator.setBackgroundColor(ctx.getResources().getColor(R.color.light_blue_500));
                }
            });

        }else{
            ((MainActivity) ctx).vIndicator.setVisibility(View.GONE);
        }

        ((MainActivity) ctx).addLog("dlId: "+vAd.getDownloadId()+" localPath: "+vAd.getLocalPath()+" isDownloading: "+((MainActivity) ctx).isDownloading(ctx, vAd.getDownloadId()));
        if(vAd.getDownloadId() >= 0 && (vAd.getLocalPath() == null || vAd.getLocalPath().isEmpty()) && !((MainActivity) ctx).isDownloading(ctx, vAd.getDownloadId())){
            ((MainActivity) ctx).addLog("Missing local file, downloading again");
            ((MainActivity) ctx).startDownload(vAd);
        }

        ((MainActivity) ctx).addLog("stream vid name: "+vAd.getName());
        ((MainActivity) ctx).addLog("PLAYING STREAM "+streamLink);
        Uri mediaUri = Uri.parse(streamLink);

        defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(ctx,
                Util.getUserAgent(ctx,
                        appName), defaultBandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(extractorsFactory)
                .createMediaSource(mediaUri);
        //MediaSource mediaSource = new ExtractorMediaSource(mediaUri, dataSourceFactory, extractorsFactory, null, null);

        /*if(exoPlayer == null){
            initializePlayer();
        }*/

        exoPlayer.setPlayWhenReady(true);
        exoPlayer.prepare(mediaSource, true, false);
    }

    //handles video playing from file
    public void startPlayFromFile(VideoAd vAd){
        vp.setCurrentVideoAd(vAd);
        vp.setPlayMode(VideoPlayer.LOCAL_FILE);

        if(((MainActivity) ctx).indicatorVisible){
            ((MainActivity) ctx).vIndicator.setVisibility(View.VISIBLE);
            ((MainActivity) ctx).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ((MainActivity) ctx).vIndicator.setBackgroundColor(ctx.getResources().getColor(R.color.deep_orange_600));
                }
            });
        }else{
            ((MainActivity) ctx).vIndicator.setVisibility(View.GONE);
        }

        File vAdFile = new File(vAd.getLocalPath());
        Log.d(TAG, "localFile duration: "+getVideoFileLength(vAdFile));
        Uri uri = Uri.fromFile(vAdFile);

        if(uri == null){
            if(!((MainActivity) ctx).isDownloading(ctx, vAd.getDownloadId())){
                ((MainActivity) ctx).addLog("fromFile Missing local file, downloading again");
                ((MainActivity) ctx).startDownload(vAd);
            }
            startStream(vAd);
            return;
        }

        if(vAd.isDefault()){
            ((MainActivity) ctx).addLog("PLAYING DEFAULT LOCAL FILE "+vAd.getLocalPath());
        }else{
            ((MainActivity) ctx).addLog("PLAYING LOCAL FILE "+vAd.getLocalPath());
        }

        defaultBandwidthMeter = new DefaultBandwidthMeter();

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);

            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return fileDataSource;
                }
            };

            MediaSource mSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory, new DefaultExtractorsFactory(), null, null);

            /*if(exoPlayer == null){
                initializePlayer();
            }*/

            exoPlayer.setPlayWhenReady(true);
            exoPlayer.prepare(mSource, false, false);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
            ((MainActivity) ctx).addLog("startPlayFromFile fileDataSourceExcept err: "+e.getLocalizedMessage());
            startStream(vAd);

            if(!((MainActivity) ctx).isDownloading(ctx, vAd.getDownloadId())){
                ((MainActivity) ctx).addLog("fromFile Missing local file, downloading again");
                ((MainActivity) ctx).startDownload(vAd);
            }

        }

    }

    public long getVideoFileLength(File file){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(ctx, Uri.fromFile(file));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        return Long.parseLong(time);
    }

    public void startPlayFromAssets(String assetName){
        /*exoPlayer = ExoPlayerFactory.newSimpleInstance(ctx, new DefaultTrackSelector((TrackSelection.Factory) null), new DefaultLoadControl());
        exoPlayer.addListener(exoPlayEventListener);

        mPlayerView.setPlayer(exoPlayer);
        mPlayerView.setShutterBackgroundColor(Color.TRANSPARENT);

        exoPlayer.addListener(exoPlayEventListener);*/
        isAsset = true;

        DataSpec dataSpec = new DataSpec(Uri.parse("asset:///" + assetName));
        final AssetDataSource assetDataSource = new AssetDataSource(ctx);
        try {
            assetDataSource.open(dataSpec);
            ((MainActivity) ctx).addLog("asset found");
        } catch (AssetDataSource.AssetDataSourceException e) {
            e.printStackTrace();
            ((MainActivity) ctx).addLog("asset src err: " +e.getLocalizedMessage());
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                //return rawResourceDataSource;
                ((MainActivity) ctx).addLog("return asset");
                return assetDataSource;
            }
        };


        MediaSource audioSource = new ExtractorMediaSource(assetDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        ((MainActivity) ctx).addLog("playing asset: "+assetDataSource.getUri().toString());
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.prepare(audioSource);
    }

    //listener for video player. mostly used for video player status logs and error handling
    public final Player.EventListener exoPlayEventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState){
                case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                    ((MainActivity) ctx).addLog("eventListen onPlayerStateChanged DISCONTINUITY_REASON_PERIOD_TRANSITION");
                    //prepareVideo(currentChannelLink);
                    break;
                case Player.STATE_BUFFERING:
                    ((MainActivity) ctx).addLog("Buffering . . . .");
                    break;
                case Player.STATE_READY:
                    ((MainActivity) ctx).addLog("Ready!!!");
                    ((MainActivity) ctx).fullScreen();
                    break;
                case Player.STATE_ENDED:
                    ((MainActivity) ctx).addLog("player ended");

                    if(!isAsset){
                        if(exoPlayer.isCurrentWindowDynamic() || !exoPlayer.isCurrentWindowSeekable()){//if live stream
                            playLoopVideo();
                        }else{
                            playLoopVideo();
                        }
                    }

                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            ((MainActivity) ctx).addLog("ERR: "+error.getLocalizedMessage());
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    //File is faulty or not supported
                    ((MainActivity) ctx).addLog("ERR TYPE_SOURCE: " + error.getSourceException().getMessage());
                    String typeSrcErrMsg = error.getSourceException().getMessage();
                    VideoAd faultyAd = new VideoAd();

                    for(VideoAd vAd : vidArrLst){
                        if(vAd.getLocalPath().equals(vp.getCurrentVideoAd().getLocalPath())){
                            faultyAd = vAd;
                        }
                    }

                    if(typeSrcErrMsg != null){
                        ((MainActivity) ctx).addLog("file is faulty and needs to be re-downloaded");

                        if(typeSrcErrMsg.contains("None of the available extractors")){
                            fileHelper.mDeleteFile(vp.getCurrentVideoAd().getLocalPath());
                            ((MainActivity) ctx).dbHelper.updateLocalPath(faultyAd.getVideoId(), "");

                            ((MainActivity) ctx).startDownload(vp.getCurrentVideoAd());
                        }else if(typeSrcErrMsg.contains("EOFException")){
                            fileHelper.mDeleteFile(vp.getCurrentVideoAd().getLocalPath());
                            ((MainActivity) ctx).dbHelper.updateLocalPath(faultyAd.getVideoId(), "");

                            ((MainActivity) ctx).startDownload(vp.getCurrentVideoAd());
                        }else if(typeSrcErrMsg.contains("Top bit not zero")){
                            fileHelper.mDeleteFile(vp.getCurrentVideoAd().getLocalPath());
                            ((MainActivity) ctx).dbHelper.updateLocalPath(faultyAd.getVideoId(), "");

                            ((MainActivity) ctx).startDownload(vp.getCurrentVideoAd());
                        }else if(typeSrcErrMsg.contains("FileNotFoundException")){
                            ((MainActivity) ctx).addLog("playedVidCount: " + playedVidCount + " toPlayVidIndex: " + toPlayVidIndex);

                            if(((MainActivity) ctx).defaultVidAd.getUrl().isEmpty()){
                                ((MainActivity) ctx).addLog("Default video url might be empty.");
                                //playedVidCount++;
                                //toPlayVidIndex++;
                            }

                        }
                    }else{
                        ((MainActivity) ctx).addLog("file is broken and needs to be re-downloaded");
                        fileHelper.mDeleteFile(vp.getCurrentVideoAd().getLocalPath());
                        ((MainActivity) ctx).dbHelper.updateLocalPath(faultyAd.getVideoId(), "");

                        ((MainActivity) ctx).startDownload(vp.getCurrentVideoAd());
                    }

                    playLoopVideo();
                    break;

                case ExoPlaybackException.TYPE_RENDERER:
                    ((MainActivity) ctx).addLog("ERR TYPE_RENDERER: " + error.getRendererException().getMessage());
                    playLoopVideo();
                    break;

                case ExoPlaybackException.TYPE_UNEXPECTED:
                    ((MainActivity) ctx).addLog("ERR TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
                    playLoopVideo();
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                // when window index changes (eg. end of window 0, start of window 1)
                //prepareVideo(currentChannelLink);
                ((MainActivity) ctx).addLog("eventListen onPositionDiscontinuity DISCONTINUITY_REASON_PERIOD_TRANSITION");
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

    public void addVideoAd(VideoAd videoAd){
        if(!videoAd.getUrl().isEmpty()){
            vidArrLst.add(videoAd);
        }
    }

    public int videoAdListSize(){
        return vidArrLst.size();
    }

    public VideoAd getVideoAd(int position){
        return vidArrLst.get(position);
    }

    public ArrayList videoAdList(){
        return vidArrLst;
    }

    public VideoPlayer videoPlayer(){
        return vp;
    }

    //releases video player. for preventing memory leaks
    public void releasePlayer() {
        if (exoPlayer == null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

}
