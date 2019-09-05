package com.alvkeke.tools.filetp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alvkeke.tools.filetp.FileTransport.BroadcastCallback;
import com.alvkeke.tools.filetp.FileTransport.BroadcastHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvCallback;
import com.alvkeke.tools.filetp.FileTransport.FileRecvHandler;
import com.alvkeke.tools.filetp.FileTransport.FileRecvThread;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements BroadcastCallback, FileRecvCallback {

    private HashMap<String, InetAddress> olUsers;
    private ArrayList<String> credibleUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        olUsers = new HashMap<>();
        credibleUsers = new ArrayList<>();
        // todo: load users from configure
        credibleUsers.add("alv-manjaro");
        credibleUsers.add("alv-rasp3b");
        credibleUsers.add("alv-xiaomi-4s");

        startListenServer("alv-xiaomi-4s", 10000);

        gotFile("hello");

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
