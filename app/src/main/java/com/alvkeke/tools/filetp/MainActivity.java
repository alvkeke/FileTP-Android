package com.alvkeke.tools.filetp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.alvkeke.tools.filetp.FileTransport.BroadcastCallback;
import com.alvkeke.tools.filetp.FileTransport.BroadcastHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvCallback;
import com.alvkeke.tools.filetp.FileTransport.FileRecvHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvThread;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements BroadcastCallback, FileRecvCallback {

    private HashMap<String, InetAddress> olUsers;
    private ArrayList<String> credibleUsers;

    private ListView mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForPermission();

        olUsers = new HashMap<>();
        credibleUsers = new ArrayList<>();
        // todo: load credible users from configure
        credibleUsers.add("alv-manjaro");
        credibleUsers.add("alv-rasp3b");
        credibleUsers.add("alv-xiaomi-4s");

        // todo: load device name and begin port from configure
        String deviceName = "alv-xiaomi-4s";
        int beginPort = 10000;
        startListenServer(deviceName, beginPort);

        mFileList = findViewById(R.id.lv_file_explorer);
        ArrayList<File> filesToShow = new ArrayList<>();
        FileListAdapter adapter = new FileListAdapter(this, filesToShow);
        mFileList.setAdapter(adapter);

        File sdcard = new File(System.getenv("EXTERNAL_STORAGE"));

        if (sdcard.exists()) {
            Log.e("debug", sdcard.getAbsolutePath());
            if (sdcard.isDirectory()){
                File[] files1 = sdcard.listFiles();
                filesToShow.clear();
                if (files1 != null) {
                    filesToShow.addAll(Arrays.asList(files1));
                }
            }
        }


        adapter.notifyDataSetChanged();


    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    void askForPermission(){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
            }
    }

    void startListenServer(String deviceName, int beginPort){

        // todo: change deviceName, load from configure
        BroadcastHandler bcHandler = new BroadcastHandler(deviceName, this);
        if (!bcHandler.startListen(beginPort)){
            Log.e("error", "start broadcast handler failed");
            return;
        }
        bcHandler.broadcast();
        bcHandler.requestBroadcast();
        Log.e("success", "start broadcast handler");

        FileRecvHandler frHandler = new FileRecvHandler(this);
        if (!frHandler.startListen(beginPort)){
            bcHandler.exit();
            Log.e("error", "start file receive handler failed");
            return;
        }
        Log.e("success", "start file receive handler");



    }

    @Override
    public void gotClientOnline(String user, InetAddress address) {

        olUsers.put(user, address);
        Log.e("broadcast", user);
    }

    @Override
    public void gotClientOffline(String user) {
        olUsers.remove(user);
    }

    @Override
    public boolean isCredible(String username) {
        for (String s : credibleUsers){
            if (s.equals(username)){
                return true;
            }
        }

        return false;
    }

    @Override
    public void gotFile(String fileLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("接收到文件")
                .setMessage(fileLocation)
                .setPositiveButton("确定", null);

        builder.create().show();
    }

    @Override
    public void recvFileFailed(byte Reason, String param) {
        switch (Reason){
            case FileRecvThread.RECV_FAILED_DATA_ERROR:
                Log.e("debug", "error data:" + param);
                break;
            case FileRecvThread.RECV_FAILED_INCREDIBLE:
                Toast.makeText(getApplicationContext(),
                        param + " 尝试发送文件，但被阻止", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
