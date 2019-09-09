package com.alvkeke.tools.filetp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alvkeke.tools.filetp.ListAdapter.FileListAdapter;
import com.alvkeke.tools.filetp.FileTransport.BroadcastCallback;
import com.alvkeke.tools.filetp.FileTransport.BroadcastHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvCallback;
import com.alvkeke.tools.filetp.FileTransport.FileRecvHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvThread;
import com.alvkeke.tools.filetp.FileTransport.FileSenderCallback;
import com.alvkeke.tools.filetp.FileTransport.SharedCallback;
import com.alvkeke.tools.filetp.FileTransport.SharedHandler;
import com.alvkeke.tools.filetp.ListAdapter.TaskItem;
import com.alvkeke.tools.filetp.ListAdapter.UserListAdapter;
import com.alvkeke.tools.filetp.ListAdapter.TaskListAdapter;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements BroadcastCallback, FileRecvCallback, FileSenderCallback, SharedCallback {

    private Set<String> mCredibleUsers;

    private Menu mMenu;

    private ListView mFileList;
    private FileListAdapter mFileListAdapter;

    private ListView mTaskList;
    private TaskListAdapter mTaskAdapter;

    private ListView mUserList;
    private UserListAdapter mUserAdapter;

    SwipeRefreshLayout tasksRefresher;
    SwipeRefreshLayout usersRefresher;

    private String mLocalDeviceName;
    private String mAttendDeviceName;
    private int mBeginPort;
    private String mSavePath;
    private boolean mIsShowHideFile;
    private int mAllowThreadNumber;

    private BroadcastHandler bcHandler;
    private FileRecvHandler frHandler;


    public final static String CONF_NAME = "configure";
    public final static String CONF_KEY_DEVICE_NAME = "deviceName";
    public final static String CONF_KEY_ATTEND_DEVICE = "attendDevice";
    public final static String CONF_KEY_BEGIN_PORT = "beginPort";
    public final static String CONF_KEY_SAVE_PATH = "savePath";
    public final static String CONF_KEY_CREDIBLE_USERS = "credibleUsers";
    public final static String CONF_KEY_SHOW_HIDE_FILE = "showHideFile";
    public final static String CONF_KEY_ALLOW_THREAD_NUMBER = "allowThreadNumber";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStoragePermission();

    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static int STORAGE_PERMISSION_REQUEST_CODE = 1;

    boolean checkPermission(){
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) == PackageManager.PERMISSION_DENIED);
    }

    void getStoragePermission(){
        if (checkPermission())
        {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE, STORAGE_PERMISSION_REQUEST_CODE);
        }
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
            afterGetPermission();
        }
    }

    void afterGetPermission(){

        restoreConfigure();

        startServerListener();

        setUserInterfaceViews();

//        setEventListener();

        showSdcardRootFiles();
    }

    void restoreConfigure(){

        SharedPreferences conf = getSharedPreferences(CONF_NAME, Context.MODE_PRIVATE);

        mCredibleUsers = conf.getStringSet(CONF_KEY_CREDIBLE_USERS, new HashSet<String>());
        mLocalDeviceName = conf.getString(CONF_KEY_DEVICE_NAME, "phone");
        mAttendDeviceName = conf.getString(CONF_KEY_ATTEND_DEVICE, "alv-manjaro");
        mBeginPort = conf.getInt(CONF_KEY_BEGIN_PORT, 10000);
        mSavePath = conf.getString(CONF_KEY_SAVE_PATH, "");
        mIsShowHideFile = conf.getBoolean(CONF_KEY_SHOW_HIDE_FILE, true);
        mAllowThreadNumber = conf.getInt(CONF_KEY_ALLOW_THREAD_NUMBER, -1);

        SharedPreferences.Editor editor = conf.edit();
        if (mSavePath.isEmpty()){
            mSavePath = System.getenv("EXTERNAL_STORAGE") + "/Download/";
            editor.putString(CONF_KEY_SAVE_PATH, mSavePath);
        }

        // todo: delete these codes
        if (mCredibleUsers.isEmpty()){
            mCredibleUsers.add("alv-manjaro");
            mCredibleUsers.add("alv-rasp3b");
            mCredibleUsers.add("alv-xiaomi-4s");
            mCredibleUsers.add("alv-xiaomi-9se");
            editor.putStringSet(CONF_KEY_CREDIBLE_USERS, mCredibleUsers);
        }
        editor.apply();

    }

    void startServerListener(){

        bcHandler = new BroadcastHandler(mLocalDeviceName, this);
        if (!bcHandler.startListen(mBeginPort)){
            Log.e("error", "start broadcast handler failed");
            finish();
            return;
        }
        bcHandler.broadcast();
        bcHandler.requestBroadcast();
        Log.e("success", "start broadcast handler");

        frHandler = new FileRecvHandler(this, mSavePath);
        if (!frHandler.startListen(mBeginPort)){
            bcHandler.exit();
            Log.e("error", "start file receive handler failed");
            finish();
            return;
        }
        Log.e("success", "start file receive handler");

        SharedHandler sharedHandler = new SharedHandler(this);
        if (!sharedHandler.startListen(mBeginPort + 1)){
            sharedHandler.exit();
            Log.e("error", "start file receive handler failed");
            finish();
            return;
        }
        Log.e("success", "start share listener");

    }

    void setUserInterfaceViews(){

        mFileList = findViewById(R.id.lv_file_explorer);
        mUserList = findViewById(R.id.drawer_list_view_users);
        mTaskList = findViewById(R.id.drawer_list_view_tasks);
        tasksRefresher = findViewById(R.id.refresh_tasks);
        usersRefresher = findViewById(R.id.refresh_users);

        mFileList.setDivider(null);

        mFileListAdapter = new FileListAdapter(this);
        mUserAdapter = new UserListAdapter(this);
        mTaskAdapter = new TaskListAdapter(this, this);

        mFileList.setAdapter(mFileListAdapter);
        mUserList.setAdapter(mUserAdapter);
        mTaskList.setAdapter(mTaskAdapter);

        mFileListAdapter.setShowHideFile(mIsShowHideFile);
        mUserAdapter.setCurrentTargetDevice(mAttendDeviceName);
//        mTaskAdapter.setAllowThreadNumber(mAllowThreadNumber);
        mTaskAdapter.setAllowThreadNumber(1);

        setEventListener();

    }

    void setEventListener(){
        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File dir = mFileListAdapter.getItem(position);

                if (!dir.exists()) {
                    Log.e("error", "directory is not exist");
                    return;
                }

                if (dir.isDirectory()){
                    Log.e("debug", "change directory");
                    mFileListAdapter.setPath(dir);
                    mFileListAdapter.notifyDataSetChanged();
                } else {
                    mFileListAdapter.toggleSelectState(position);
                    setFileMenuVisible(mFileListAdapter.hasSelected());
                    mFileListAdapter.notifyDataSetChanged();
                }

            }
        });

        mFileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            File fileToSend;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.e("long click", "position: " + position);
                fileToSend = mFileListAdapter.getItem(position);
                Log.e("debug", "send file: "+ fileToSend.getAbsolutePath());

                if (!fileToSend.exists()) return true;

                if (fileToSend.isDirectory()){
                    // 发送文件夹中的所有文件

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("发送文件")
                            .setMessage("确定要发送文件夹：\n" + fileToSend.getName() +
                                    "\n下的所有文件吗? (不包括子文件夹内的文件)")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    for (File file : fileToSend.listFiles()) {
                                        if (file.isDirectory()) continue;
                                        TaskItem task = new TaskItem(file.getAbsolutePath());
                                        mTaskAdapter.addTask(task);
//                                        mTaskAdapter.notifyDataSetChanged();
                                    }

                                    InetAddress address = mUserAdapter.getSelectAddress();
                                    if (address != null) {
                                        mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
                                    }
                                    mTaskAdapter.notifyDataSetChanged();
                                }
                            });
                    builder.create().show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("发送文件")
                            .setMessage("确定要发送\n" + fileToSend.getName() + "\n吗?" +
                                    "(size:" + fileToSend.length() +"B)")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    TaskItem task = new TaskItem(fileToSend.getAbsolutePath());
                                    mTaskAdapter.addTask(task);
