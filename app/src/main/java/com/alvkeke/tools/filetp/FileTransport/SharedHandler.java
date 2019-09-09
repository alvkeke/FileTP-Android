package com.alvkeke.tools.filetp.FileTransport;

import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SharedHandler {

    private ServerSocket serverSocket;
    private boolean inLoop;
    private SharedCallback mCallback;

    public SharedHandler(SharedCallback callback){
        mCallback = callback;
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

                    socketAccept.setSoTimeout(10000);
                    DataInputStream dis = new DataInputStream(socketAccept.getInputStream());

                    while (true){
                        try {
                            String filePath = dis.readUTF();
                            File file = new File(filePath);
                            mCallback.gotShare(file);
                        } catch (SocketTimeoutException e){
                            dis.close();
                            socketAccept.close();
                            break;
                        }
                    }

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
