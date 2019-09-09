package com.alvkeke.tools.filetp.fileTransport;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileRecvHandler {

    private ServerSocket serverSocket;
    private boolean inLoop;
    private FileRecvCallback mCallback;
    private String mSavePath;

    public FileRecvHandler(FileRecvCallback callback, String savePath){
        mCallback = callback;
        mSavePath = savePath;
    }

    public boolean startListen(int port){

        inLoop = true;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(new ListenThread()).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    class ListenThread implements Runnable{
        @Override
        public void run() {

            while (inLoop){
                try {
                    Socket socketAccept = serverSocket.accept();
                    Log.e("debug", "got an client want to send task.");
                    new Thread(new FileRecvThread(mCallback, socketAccept, mSavePath)).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exit(){
        inLoop = false;
    }

}
