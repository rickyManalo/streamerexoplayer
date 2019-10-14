package biz.net.com.streamerexoplayer.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import biz.net.com.streamerexoplayer.DownloadListener;
import biz.net.com.streamerexoplayer.MainActivity;
import biz.net.com.streamerexoplayer.models.VideoAd;

public class DownloadHelper{
    private static String TAG = "vidStreamV3";
    private static File outputFile = null;
    private static DownloadListener dlListener;
    private static Context ctx;
    private static String url;
    private int count = 0;

    public DownloadHelper(Context context){
        dlListener = null;
        ctx = context;
    }

    private class DownloadJob extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String fileUrl = (String)params[0];
            File outputFile = (File)params[1];
            int dlId = (int)params[2];

            try {
                URL u = new URL(fileUrl);
                URLConnection conn = u.openConnection();
                InputStream is;

                is = conn.getInputStream();

                OutputStream outputOs = new FileOutputStream(outputFile);
                byte[] data = new byte[is.available()];

                byte[] buffer = new byte[1024];
                int len;
                int lenCounter = 0;
                while ((len = is.read(buffer)) != -1) {
                    outputOs.write(buffer, 0, len);//Write new file
                    lenCounter += len;
                }

                is.close();
                outputOs.close();

                ((MainActivity)ctx).addLog("dlHelp finish");
                dlListener.onDownloadFinish(new HashMap<String, String>(){{
                    put("id", Integer.toString(dlId));
                    put("path", outputFile.getAbsolutePath());
                }});

            } catch(FileNotFoundException e) {
                Log.d(TAG, "fnf err: "+e.getLocalizedMessage());
            } catch (IOException e) {
                Log.d(TAG, "io err: "+e.getLocalizedMessage());
            }

            return "Nan";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    public void startDownloadVideoAd(String url, VideoAd videoAd, File output_file) {
        this.url = url;
        this.outputFile = output_file;
        //videoAd.setDownloadId(count);

        ((MainActivity) ctx).addLog("startDownloadVideoAd dlId: "+videoAd.getDownloadId());
        DownloadJob dlJob = new DownloadJob();
        dlJob.execute(url, output_file, videoAd.getDownloadId());
        count++;
    }

    public void setDownloadListener(DownloadListener listener){
        this.dlListener = listener;
    }
}
