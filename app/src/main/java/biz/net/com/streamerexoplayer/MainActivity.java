package biz.net.com.streamerexoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;

import biz.net.com.streamerexoplayer.helpers.DbHelper;
import biz.net.com.streamerexoplayer.helpers.DownloadHelper;
import biz.net.com.streamerexoplayer.helpers.FileHelper;
import biz.net.com.streamerexoplayer.helpers.VideoPlayerHelper;
import biz.net.com.streamerexoplayer.models.DownloadItems;
import biz.net.com.streamerexoplayer.models.VideoAd;
import biz.net.com.streamerexoplayer.models.VideoPlayer;

public class MainActivity extends AppCompatActivity{
    private static final String SHARE = "share";
    private static final String CAFE = "cafe";
    private PlayerView playerView;
    private ConstraintLayout lytMain;
    private RelativeLayout lytBottom;
    private TextView lblMsgAds;
    private TextView lblLogs;
    private ScrollView scrLogs;
    public View vIndicator;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static{
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }
    private static final String TAG = "vidStreamV3";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String serverURL = "";
    private static final String SERVER_URL = "server_url";
    private static final String SHARE_VIDEO_ADS = "share_video_ads";
    private final Context ctx = this;
    private String defaultUrl = "";

    private RequestQueue queue;

    private boolean playWhenReady = true;

    //private DownloadManager dlMgr;
    private long lastDownload = -1L;
    public DbHelper dbHelper;
    private AlertDialog dlgSvrAdd;

    private final boolean useDummyData = false;
    public VideoAd defaultVidAd = new VideoAd();