//                                    mTaskAdapter.notifyDataSetChanged();

                                    InetAddress address = mUserAdapter.getSelectAddress();
                                    if (address != null) {
                                        mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
                                    }
                                    mTaskAdapter.notifyDataSetChanged();
                                }
                            });
                    builder.create().show();
                }
                return true;
            }
        });

        tasksRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mTaskAdapter.clearFinishedTask();
                mTaskAdapter.notifyDataSetChanged();

                InetAddress address = mUserAdapter.getSelectAddress();
                if (address != null) {
                    mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "请检查目标设备是否在线", Toast.LENGTH_SHORT).show();
                }
                mTaskAdapter.notifyDataSetChanged();
                tasksRefresher.setRefreshing(false);
            }
        });

        usersRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bcHandler.broadcast();
                bcHandler.requestBroadcast();
                usersRefresher.setRefreshing(false);
            }
        });

    }

    void showSdcardRootFiles(){

        File sdcard = new File(System.getenv("EXTERNAL_STORAGE"));

        mFileListAdapter.setPath(sdcard);
        mFileListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {

        InetAddress address = mUserAdapter.getSelectAddress();
        if (address != null){
            mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
        }
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        if (!mFileListAdapter.moveToLastPath()) {
            super.onBackPressed();
        }
        mFileListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        menu.findItem(R.id.menu_main_select_all).setVisible(false);
        menu.findItem(R.id.menu_main_send).setVisible(false);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_select_all:

                if (mFileListAdapter.isSelectAll()){
                    mFileListAdapter.unselectAll();
                } else {
                    mFileListAdapter.selectAll();
                }
                setFileMenuVisible(mFileListAdapter.hasSelected());
                mFileListAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_main_send:

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("发送文件")
                        .setMessage("确定要发送选中的所有文件吗?")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 添加到任务队列
                                for (File e : mFileListAdapter.getSelectFiles()){
                                    TaskItem task = new TaskItem(e.getAbsolutePath());
                                    mTaskAdapter.addTask(task);
                                }
                                InetAddress address = mUserAdapter.getSelectAddress();
                                if (address != null) {
                                    mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);

                                }
                                setFileMenuVisible(false);
                                mFileListAdapter.unselectAll();
                                mFileListAdapter.notifyDataSetChanged();
                                mTaskAdapter.notifyDataSetChanged();
                            }
                        });
                builder.create().show();
                break;
            case R.id.menu_main_setting:

                Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
                // todo: change the request code
                startActivityForResult(intentSetting, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void setFileMenuVisible(boolean visible){
        mMenu.findItem(R.id.menu_main_send).setVisible(visible);
        mMenu.findItem(R.id.menu_main_select_all).setVisible(visible);
    }


    @Override
    public void gotClientOnline(String deviceName, InetAddress address) {

        mUserAdapter.addUser(deviceName, address);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.notifyDataSetChanged();
//                usersRefresher.setRefreshing(false);
            }
        });
        Log.e("broadcast", deviceName);
    }

    @Override
    public void gotClientOffline(String deviceName) {
        mUserAdapter.removeUser(deviceName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.notifyDataSetChanged();
            }
        });
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

    @Override
    public void sendFileFailed(TaskItem task) {
        mTaskAdapter.setTaskError(task);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTaskAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void sendFileSuccess(TaskItem task) {
        mTaskAdapter.finishTask(task);

        InetAddress address = mUserAdapter.getSelectAddress();
        if (address != null) {
            mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTaskAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void sendFileInProcess(TaskItem taskItem, float percentage) {
//        Log.e("sending", taskItem.getName() + " : "+ percentage);
        taskItem.setPercentage(percentage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTaskAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void gotShare(File file) {
        TaskItem task = new TaskItem(file.getAbsolutePath());
        mTaskAdapter.addTask(task);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTaskAdapter.notifyDataSetChanged();
                InetAddress address = mUserAdapter.getSelectAddress();
                if (address != null) {
                    mTaskAdapter.checkWaitingTasks(address, mBeginPort, mLocalDeviceName);
                }
                mTaskAdapter.notifyDataSetChanged();
            }
        });
    }
}
