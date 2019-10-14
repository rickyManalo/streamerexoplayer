package biz.net.com.streamerexoplayer.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import biz.net.com.streamerexoplayer.models.VideoAd;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "vidStreamV3"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static final String DB_NAME ="sharecafe_video.db";// Database name
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    private final String TBL_VIDEO_ADS = "videoAds";
    private final String URL = "url";
    private final String VIDEO_ID = "video_id";
    private final String VERSION = "version";
    private final String LOCAL_PATH = "local_path";
    private final String VIDEO_NAME = "name";
    private final String IS_DEFAULT = "is_default";

    public DbHelper(Context context){
        super(context, DB_NAME, null, 1);// 1? its Database Version
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else{
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public void createDatabase() throws IOException{
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist){
            this.getReadableDatabase();
            this.close();
            try{
                //Copy the database from assests
                copyDataBase();
                Log.d(TAG, "createDatabase database created");
            }
            catch (IOException mIOException){
                Log.d(TAG, "createDb err: "+mIOException.getLocalizedMessage());
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }


    public String getServerAddress() {
        String server_address = "";
        String selectQuery = "SELECT  * FROM " + "server_address";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                server_address = cursor.getString(1);
            } while (cursor.moveToNext());
        }

        return server_address;
    }

    public int saveServerAddress(String server_address) {
        SQLiteDatabase dbcon = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("server_add", server_address);
        return dbcon.update("server_address", values, "ID" + " = ?",
                new String[]{String.valueOf(1)});
    }

    public int updateURL(int id, String newUrl) {
        SQLiteDatabase dbcon = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(URL, newUrl);
        return dbcon.update(TBL_VIDEO_ADS, values,"id=?", new String[]{String.valueOf(id)});
    }

    public int updateLocalPath(int videoId, String newLocalPath) {
        Log.d(TAG, "db updatingLocalPath");
        SQLiteDatabase dbcon = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOCAL_PATH, newLocalPath);
        return dbcon.update(TBL_VIDEO_ADS, values,VIDEO_ID+"=?", new String[]{String.valueOf(videoId)});
    }

    public int updateVersion(int videoId, int newVersion) {
        Log.d(TAG, "db updatingVersion");
        SQLiteDatabase dbcon = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VERSION, newVersion);
        return dbcon.update(TBL_VIDEO_ADS, values,VIDEO_ID+"=?", new String[]{String.valueOf(videoId)});
    }

    public void addVideoAd(VideoAd v){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conVal = new ContentValues();
        conVal.put(VIDEO_NAME, v.getName());
        conVal.put(URL, v.getUrl());
        conVal.put(VIDEO_ID, v.getVideoId());
        conVal.put(VERSION, v.getVersion());
        conVal.put(LOCAL_PATH, v.getLocalPath());
        conVal.put(IS_DEFAULT, (v.isDefault() ? 1 : 0));
        long rowInserted = db.insert(TBL_VIDEO_ADS, null, conVal);
        db.close();

        if(rowInserted != -1){
            Log.d(TAG, "db Inserted videoAd "+v.getName());
        }else{
            Log.d(TAG, "db !Inserted videoAd "+v.getName());
        }
    }

    public boolean isVideoAdExist(VideoAd va){
        String query = "SELECT * FROM "+TBL_VIDEO_ADS+" WHERE "+VIDEO_ID+"="+va.getVideoId();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    public boolean isVideoAdVersionSame(VideoAd fromServer){
        String query = "SELECT * FROM "+TBL_VIDEO_ADS+" WHERE "+VIDEO_ID+"="+fromServer.getVideoId();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst() && cursor.getCount() > 0){
            int fromDbVersion = cursor.getInt(cursor.getColumnIndex(VERSION));
            if(fromDbVersion == fromServer.getVersion()){
                cursor.close();
                return true;
            }else{
                return false;
            }
        }else{
            cursor.close();
            return false;
        }
    }

    public VideoAd getDefaultVideoAd(){
        VideoAd vAd = new VideoAd();
        String selectQuery = "SELECT * FROM "+TBL_VIDEO_ADS+" WHERE "+IS_DEFAULT+"=1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                vAd.setName(cursor.getString(cursor.getColumnIndex(VIDEO_NAME)));
                vAd.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
                vAd.setVideoId(cursor.getInt(cursor.getColumnIndex(VIDEO_ID)));
                vAd.setVersion(cursor.getInt(cursor.getColumnIndex(VERSION)));
                vAd.setLocalPath(cursor.getString(cursor.getColumnIndex(LOCAL_PATH)));
                vAd.setDefault(cursor.getInt(cursor.getColumnIndex(IS_DEFAULT)) == 1);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return vAd;
    }

    public String getVideoAdLocalPath(VideoAd va){
        String localPath = "";
        String selectQuery = "SELECT "+LOCAL_PATH+" FROM "+TBL_VIDEO_ADS+" WHERE "+VIDEO_ID+"="+va.getVideoId();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            localPath = cursor.getString(cursor.getColumnIndex(LOCAL_PATH));
        }

        cursor.close();

        if(localPath != null && !localPath.isEmpty()){
            return localPath;
        }else{
            return "";
        }
    }

    public ArrayList<VideoAd> getAllVideoAds() {
        ArrayList<VideoAd> videoAdLst = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+TBL_VIDEO_ADS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                VideoAd vAd = new VideoAd();

                vAd.setName(cursor.getString(cursor.getColumnIndex(VIDEO_NAME)));
                vAd.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
                vAd.setVideoId(cursor.getInt(cursor.getColumnIndex(VIDEO_ID)));
                vAd.setVersion(cursor.getInt(cursor.getColumnIndex(VERSION)));
                vAd.setLocalPath(cursor.getString(cursor.getColumnIndex(LOCAL_PATH)));
                vAd.setDefault(cursor.getInt(cursor.getColumnIndex(IS_DEFAULT)) == 1);

                videoAdLst.add(vAd);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return videoAdLst;
    }

    public boolean deleteVideoAd(VideoAd vAd){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TBL_VIDEO_ADS, VIDEO_ID+"=?", new String[]{Integer.toString( vAd.getVideoId() )}) > 0;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private boolean checkDataBase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    private void copyDataBase() throws IOException{
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public synchronized void close(){
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }
}