    public boolean indicatorVisible = false;
    private static final String SHOW_INDICATOR = "show_indicator";
    private int STORAGE_PERMISSION_REQUEST_CODE = 425188;
    private static final int REQUEST_READ_STORAGE_STATE = 1;
    private String videoTest = "{\n" +
            "  \"default\": {\n" +
            "    \"url\": \"http://192.168.151.33:1228/media/files/video_Url_D1WHWQu0We/quick%20tutorial.mp4\",\n" +
            "    \"id\": 0,\n" +
            "    \"version\": 0\n" +
            "  },\n" +
            "  \"videos\": [\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_zJq8aaeQOY/Sweet%20and%20Sour%20Fish_1.mp4\",\n" +
            "      \"id\": 3,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_hdhlklPdcb/Sweet%20and%20Sour%20Meat%20Ball_1.mp4\",\n" +
            "      \"id\": 4,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_FB2TgLB3yj/Beef%20Kaldereta.mp4\",\n" +
            "      \"id\": 7,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_F5BPFB87vn/Share%20Pizza.mp4\",\n" +
            "      \"id\": 9,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_AlmdUINntY/Bicol%20Express.mp4\",\n" +
            "      \"id\": 10,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_jOFniU0tbD/Chicken%20Kaldereta.mp4\",\n" +
            "      \"id\": 11,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_hRqqOY8i0D/Chinese%20Beef%20and%20Mushroom.mp4\",\n" +
            "      \"id\": 12,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_ommRlHVuiz/Pork%20Steak.mp4\",\n" +
            "      \"id\": 13,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_NPosD89JBw/Share%20Pasta.mp4\",\n" +
            "      \"id\": 14,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_4JUE-XfnKo/Share%20Sisig.mp4\",\n" +
            "      \"id\": 15,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_Lg2ugw1LNs/Sweet%20and%20Sour%20Fish.mp4\",\n" +
            "      \"id\": 16,\n" +
            "      \"version\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_oYspJy4Po_/Share%20Cafe%20Brownie.mp4\",\n" +
            "      \"id\": 18,\n" +
            "      \"version\": 2\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_qGK2HIcdwD/Share%20Churros.mp4\",\n" +
            "      \"id\": 19,\n" +
            "      \"version\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://192.168.151.33:1228/media/files/video_Url_ek-5vKWfOk/Pork%20Menudo.mp4\",\n" +
            "      \"id\": 20,\n" +
            "      \"version\": 1\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private String messageTest = "{\n" +
            "  \"messages\": [\n" +
            "    {\n" +
            "      \"Text\": \"Welcome to Share Cafe!\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"Text\": \"Enjoy your meal!\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"Text\": \"Pre Opening\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    public final String DOWNLOAD_FILE_PATH = "download_file_path";
    public final String VIDEO_ID = "video_id";
    private ArrayList<DownloadItems> dlItemsArrLst;
    private ConnectivityManager connMgr;
    private Handler adsHandler, tempLockHandler;
    private String vidGroup = "";
    private static final String VIDEO_GROUP = "video_group";
    private final int FIVE_MINUTE = 300000;
    public String appName = "";
    private VideoPlayerHelper vidPlayHandler;
    private FileHelper fileHelper;
    private boolean createVidLstLocked = false;
    private DownloadHelper dlHelper;
    private int downloadId = 3;
    private String DOWNLOAD_ID = "download_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("share_video_ads_v3", MODE_PRIVATE);
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        adsHandler = new Handler();
        tempLockHandler = new Handler();
        dlHelper = new DownloadHelper(ctx);

        /*String sampleVideoList = videoUpdateTest;
        queue = Volley.newRequestQueue(this, new BaseHttpStack() {
            @Override
            public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) {
                SystemClock.sleep(1000L);
                switch (request.getUrl()){
                    case "http://192.168.151.33:1228/video/":
                        return new HttpResponse(200, new ArrayList<>(),
                                sampleVideoList.length(), new ByteArrayInputStream(baileyAffogatoTest2.getBytes()));
                    case "http://192.168.151.33:1228/message/":
                        return new HttpResponse(200, new ArrayList<>(),
                                messageTest.length(), new ByteArrayInputStream(messageTest.getBytes()));
                    default:
                        return new HttpResponse(400, new ArrayList<>());
                }
            }
        });*/

        playerView = findViewById(R.id.exoPlayerView);
        lytMain = findViewById(R.id.lytMain);
        lytBottom = findViewById(R.id.lytBottom);
        lblMsgAds = findViewById(R.id.lblMsgAds);
        vIndicator = findViewById(R.id.vIndicatior);
        lblLogs = findViewById(R.id.lblLogs);
        scrLogs = findViewById(R.id.scrLogs);

        vidPlayHandler = new VideoPlayerHelper(ctx, playerView);
        appName = ((MyApp) getApplicationContext()).getApplicationName();
        queue = Volley.newRequestQueue(this);
        dlItemsArrLst = new ArrayList<>();
        fileHelper = new FileHelper(ctx);

        if(indicatorVisible){
            vIndicator.setVisibility(View.VISIBLE);
        }else{
            vIndicator.setVisibility(View.GONE);
        }

        fullScreen();

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER){
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        dbHelper = new DbHelper(getApplicationContext());
        try {
            dbHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dlHelper.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadFinish(HashMap<String, String> downloadDetails) {
                String dlFilePath = downloadDetails.get("path");
                int dlId = Integer.parseInt(downloadDetails.get("id"));
                addLog("dlFinished: " + dlFilePath+" dlId: "+dlId);

                //TODO: save local path of video to db, add localpath to videoPlayArraylist

                /*VideoAd va = vidPlayHandler.getVideoAd(x);
                saveVideoAd();*/

                for(int x = 0; x < vidPlayHandler.videoAdListSize(); x++){
                    VideoAd va = vidPlayHandler.getVideoAd(x);
                    if(va.getDownloadId() == dlId && !dlFilePath.equals("")){

                        DownloadItems di = dlItemsArrLst.get(x);
                        di.setDownloadComplete(true);

                        saveVideoAd(va, dlFilePath, Integer.parseInt(downloadDetails.get("id")));
                    }else if(defaultVidAd.getDownloadId() == dlId && !dlFilePath.equals("")){

                        DownloadItems di = dlItemsArrLst.get(x);
                        di.setDownloadComplete(true);

                        saveVideoAd(defaultVidAd, dlFilePath, Integer.parseInt(downloadDetails.get("id")));
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        addLog("device ip: "+ip);

        downloadId = prefs.getInt(DOWNLOAD_ID, 3);
        downloadId = checkDownloadId(downloadId);

        initDownloader();
        indicatorVisible = prefs.getBoolean(SHOW_INDICATOR, false);
        scrLogs.setVisibility(View.GONE);

        vidGroup = prefs.getString(VIDEO_GROUP, "");

        if (isServerIpExist()){
            playerView.setVisibility(View.VISIBLE);

            if(dlgSvrAdd != null && dlgSvrAdd.isShowing()){
                dlgSvrAdd.dismiss();
            }

            if(useDummyData){
                populateVidArrLst();
                vidPlayHandler.playLoopVideo();
            }else {
                if(hasInternetConnection()){
                    getAdvertisements(serverURL, vidGroup);
                }else{
                    registerReceiver(connReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                }
            }

            showIndicators(indicatorVisible);
        }else{
            showIndicators(indicatorVisible);
            
            if(dlgSvrAdd == null){
                showServerAddressInputDlg();
            }
        }
    }

    private void showServerAddressInputDlg(){
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg_server_address, null);

        final EditText txtServerAddress = v.findViewById(R.id.txtDlgServerAddress);
        Button btnConnect = v.findViewById(R.id.btnDlgServerAddressConnect);
        Switch swiShowIndicator = v.findViewById(R.id.swiShowIndicator);

        AlertDialog.Builder dlgBuild = new AlertDialog.Builder(ctx);
        dlgBuild.setCancelable(false);
        dlgBuild.setView(v);

        dlgSvrAdd = dlgBuild.create();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtSvrAddInput = txtServerAddress.getText().toString().trim();

                if(txtSvrAddInput.isEmpty()){
                    return;
                }

                indicatorVisible = swiShowIndicator.isChecked();
                editor = prefs.edit();
                editor.putString(SERVER_URL, txtSvrAddInput);
                editor.putBoolean(SHOW_INDICATOR, indicatorVisible);
                editor.apply();
                addLog("serverUrl: "+txtSvrAddInput);

                serverURL = txtSvrAddInput;

                showIndicators(indicatorVisible);

                getAdvertisements(txtSvrAddInput, "");
                dlgSvrAdd.dismiss();

                /*if(vidGroup.isEmpty()){
                    showVideoGroupPicker(txtSvrAddInput);
                }*/
                //TODO: apply new changes for grouped videos
            }
        });

        dlgSvrAdd.show();

    }

    private void showVideoGroupPicker(final String serverURL){
        View v = getLayoutInflater().inflate(R.layout.dlg_video_group_picker, null);

        Button btnShare = v.findViewById(R.id.btnVidGrpPickerShare);
        Button btnCafe = v.findViewById(R.id.btnVidGrpPickerCafe);
        TextView lblTitle = v.findViewById(R.id.lblVidGrpPickerTitle);

        AlertDialog dlgBuild = new AlertDialog.Builder(ctx).create();
        dlgBuild.setCancelable(false);
        dlgBuild.setView(v);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vidGroup = SHARE;
                getAdvertisements(serverURL, vidGroup);

                dlgBuild.dismiss();
            }
        });

        btnCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vidGroup = CAFE;
                getAdvertisements(serverURL, vidGroup);
                dlgBuild.dismiss();
            }
        });

        editor.putString(VIDEO_GROUP, vidGroup);
        editor.commit();

       dlgBuild.show();
    }

    //shows or hides the corner box and logs
    private void showIndicators(boolean indicatorVisible) {
        if(indicatorVisible){
            vIndicator.setVisibility(View.VISIBLE);
            lblLogs.setVisibility(View.VISIBLE);
            scrLogs.setVisibility(View.VISIBLE);
        }else{
            vIndicator.setVisibility(View.GONE);
            lblLogs.setVisibility(View.GONE);
            scrLogs.setVisibility(View.GONE);
        }
    }

    //calls video and message api
    private void getAdvertisements(final String serverURL, String vidGrp){
        addLog("getAds");
        getVideoAds(serverURL, vidGrp, true);
        getMessageAds(serverURL, vidGrp);

        Handler dbVidDeleteHandler = new Handler();
        dbVidDeleteHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doDbVideoDeletions();
                //doOtherVideoDeletions();
            }
        }, 5000);

        adsHandler.postDelayed(msgRunnable, FIVE_MINUTE);
        adsHandler.postDelayed(videoUpdateRunnable, FIVE_MINUTE);
    }

    private final Runnable msgRunnable = new Runnable() {
        @Override
        public void run() {
            addLog("Updating ads marquee");
            getMessageAds(serverURL, vidGroup);

            adsHandler.postDelayed(this, FIVE_MINUTE);
        }
    };

    //code for updating videoAd after some time
    private final Runnable videoUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            addLog("Updating ads video");
            getVideoAds(serverURL, vidGroup, false);

            adsHandler.postDelayed(this, FIVE_MINUTE);
        }
    };

    private void doOtherVideoDeletions() {
        //This method deletes every video that is not linked to any record on the database
        addLog("other video deletions start");
        ArrayList<VideoAd> dbVidAdLst = dbHelper.getAllVideoAds();
        ArrayList<String> vidPathArrLst = getVidLstInDownloads();
        boolean isSamePathFound = false;

        for(String path : vidPathArrLst){
            addLog("dl path: "+path);
            for(int x = 0; x < dbVidAdLst.size(); x++){
                VideoAd vidAd = dbVidAdLst.get(x);

                if(path.equals(vidAd.getLocalPath())){
                    isSamePathFound = true;
                }else{
                    isSamePathFound = false;
                }

                if(isSamePathFound){
                    addLog("same path found");
                    break;
                }else if(!isSamePathFound && x == (dbVidAdLst.size() - 1)){
                    addLog("delete "+path);
                    fileHelper.mDeleteFile(path);
                }
            }
        }
    }

    //for getting everything inside Downloads folder. used for deleting everything inside Downloads folder
    // and only keeping the ones that are linked inside the database
    private ArrayList<String> getVidLstInDownloads() {
        ArrayList<String> arrLst = new ArrayList<>();

        String selection= MediaStore.Video.Media.DATA +" like?";
        String[] parameters = { MediaStore.Images.Media.DATA };
        String[] selectionArgs=new String[]{"%"+Environment.DIRECTORY_DOWNLOADS+"%"};
        Cursor videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                parameters, selection, selectionArgs, MediaStore.Video.Media.DATE_TAKEN + " DESC");

        if(videoCursor.moveToFirst()){
            do{
                arrLst.add(videoCursor.getString(0));
            }while (videoCursor.moveToNext());
        }

        videoCursor.close();
        return arrLst;
    }

    //deletes videos that are not present on the video list api call and are still on the database
    private void doDbVideoDeletions(){
        addLog("video deletions start");
        ArrayList<VideoAd> dbVidAdLst = dbHelper.getAllVideoAds();
        boolean isSameVidFound = false;

        lab1: for(VideoAd dbVidAd : dbVidAdLst){
            isSameVidFound = false;
            for(int x = 0; x < vidPlayHandler.videoAdListSize(); x++){
                VideoAd vidAd = vidPlayHandler.getVideoAd(x);

                if(dbVidAd.getVideoId() == vidAd.getVideoId()){
                    isSameVidFound = true;
                }else if(dbVidAd.getVideoId() == 0){//if video ad is a default
                    break;
                }else{
                    isSameVidFound = false;
                }

                if(isSameVidFound){
                    break;
                }else if(!isSameVidFound && x == (vidPlayHandler.videoAdListSize() - 1)){
                    addLog("delete "+dbVidAd.getName());
                    deleteVidAd(dbVidAd);
                }
            }
        }
        //TODO: change how we track double downloads?
    }

    //deletes files. used with doDbVideoDeletions()
    private void deleteVidAd(VideoAd vidAd) {
        String videoPath = vidAd.getLocalPath();
        dbHelper.deleteVideoAd(vidAd);

        File file = new File(videoPath);
        boolean isDeleted1 = file.delete();

        if(isDeleted1){
            addLog("deleted 1");
        }
        if(file.exists()){
            try {
                boolean isDeleted2 = file.getCanonicalFile().delete();

                if(isDeleted2){
                    addLog("deleted 2");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(file.exists()){
                boolean isDeleted3 = getApplicationContext().deleteFile(file.getName());

                if(isDeleted3){
                    addLog("deleted 3");
                }
            }
        }
    }

    private int checkDownloadId(int downloadId){
        if(downloadId == 2147483647){
            return 0;
        }else {
            return downloadId;
        }
    }

    //calls api for messages
    private void getMessageAds(String serverURL, String vidGrp){
        addLog("getMsgAds");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,"http://"+serverURL+"/message/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                addLog("getMsgAds res: "+response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    JSONArray messageArray = jsonObj.getJSONArray("messages");
                    String messages = "";
                    for (int i=0; i<messageArray.length(); i++){
                        JSONObject messageObj= messageArray.getJSONObject(i);
                        messages += messageObj.getString("Text");

                        if(i != messageArray.length()){
                            messages += "     |     ";
                        }
                    }

                    addLog("receivedMsg: "+messages+"|");

                    appendTxtToMarquee(lblMsgAds, messages);

                    if(messages.length() > 0){
                        lytBottom.setVisibility(View.VISIBLE);
                    }else {
                        lytBottom.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    addLog("getMsgAds res err: " + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                addLog("getMsgAds err: " + error.getMessage());
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    //calls api for video
    public void getVideoAds(String serverURL, String vidGrp, final boolean playVideosOnSuccess){
        addLog("getVidAds");
        String url = "http://"+serverURL+"/video/";

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                addLog("getVidAds res: "+response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    JSONObject defaultObj = jsonObj.getJSONObject("default");
                    JSONArray videoObj = jsonObj.getJSONArray("videos");

                    defaultUrl = defaultObj.getString("url");

                    if(!createVidLstLocked){
                        createVideoLstFromApi(defaultObj, videoObj, playVideosOnSuccess);
                    }else {
                        addLog("Video list creation delayed to prevent errors.");
                        tempLockHandler.postDelayed(new unlockCreateVidLstRunnable(defaultObj, videoObj, playVideosOnSuccess), 500);
                    }

                } catch (JSONException e) {
                    //Something is wrong with the JSON data sent
                    addLog("getVidAds res err: " + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "VideoAds Err: "+error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                addLog("getVidAds err: " + error.getMessage());
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private void createVideoLstFromApi(JSONObject defaultObj, JSONArray videoObj, boolean playVideosOnSuccess) {
        try{
            defaultVidAd = new VideoAd();
            defaultVidAd.setName(getVideoNameFromLink(defaultObj.getString("url")));
            defaultVidAd.setUrl(defaultObj.getString("url"));
            defaultVidAd.setVideoId(defaultObj.getInt("id"));
            defaultVidAd.setVersion(defaultObj.getInt("version"));
            defaultVidAd.setLocalPath("");
            defaultVidAd.setVideoId(0);
            defaultVidAd.setDownloadId(0);
            defaultVidAd.setDefault(true);
            defaultVidAd.setTransferred(false);

            videoChecker(defaultVidAd);

            if(vidPlayHandler.videoAdList() != null){
                vidPlayHandler.videoAdList().clear();
            }

            for (int i = 0; i < videoObj.length(); i++){
                JSONObject video = videoObj.getJSONObject(i);

                VideoAd v = new VideoAd();
                v.setName(getVideoNameFromLink(video.getString("url")));
                v.setUrl(video.getString("url"));
                v.setVideoId(video.getInt("id"));
                v.setVersion(video.getInt("version"));

                v.setLocalPath(dbHelper.getVideoAdLocalPath(v));
                v.setDownloadId(0);
                v.setDefault(false);
                v.setTransferred(false);

                videoChecker(v);

                vidPlayHandler.addVideoAd(v);
            }

            addLog("vidArrLst len: "+vidPlayHandler.videoAdListSize());

            if(playVideosOnSuccess){
                vidPlayHandler.playLoopVideo();
            }
        }catch (JSONException e){
            addLog("createVideoLstFromApi err: "+e.getLocalizedMessage());
        }
    }

    //This method checks if local path already exist.
    //If local path doesn't exist, download video and save local path afterwards.
    //Or anything related check for a videoAd
    private void videoChecker(VideoAd videoAd) {
        //TODO: default video didn't change even after the video file is changed. Maybe because the video version isn't updated.
        if(!dbHelper.isVideoAdExist(videoAd)){
            dbHelper.addVideoAd(videoAd);

            //TODO: search same named video inside directory to relink to a videoAd to not re-download

            startDownload(videoAd);
        }else{
            if(!dbHelper.isVideoAdVersionSame(videoAd)){
                dbHelper.updateVersion(videoAd.getVideoId(), videoAd.getVersion());

                fileHelper.mDeleteFile(videoAd.getLocalPath());

                startDownload(videoAd);
            }else{
                if(videoAd.isDefault()){
                    addLog("Skipped Download Default Video ");
                }else{
                    addLog("Skipped Download Video");
                }
            }

            if(videoAd.isDefault()){
                addLog("!Inserted Default videoAd");
            }else{
                addLog("!Inserted videoAd");
            }
        }
    }

    //constructs string for marquee. used with getMessageAds()
    private void appendTxtToMarquee(TextView lbl, String adMsg){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(adMsg);

        WindowManager window = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        int roughRemainingSpaces = screenWidth - adMsg.length();
        //int remainingSpaces = roughRemainingSpaces;//just for saving if we need it somewhere

        while(roughRemainingSpaces >= 0){
            stringBuilder.append(adMsg);
            roughRemainingSpaces -= adMsg.length();
        }

        String builtString = stringBuilder.toString();
        String finalTxt = builtString.trim();
        finalTxt = finalTxt.substring(0, finalTxt.length()-1);

        lbl.setText(finalTxt);

        lbl.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        lbl.setSingleLine(true);
        lbl.setMarqueeRepeatLimit(-1);
        lbl.setSelected(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        vidPlayHandler.releasePlayer();
        //unregisterReceiver(dlCompleteReceiver);
        adsHandler.removeCallbacksAndMessages(null);
        tempLockHandler.removeCallbacksAndMessages(null);

        editor = prefs.edit();
        editor.putInt(DOWNLOAD_ID, downloadId);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT <= 23) {
            vidPlayHandler.releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Util.SDK_INT <= 23) {
            vidPlayHandler.releasePlayer();
        }
    }

    //handles file downloading. places downloaded file inside Downloads folder
    public void startDownload(VideoAd videoAd) {
        String videoUrl = videoAd.getUrl();

        if(videoUrl.isEmpty()){
            addLog("Video url is empty, skipping download");
            return;
        }

        /*Uri uri = Uri.parse(videoUrl);

        Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        //new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+getVideoNameFromLink(videoUrl));

        lastDownload = dlMgr.enqueue(new DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                    DownloadManager.Request.NETWORK_MOBILE)
                            .setAllowedOverRoaming(false)
                            .setTitle("Downloading video ad")
                            .setDescription("Downloading "+getVideoNameFromLink(videoUrl))
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getVideoNameFromLink(videoUrl)));

        videoAd.setDownloadId(lastDownload);
        addLog("startingDownload "+videoAd.getName()+" id: "+lastDownload);

        DownloadItems di = new DownloadItems();
        di.setDownloadId((int) lastDownload);
        di.setDownloadComplete(false);

        dlItemsArrLst.add(di);*/

        downloadId++;
        downloadId = checkDownloadId(downloadId);
        videoAd.setDownloadId(downloadId);

        DownloadItems di = new DownloadItems();
        di.setDownloadId(downloadId);
        di.setDownloadComplete(false);

        dlItemsArrLst.add(di);

        File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(basePath, ((MainActivity)ctx).getVideoNameFromLink(videoAd.getUrl()));
        dlHelper.startDownloadVideoAd(videoAd.getUrl(), videoAd, outputFile);
        addLog("startingDownload "+videoAd.getName()+" id: "+downloadId);
    }

    //handles action after a download is finished. used for updating videoAd local path.
    /*private final BroadcastReceiver dlCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            //long xtraDlId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String dlFilePath = "";

            Cursor c = dlMgr.query(new DownloadManager.Query());
            boolean found = false;

            if(c.moveToFirst()) {
                do {

                    //String dlFilePath = Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).getPath();

                    boolean exitDlComplete = false;
                    int dlId = Integer.parseInt( c.getString(c.getColumnIndex(DownloadManager.COLUMN_ID)) );

                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        if (downloadFileLocalUri != null) {
                            File mFile = new File(Uri.parse(downloadFileLocalUri).getPath());

                            if(mFile.getAbsolutePath() != null){
                                dlFilePath = mFile.getAbsolutePath();
                            }else{
                                dlFilePath = "";
                            }

                        }
                    }else{
                        dlFilePath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    }

                    //For preventing multiple downloads of same id especially in slow connection
                    for(DownloadItems di : dlItemsArrLst){
                        if(di.getDownloadId() == dlId){
                            if(!di.isDownloadComplete() && ( dlFilePath != null && !dlFilePath.equals("") )){
                                di.setDownloadComplete(true);
                            }else{
                                exitDlComplete = true;
                            }
                        }
                        if(exitDlComplete){
                            addLog("download ["+dlId+"] repeated");
                            return;
                        }
                    }

                    addLog("dlFinished: " + dlFilePath+", dlId: "+dlId);

                    for(int x = 0; x < vidPlayHandler.videoAdListSize(); x++){
                        VideoAd va = vidPlayHandler.getVideoAd(x);
                        if(va.getDownloadId() == dlId && !dlFilePath.equals("")){
                            //check if videoAd already have a local path that looks like a valid file path, i think
                            //or to check if videoAd is not yet in external storage to prevent it to be moved there again, i think
                            *//*if(!va.getLocalPath().contains( getExternalDownloadPath() )){
                                dbHelper.updateLocalPath(va.getVideoId(), dlFilePath);
                            }*//*

                            saveVideoAd(va, dlFilePath, dlId);
                            found = true;

                            break;
                        }else if(defaultVidAd.getDownloadId() == dlId && !dlFilePath.equals("")){
                            *//*if(!defaultVidAd.getLocalPath().contains( getExternalDownloadPath() )){
                                dbHelper.updateLocalPath(defaultVidAd.getVideoId(), dlFilePath);
                            }*//*

                            saveVideoAd(defaultVidAd, dlFilePath, dlId);
                            found = true;

                            break;
                        }
                    }

                } while (c.moveToNext() && !found);
            } else {
                addLog("empty cursor :(");
            }
        }
    };*/

    private void saveVideoAd(VideoAd videoAd, String downloadedFilePath, int downloadId) {
        if(!videoAd.getName().isEmpty()){
        }
        dbHelper.updateLocalPath(videoAd.getVideoId(), downloadedFilePath);

        videoAd.setLocalPath(downloadedFilePath);

        //makeTransferItem(downloadedFilePath, va.getVideoId());
        addLog("saveVideoAd videoName: "+videoAd.getName()+" currentPlayingDlId: "+vidPlayHandler.videoPlayer().getCurrentVideoAd().getDownloadId()+" dlId: "+downloadId);

        if(vidPlayHandler.videoPlayer().getPlayMode() == VideoPlayer.STREAM && vidPlayHandler.videoPlayer().getCurrentVideoAd().getDownloadId() == downloadId){
            addLog("Video with same download id finished. Switching to local file playing");
            vidPlayHandler.startPlayFromFile(videoAd);
        }
    }

    //init downloader. asks permission for storage on lollipop devices and newer
    private void initDownloader() {
        //registerReceiver(dlCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //dlMgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_STATE);
        } else {
        }
    }

    //for checking if download is still running. used with startStream() and startPlayFromFile()
    public boolean isDownloading(Context context , long downloadId){
        return !getDownloadStatus(context , downloadId);
    }

    //for checking download status. used with isDownloading()
    private boolean getDownloadStatus(Context ctx, long downloadId) {
        /*DownloadManager downloadManager =
                (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);// filter your download by download Id
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            c.close();
            Log.i("DOWNLOAD_STATUS", String.valueOf(status));
            return status;
        }
        Log.i("AUTOMATION_DOWNLOAD", "DEFAULT");*/

        for (int x = 0; x < dlItemsArrLst.size(); x++){
            DownloadItems di = dlItemsArrLst.get(x);

            if(di.getDownloadId() == downloadId){
                return di.isDownloadComplete();
            }
        }

        return false;
    }

    //checks if server ip is already saved on SharedPreferences
    private boolean isServerIpExist() {
        if(prefs == null){
            prefs = getSharedPreferences(SHARE_VIDEO_ADS, MODE_PRIVATE);
        }

        serverURL = prefs.getString(SERVER_URL, "");
        addLog("serverUrlSaved: "+serverURL);

        if(!serverURL.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    //for hiding and showing the indicators. used with showIndicators()
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();

        if(editor == null){
            editor = prefs.edit();
        }

        if(event.getAction() != KeyEvent.ACTION_DOWN){
            return true;
        }

        switch(keycode){
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if(indicatorVisible){
                    showIndicators(false);

                    editor.putBoolean(SHOW_INDICATOR, false);
                    editor.commit();
                    indicatorVisible = false;
                }else{
                    showIndicators(true);

                    editor.putBoolean(SHOW_INDICATOR, true);
                    editor.commit();

                    indicatorVisible = true;
                }
            break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if(indicatorVisible){
                    showIndicators(false);

                    editor.putBoolean(SHOW_INDICATOR, false);
                    editor.commit();
                    indicatorVisible = false;
                }else{
                    showIndicators(true);

                    editor.putBoolean(SHOW_INDICATOR, true);
                    editor.commit();

                    indicatorVisible = true;
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                scrLogs.arrowScroll(ScrollView.FOCUS_UP);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                scrLogs.arrowScroll(ScrollView.FOCUS_DOWN);
                break;
        }

        return true;
    }

    //compulsory code for storage access request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_READ_STORAGE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }
                break;

        }
    }

    //for simplifying Log
    public void addLog(String log_msg){
        if(log_msg.contains("err:") || log_msg.contains("ERR")){
            Log.e(TAG, log_msg);
        }else{
            Log.d(TAG, log_msg);
        }

        if(indicatorVisible){
            /*String logMsg = lblLogs.getText().toString();
            logMsg += log_msg;*/
            try{
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        lblLogs.setText(lblLogs.getText().toString()+log_msg+"\n");
                    }
                });

            }catch (Exception e){
                Log.e(TAG, "addLog err: "+e.getLocalizedMessage());
                Log.e(TAG, "logMsg: "+log_msg);
            }

            int strLineCount = countStringLines(log_msg);

            for(int x = 0; x < strLineCount; x++){

                scrLogs.post(new Runnable() {
                    @Override
                    public void run() {
                        scrLogs.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }
    }

    //used with addLog
    private static int countStringLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    //checks if connection is available
    private boolean hasInternetConnection() {
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        boolean hasNetConn = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        addLog("hasNetConn: "+hasNetConn);
        return hasNetConn;
    }

    //get the name of a file from a url. used in a lot of things
    public String getVideoNameFromLink(String url) {
        String[] strArr = url.split("/");
        String fileName = strArr[strArr.length - 1];

        fileName = fileName.replace("%20", " ");
        return fileName;
    }

    public VideoPlayerHelper getVidPlayHandler(){
        return vidPlayHandler;
    }

    //for dummy video list. replace links if needed
    private void populateVidArrLst() {
        defaultUrl = "http://192.168.151.33:1228/media/files/video_Url_D1WHWQu0We/quick%20tutorial.mp4";

        VideoAd va1 = new VideoAd();
        va1.setName("Video 1");
        va1.setUrl("http://192.168.149.53:1228/media/files/video_Url_lt8XZUb4TZ/Sweet%20and%20Sour%20Fish_1.mp4");
        va1.setVideoId(4);
        va1.setVersion(0);

        vidPlayHandler.addVideoAd(va1);

        VideoAd va2 = new VideoAd();
        va2.setName("Video 2");
        va2.setUrl("http://192.168.149.53:1228/media/files/video_Url_jbdISQgEth/for%20Share%20Cafe_1.mp4");
        va2.setVideoId(5);
        va2.setVersion(0);

        vidPlayHandler.addVideoAd(va2);

        VideoAd va3 = new VideoAd();
        va3.setName("Video 3");
        va3.setUrl("http://192.168.149.53:1228/media/files/video_Url_68jtFATsA4/Beef%20Kaldereta_1.mp4");
        va3.setVideoId(6);
        va3.setVersion(0);

        vidPlayHandler.addVideoAd(va3);
    }

    //broadcastReceiver for wifi connection. used to eliminate close then open actions when connection
    // is not available after onBoot
    private final BroadcastReceiver connReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    try {
                        addLog("Network is connected, calling getAds");
                        getAdvertisements(serverURL, vidGroup);

                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    addLog("Network not connected");
                }
            }
        }
    };

    public class unlockCreateVidLstRunnable implements Runnable{
        JSONArray videoObj;
        boolean playVideosOnSuccess;
        JSONObject defaultObj;

        public unlockCreateVidLstRunnable(JSONObject defaultJsonObj, JSONArray videoJsonObj, boolean isPlayVideosOnSuccess){
            this.defaultObj = defaultJsonObj;
            this.videoObj = videoJsonObj;
            this.playVideosOnSuccess = isPlayVideosOnSuccess;
        }

        @Override
        public void run() {
            addLog("Unlocked create vid list in runnable");
            setCreateVidLstUnlocked();
            createVideoLstFromApi(defaultObj, videoObj, playVideosOnSuccess);
        }
    }

    public void setCreateVidLstLocked(){
        addLog("locked create vid lst");
        createVidLstLocked = true;
    }

    public void setCreateVidLstUnlocked(){
        addLog("unlocked create vid lst");
        createVidLstLocked = false;
    }

    //hides everything unnecessary to make app show in full screen
    public void fullScreen(){
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        } else {
                            // The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                    }
                });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        addLog("trimMemory");
        //releasePlayer();
    }
}