package biz.net.com.streamerexoplayer.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import biz.net.com.streamerexoplayer.MainActivity;

public class FileHelper {
    Context ctx;
    String appName;
    private ArrayList<HashMap<String, Object>> transferablePathLst;
    String TAG = "vidStreamV3";

    public FileHelper(Context context){
        ctx = context;
        appName = ((MainActivity) ctx).appName;
    }

    //Adds paths for transferring to external storage
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeTransferItem(String downloaded_file_path, int video_id) {
        /*if(getExternalFilesDirs(null)[1] != null && !getExternalFilesDirs(null)[1].getAbsolutePath().isEmpty()){
            if(!downloaded_file_path.equals("")){
                addTransferablePath(downloaded_file_path, video_id);
            }
        }else{
            addLog("External storage not found; transfer cancelled");
        }*/
    }

    //get the path of the available external storage
    private String getExternalDownloadPath(){
        String externalStoragePath = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try{
                ((MainActivity) ctx).addLog("getExternalDownloadPath externalFileDir[0]: "+ctx.getExternalFilesDirs(null)[0]+" externalFileDir[1]: "+ctx.getExternalFilesDirs(null)[1]);
            }catch (Exception e){
                ((MainActivity) ctx).addLog("getExternalDownloadPath externalFileDir err: "+e.getLocalizedMessage());
            }

            //try to get sdcard path, i think
            if(ctx.getExternalFilesDirs(null)[1] != null){
                try{
                    externalStoragePath = ctx.getExternalFilesDirs(null)[1].getAbsolutePath();
                    ((MainActivity) ctx).addLog("getExternalDownloadPath getExternalFilesDirs(null)[1]: "+externalStoragePath);
                }catch (Exception e){
                    ((MainActivity) ctx).addLog("getExternalDownloadPath getExternalFilesDirs err: "+e.getLocalizedMessage());
                }
            }
        }

        if(externalStoragePath.equals("")){
            try{
                externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
                ((MainActivity) ctx).addLog("getExternalDownloadPath externalStoragePath empty: "+externalStoragePath);
            }catch (Exception e){
                ((MainActivity) ctx).addLog("getExternalDownloadPath externalStoragePath empty err: "+e.getLocalizedMessage());
            }
        }

        return externalStoragePath;
    }

    //deletes file. used with doOtherVideoDeletions()
    public void mDeleteFile(String path){
        ((MainActivity) ctx).addLog("MDELETEFILE path: "+path);
        File file = new File(path);
        boolean isDeleted1 = file.delete();

        if(isDeleted1){
            ((MainActivity) ctx).addLog("file deleted 1");
        }
        if(file.exists()){
            try {
                boolean isDeleted2 = file.getCanonicalFile().delete();

                if(isDeleted2){
                    ((MainActivity) ctx).addLog("file deleted 2");
                }
            } catch (IOException e) {
                ((MainActivity) ctx).addLog("file deleted 2 err: "+e.getMessage());
                e.printStackTrace();
            }
            if(file.exists()){
                boolean isDeleted3 = ctx.getApplicationContext().deleteFile(file.getName());

                if(isDeleted3){
                    ((MainActivity) ctx).addLog("file deleted 3");
                }
            }
        }
    }

    //reserve code for transferring file to external storage
    private void doTransfers() {
        int count = 0;
        while(transferablePathLst.size() > 0){
            HashMap<String, Object> srcHashMap = transferablePathLst.get(count);
            String srcPath = srcHashMap.get( ((MainActivity) ctx).DOWNLOAD_FILE_PATH ).toString();
            int videoId = (int) srcHashMap.get( ((MainActivity) ctx).VIDEO_ID );

            if(!((MainActivity) ctx).getVidPlayHandler().videoPlayer().getCurrentVideoAd().getLocalPath().equals(srcPath)){
                File srcFile = new File(srcPath, "");

                String externalStoragePath = getExternalDownloadPath();
                File dstFile = new File(externalStoragePath);
                ((MainActivity) ctx).addLog("srcFile: "+srcPath+" \n dstFile: "+externalStoragePath);

                try {
                    transferablePathLst.remove(count);
                    exportFile(srcFile, dstFile, videoId);
                    //copyFile(srcFile, dstFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    transferablePathLst.remove(count);
                    Log.d(TAG, "doTransfers ioErr: "+e.getLocalizedMessage());
                    //addLog("doTransfers ioErr: "+e.getLocalizedMessage());
                }
            }
            else{
                transferablePathLst.remove(count);
                ((MainActivity) ctx).addLog("file currently playing "+((MainActivity) ctx).getVidPlayHandler().videoPlayer().getCurrentVideoAd().getLocalPath());
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        addTransferablePath(srcPath, videoId);
                        Log.d(TAG, "re-adding "+((MainActivity) ctx).getVidPlayHandler().videoPlayer().getCurrentVideoAd().getLocalPath()+" in transfer list");
                    }
                }, 40000);
            }

            count++;
        }
        ((MainActivity) ctx).addLog("doTransfers done");
    }

    //adds file path to to-be transferred files. used with doTransfers()
    private void addTransferablePath(String dlFilePath, Integer videoId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(((MainActivity) ctx).DOWNLOAD_FILE_PATH, dlFilePath);
        hashMap.put(((MainActivity) ctx).VIDEO_ID, videoId);
        transferablePathLst.add(hashMap);
        doTransfers();
    }

    //makes the transferring of files
    private File exportFile(File src, File dst, int videoId) throws IOException  {

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File expFile = new File(dst.getPath() + File.separator + src.getName().replaceAll("%20", ""));
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            ((MainActivity) ctx).addLog("exportFile fnf err: "+e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }catch (Exception e){
            ((MainActivity) ctx).addLog("exportFile err: "+e.getLocalizedMessage());
            return null;
        }

        ((MainActivity) ctx).addLog("inChannel size: "+inChannel.size());

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            ((MainActivity) ctx).addLog("EXPORT FILE dstPath: "+dst.getAbsolutePath()+"/"+((MainActivity) ctx).getVideoNameFromLink(src.getAbsolutePath()));

            ((MainActivity) ctx).dbHelper.updateLocalPath(videoId, dst.getAbsolutePath()+"/"+((MainActivity) ctx).getVideoNameFromLink(src.getAbsolutePath()));
            mDeleteFile(src.getAbsolutePath());
        }catch (Exception e){
            ((MainActivity) ctx).addLog("exportFile err: "+e.getLocalizedMessage());
        }finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
            ((MainActivity) ctx).addLog("exportFile close");
        }

        return expFile;
    }

    public void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                ((MainActivity) ctx).addLog("COPY FILE dstPath: "+dst.getAbsolutePath()+"/"+((MainActivity) ctx).getVideoNameFromLink(src.getAbsolutePath()));
            }
        }
    }
}
