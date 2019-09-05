package com.alvkeke.tools.filetp.FileTransport;

import java.io.*;
import java.net.Socket;

public class FileRecvThread implements Runnable{

    private Socket mSocket;
    private FileRecvCallback mCallback;
    private String mSavePath;

    public static final byte RECV_FAILED_DATA_ERROR = 1;
    public static final byte RECV_FAILED_INCREDIBLE = 2;
    public static final byte RECV_FAILED_SAVE_PATH_ERROR = 3;

    FileRecvThread(FileRecvCallback callback, Socket socket, String savePath){
        mCallback = callback;
        mSocket = socket;
        mSavePath = savePath;
    }

    @Override
    public void run() {
        /*
        * finish the method for receiving file
        * */
        try {
            DataInputStream dis = new DataInputStream(mSocket.getInputStream());

            String deviceName = dis.readUTF();
            String filename = dis.readUTF();
            Long fileLength = dis.readLong();

            if (!mCallback.isCredible(deviceName)){
                mCallback.recvFileFailed(RECV_FAILED_INCREDIBLE, deviceName);
            }

            File dir = new File(mSavePath);
            if (!(dir.exists() && dir.isDirectory())){
                mCallback.recvFileFailed(RECV_FAILED_SAVE_PATH_ERROR, mSavePath);
            }
            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int length;
            while ((length = dis.read(buf)) != -1){
                fos.write(buf, 0, length);
                fos.flush();
            }

            mCallback.gotFile(file.getAbsolutePath());

            dis.close();
            fos.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
