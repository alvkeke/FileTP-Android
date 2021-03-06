package com.alvkeke.tools.filetp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ShardedActivity extends AppCompatActivity {


    static final String EXTRA_NAME_PATH = "filePath";
    static final String EXTRA_NAME_PATH_LIST = "filePathList";

    private int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharded);

        SharedPreferences conf = getSharedPreferences(MainActivity.CONF_NAME, 0);
        mPort = conf.getInt(MainActivity.CONF_KEY_BEGIN_PORT, 10000)+1;

        Intent iSend = getIntent();
        String action = iSend.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleSingleSend(iSend);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleMultiSend(iSend);
        }

        finish();

    }

    String handleUri(Uri uri){

        if (uri == null) {
            return null;
        }

        String filePath = uri.getPath();
        if (filePath == null){
            return null;
        }

        File file = new File(filePath);

        if (!file.exists()){
            Log.e("handleUri", "path error or data format error, fix it. \n" + filePath);

            String[] splits = filePath.split("/");
            File sdcardPath = Environment.getExternalStoragePublicDirectory("");

            int iii;
            for (iii = 0; iii<splits.length; iii++){
                if (splits[iii].isEmpty())
                    continue;
                File t = new File(sdcardPath, splits[iii]);
                if (t.exists()){
                    break;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(sdcardPath.getAbsoluteFile());
            for (; iii<splits.length; iii++){
                sb.append('/');
                sb.append(splits[iii]);
            }
            filePath = sb.toString();

            file = new File(filePath);
            if (!file.exists()) {
                Log.e("handleUri", "file is not exist:" + filePath);
                Toast.makeText(getApplicationContext(),
                        "获取文件路径失败...", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return filePath;
    }

    void handleSingleSend(Intent iSend){

        Uri uri = iSend.getParcelableExtra(Intent.EXTRA_STREAM);

        String filePath = handleUri(uri);
        if (filePath == null) {
            return;
        }

        Log.e("debug", "found file: " + filePath);

        Intent iBack = new Intent(ShardedActivity.this, MainActivity.class);
//        iBack.putExtra(EXTRA_NAME_PATH, filePath);
        startActivity(iBack);
        new Thread(new SendSingleTask(filePath)).start();
    }

    void handleMultiSend(Intent iSend){

        ArrayList<Uri> uris = iSend.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        ArrayList<String> files = new ArrayList<>();

        for (Uri uri : uris){
            String file = handleUri(uri);
            if (file != null){
                files.add(file);
                Log.e("debug", "found file: " + file);
            }
        }

        if (files.isEmpty()){
            return;
        }

        Intent iBack = new Intent(ShardedActivity.this, MainActivity.class);
//        iBack.putStringArrayListExtra(EXTRA_NAME_PATH_LIST, files);
        startActivity(iBack);
        new Thread(new SendMultiTask(files)).start();

    }

    class SendSingleTask implements Runnable{

        String mFileName;

        SendSingleTask(String filename){
            mFileName = filename;
        }

        @Override
        public void run() {
            try {

                Socket socket = new Socket("127.0.0.1", mPort);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(mFileName);
                dos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class SendMultiTask implements Runnable{

        ArrayList<String> mFiles;

        SendMultiTask(ArrayList<String> files){
            mFiles = files;
        }

        @Override
        public void run() {
            try {

                Socket socket = new Socket("127.0.0.1", mPort);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                for (String s : mFiles) {
                    dos.writeUTF(s);
                }
                dos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
