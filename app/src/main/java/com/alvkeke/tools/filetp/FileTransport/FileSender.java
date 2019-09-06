package com.alvkeke.tools.filetp.FileTransport;


import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class FileSender {

    private String mLocalDeviceName;
    private InetAddress mAddress;
    private int mPort;

    public FileSender(String localDeviceName, InetAddress address, int port){
        mLocalDeviceName = localDeviceName;
        mAddress = address;
        mPort = port;

    }

    public void send(File file){

        new Thread(new SenderThread(file)).start();
    }

    class SenderThread implements Runnable{

        File file;

        SenderThread(File file){
            this.file = file;
        }

        @Override
        public void run() {

            try {
                Socket socket = new Socket(mAddress, mPort);
                FileInputStream fis = new FileInputStream(file);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(mLocalDeviceName);
                dos.flush();
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());

                byte[] buf = new byte[1024];
                int length;
                while ((length = fis.read(buf)) != -1){
                    dos.write(buf, 0, length);
                    dos.flush();
                }
                Log.e("debug", "file send success");

                fis.close();
                dos.close();

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("debug", "file send failed");
            }
        }
    }
}
