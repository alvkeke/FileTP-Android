package com.alvkeke.tools.filetp.FileTransport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class BroadcastHandler {

    private String mLocalDeviceName;
    private DatagramSocket mSocket;
    private int mBroadPort;
    private BroadcastCallback mCallback;
    private boolean inLoop;

    public BroadcastHandler(String localDeviceName, BroadcastCallback callback){
        mLocalDeviceName = localDeviceName;
        mCallback = callback;
    }

    public void setLocalDeviceName(String name){
        mLocalDeviceName = name;
    }


    public boolean startListen(int port){

        inLoop = true;
        mBroadPort = port;
        try {
            mSocket = new DatagramSocket(mBroadPort);
            new Thread(new handleThread()).start();

            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return false;
    }

    class handleThread implements Runnable{
        @Override
        public void run() {

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (inLoop){

                Arrays.fill(buf, (byte) 0);
                packet.setData(buf);
                try {
                    mSocket.receive(packet);

                    // cmd(30) + remoteDeviceName
                    String data = new String(packet.getData()).trim();
                    if (data.length() <= 30){
                        continue;   // 防止字符串过短而导致程序关闭
                    }
                    String cmd = data.substring(0, 30);
                    String remoteDeviceName = data.substring(30);

                    InetAddress remoteAddress = packet.getAddress();

                    if (remoteDeviceName.equals(mLocalDeviceName)){
                        continue;
                    }

                    switch (cmd) {
                        case Cs.CMD_LOGIN_STR:
                            mCallback.gotClientOffline(remoteDeviceName);
                            mCallback.gotClientOnline(remoteDeviceName, remoteAddress);
                            break;
                        case Cs.CMD_LOGOUT_STR:
                            mCallback.gotClientOffline(remoteDeviceName);
                            break;
                        case Cs.CMD_BROADCAST_REQUEST:
                            broadcast();
                            break;
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

    public void broadcast(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // complete the method that broadcast this client's msg to other client
                String strSend = Cs.CMD_LOGIN_STR + mLocalDeviceName;
                byte[] data = strSend.getBytes();
                try {
                    InetAddress address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, mBroadPort);
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void logout(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sToSend = Cs.CMD_LOGOUT_STR + mLocalDeviceName;
                byte[] data = sToSend.getBytes();

                try {
                    InetAddress address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, mBroadPort);
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void requestBroadcast(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                // complete the method that broadcast this client's msg to other client
                String strSend = Cs.CMD_BROADCAST_REQUEST + mLocalDeviceName;
                byte[] data = strSend.getBytes();
                try {
                    InetAddress address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, mBroadPort);
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
