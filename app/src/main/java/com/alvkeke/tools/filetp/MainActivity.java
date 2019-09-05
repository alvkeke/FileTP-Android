package com.alvkeke.tools.filetp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements BroadcastCallback, FileRecvCallback {

    private HashMap<String, InetAddress> olUsers;
    private Set<String> credibleUsers;

    private ListView mFileList;
    private FileListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences conf = getSharedPreferences("configure", Context.MODE_PRIVATE);

        olUsers = new HashMap<>();
        credibleUsers = conf.getStringSet("credibleUsers", new HashSet<String>());

        // todo: delete these code for test
        if (credibleUsers != null) {
            credibleUsers.add("alv-manjaro");
            credibleUsers.add("alv-rasp3b");
            credibleUsers.add("alv-xiaomi-4s");
        }

        String deviceName = conf.getString("deviceName", "phone");
        int beginPort = conf.getInt("beginPort", 10000);
        String savePath = conf.getString("savePath", "");
        startListenServer(deviceName, beginPort, savePath);

        mFileList = findViewById(R.id.lv_file_explorer);
        mFileList.setDivider(null);
        ArrayList<File> dirList = new ArrayList<>();
        ArrayList<File> fileList = new ArrayList<>();
        mAdapter = new FileListAdapter(this, dirList, fileList);
        mFileList.setAdapter(mAdapter);

        boolean showHideFile = conf.getBoolean("showHideFile", true);
        mAdapter.setShowHideFile(showHideFile);

        setEventListener();
        askForPermission();

    }

    void setEventListener(){
        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("click", "position: " + position);
            }
        });
    }

    void startListenServer(String deviceName, int beginPort, String savePath){

        BroadcastHandler bcHandler = new BroadcastHandler(deviceName, this);
        if (!bcHandler.startListen(beginPort)){
            Log.e("error", "start broadcast handler failed");
            return;
        }
        bcHandler.broadcast();
        bcHandler.requestBroadcast();
        Log.e("success", "start broadcast handler");

        FileRecvHandler frHandler = new FileRecvHandler(this, savePath);
        if (!frHandler.startListen(beginPort)){
            bcHandler.exit();
            Log.e("error", "start file receive handler failed");
            return;
        }
        Log.e("success", "start file receive handler");



    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static int STORAGE_PERMISSION_REQUEST_CODE = 1;

    boolean checkPermission(){
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) == PackageManager.PERMISSION_DENIED);
    }

    void askForPermission(){
        if (checkPermission())
        {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    void showSdcardFiles(){

        File sdcard = new File(System.getenv("EXTERNAL_STORAGE"));

        mAdapter.setPath(sdcard);
        mAdapter.rankList();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE){
            for (int i : grantResults){
                if (i == PackageManager.PERMISSION_DENIED){
                    finish();
                }
            }
            showSdcardFiles();
        }
    }

    @Override
    public void gotClientOnline(String deviceName, InetAddress address) {

        olUsers.put(deviceName, address);
        Log.e("broadcast", deviceName);
    }

    @Override
    public void gotClientOffline(String deviceName) {
        olUsers.remove(deviceName);
    }

    @Override
    public boolean isCredible(String deviceName) {
        for (String s : credibleUsers){
            if (s.equals(deviceName)){
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
    public void recvFileFailed(byte Reason, final String param) {
        switch (Reason){
            case FileRecvThread.RECV_FAILED_DATA_ERROR:
                Log.e("debug", "error data:" + param);
                break;
            case FileRecvThread.RECV_FAILED_INCREDIBLE:
                Toast.makeText(getApplicationContext(),
                        param + " 尝试发送文件，但被阻止", Toast.LENGTH_SHORT).show();
                break;
            case FileRecvThread.RECV_FAILED_SAVE_PATH_ERROR:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("接收文件")
                        .setMessage("保存路径不存在，是否创建?\n(此时创建文件夹并不能保存当前传输的文件)");
                builder.setNegativeButton("取消",null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!new File(param).mkdir()){
                            Toast.makeText(getApplicationContext(),
                                    "自动创建失败。。。", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        }
    }

}
