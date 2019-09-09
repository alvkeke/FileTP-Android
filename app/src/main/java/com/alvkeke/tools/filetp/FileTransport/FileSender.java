package com.alvkeke.tools.filetp.FileTransport;


import android.util.Log;

import com.alvkeke.tools.filetp.ListAdapter.TaskItem;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class FileSender {

    private String mLocalDeviceName;
    private InetAddress mAddress;
    private int mPort;
    private FileSenderCallback mCallback;

    public FileSender(FileSenderCallback callback, String localDeviceName, InetAddress address, int port){
        mCallback = callback;
        mLocalDeviceName = localDeviceName;
        mAddress = address;
        mPort = port;

    }

    public void send(TaskItem task){

        new Thread(new SenderThread(task)).start();
    }

    class SenderThread implements Runnable{

        TaskItem task;

        SenderThread(TaskItem task){
            this.task = task;
        }

        @Override
        public void run() {

            try {
                Socket socket = new Socket(mAddress, mPort);
                FileInputStream fis = new FileInputStream(task);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(mLocalDeviceName);
                dos.flush();
                dos.writeUTF(task.getName());
                dos.flush();
                dos.writeLong(task.length());

                byte[] buf = new byte[1024];
                float sentLength= 0;
                int length;
                while ((length = fis.read(buf)) != -1){
                    dos.write(buf, 0, length);
                    dos.flush();
                    sentLength += length;
                    mCallback.sendFileInProcess(task, sentLength/task.length()*100);
                }
                Log.e("debug", "task send success");

                fis.close();
                dos.close();

                socket.close();
                mCallback.sendFileSuccess(task);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("debug", "task send failed");
                mCallback.sendFileFailed(task);
            }
        }
    }
}
