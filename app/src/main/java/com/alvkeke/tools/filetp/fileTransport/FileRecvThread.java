package com.alvkeke.tools.filetp.fileTransport;

import com.alvkeke.tools.filetp.listAdapter.RecvTaskItem;

import java.io.*;
import java.net.Socket;
import java.util.Date;

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
        * finish the method for receiving task
        * */
        try {
            DataInputStream dis = new DataInputStream(mSocket.getInputStream());

            String deviceName = dis.readUTF();
            String filename = dis.readUTF();
            long fileLength = dis.readLong();

            RecvTaskItem task = mCallback.recvFileBegin(deviceName, filename, fileLength);

            if (!mCallback.isCredible(deviceName)){
                mCallback.recvFileFailed(task, RECV_FAILED_INCREDIBLE, deviceName);
                dis.close();
                mSocket.close();
                return;
            }

            File dir = new File(mSavePath);
            if (!(dir.exists() && dir.isDirectory())){
                mCallback.recvFileFailed(task, RECV_FAILED_SAVE_PATH_ERROR, mSavePath);
            }
            File file = new File(dir, filename);
            if (file.exists()){
                file = new File(dir, filename +"_"+ new Date().getTime());
            }

            FileOutputStream fos = new FileOutputStream(file);

            float recvLength = 0;
            byte[] buf = new byte[1024];
            int length;
            while ((length = dis.read(buf)) != -1){
                fos.write(buf, 0, length);
                fos.flush();
                recvLength += length;
                mCallback.recvFileInProcess(task, recvLength/fileLength * 100);
            }

            mCallback.recvFileSuccess(task, file.getAbsolutePath());

            dis.close();
            fos.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
