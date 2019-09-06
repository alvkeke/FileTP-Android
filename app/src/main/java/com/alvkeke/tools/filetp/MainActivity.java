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

import com.alvkeke.tools.filetp.FileExplorer.FileListAdapter;
import com.alvkeke.tools.filetp.FileTransport.BroadcastCallback;
import com.alvkeke.tools.filetp.FileTransport.BroadcastHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvCallback;
import com.alvkeke.tools.filetp.FileTransport.FileRecvHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvThread;
import com.alvkeke.tools.filetp.FileTransport.FileSender;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements BroadcastCallback, FileRecvCallback {

    private HashMap<String, InetAddress> mOnlineUsers;
    private Set<String> mCredibleUsers;

    private ListView mFileList;
    private FileListAdapter mAdapter;

    private String mLocalDeviceName;
    private int mBeginPort;
    private String mSavePath;
    private boolean mIsShowHideFile;

    private final static String CONF_NAME = "configure";
    private final static String CONF_KEY_DEVICE_NAME = "deviceName";
    private final static String CONF_KEY_BEGIN_PORT = "beginPort";
    private final static String CONF_KEY_SAVE_PATH = "savePath";
    private final static String CONF_KEY_CREDIBLE_USERS = "credibleUsers";
    private final static String CONF_KEY_SHOW_HIDE_FILE = "showHideFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOnlineUsers = new HashMap<>();

        restoreConfigure();

        // todo: delete these code for test
        if (mCredibleUsers != null) {
            mCredibleUsers.add("alv-manjaro");
            mCredibleUsers.add("alv-rasp3b");
            mCredibleUsers.add("alv-xiaomi-4s");
        }

//        startListenServer(mLocalDeviceName, mBeginPort, mSavePath);
        startListenServer();

        mFileList = findViewById(R.id.lv_file_explorer);
        mFileList.setDivider(null);
        ArrayList<File> dirList = new ArrayList<>();
        ArrayList<File> fileList = new ArrayList<>();
        mAdapter = new FileListAdapter(this, dirList, fileList);
        mFileList.setAdapter(mAdapter);

        mAdapter.setShowHideFile(mIsShowHideFile);

        setEventListener();
        askForPermission();

    }

    @Override
    public void onBackPressed() {
        if (!mAdapter.moveToLastPath()) {
            super.onBackPressed();
        }
        mAdapter.notifyDataSetChanged();
    }

    void restoreConfigure(){

        SharedPreferences conf = getSharedPreferences(CONF_NAME, Context.MODE_PRIVATE);

        mCredibleUsers = conf.getStringSet(CONF_KEY_CREDIBLE_USERS, new HashSet<String>());
        mLocalDeviceName = conf.getString(CONF_KEY_DEVICE_NAME, "phone");
        mBeginPort = conf.getInt(CONF_KEY_BEGIN_PORT, 10000);
        mSavePath = conf.getString(CONF_KEY_SAVE_PATH, "");
        mIsShowHideFile = conf.getBoolean(CONF_KEY_SHOW_HIDE_FILE, false);

    }

    void setEventListener(){
        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("click", "position: " + position);
                File dir = mAdapter.getItem(position);

                if (!dir.exists()) {
                    Log.e("error", "directory is not exist");
                    return;
                }

                if (dir.isDirectory()){
                    Log.e("debug", "change directory");
                    mAdapter.setPath(dir);
//                    mAdapter.rankList();
                    mAdapter.notifyDataSetChanged();

                }

            }
        });

        mFileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            File fileToSend;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.e("long click", "position: " + position);
                fileToSend = mAdapter.getItem(position);
                Log.e("debug", "send file: "+ fileToSend.getAbsolutePath());

                if (!fileToSend.exists()) return true;

                if (fileToSend.isDirectory()){

                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("发送文件")
                            .setMessage("确定要发送\n" + fileToSend.getName() + "\n吗?" +
                                    "(size:" + fileToSend.length() +"B)")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    InetAddress address = mOnlineUsers.get("alv-manjaro");
                                    if (address == null){
                                        Toast.makeText(getApplicationContext(),
                                                "该用户已离线", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    FileSender fileSender = new FileSender(mLocalDeviceName, address, mBeginPort);
                                    fileSender.send(fileToSend);
                                }
                            });
                    builder.create().show();
                }
                return true;
            }
        });
    }

    void startListenServer(){
//        void startListenServer(String deviceName, int beginPort, String savePath){

        BroadcastHandler bcHandler = new BroadcastHandler(mLocalDeviceName, this);
        if (!bcHandler.startListen(mBeginPort)){
            Log.e("error", "start broadcast handler failed");
            return;
        }
        bcHandler.broadcast();
        bcHandler.requestBroadcast();
        Log.e("success", "start broadcast handler");

        FileRecvHandler frHandler = new FileRecvHandler(this, mSavePath);
        if (!frHandler.startListen(mBeginPort)){
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

    void showSdcardRootFiles(){

        File sdcard = new File(System.getenv("EXTERNAL_STORAGE"));

        mAdapter.setPath(sdcard);
//        mAdapter.rankList();
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
            showSdcardRootFiles();
        }
    }

    @Override
    public void gotClientOnline(String deviceName, InetAddress address) {

        mOnlineUsers.put(deviceName, address);
        Log.e("broadcast", deviceName);
    }

    @Override
    public void gotClientOffline(String deviceName) {
        mOnlineUsers.remove(deviceName);
    }

    @Override
    public boolean isCredible(String deviceName) {
        for (String s : mCredibleUsers){
            if (s.equals(deviceName)){
                return true;
            }
        }

        return false;
    }

    @Override
    public void recvFileBegin() {

    }

    @Override
    public void recvFileInProcess() {

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
