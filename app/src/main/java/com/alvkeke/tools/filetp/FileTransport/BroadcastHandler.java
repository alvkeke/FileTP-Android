package com.alvkeke.tools.filetp.FileTransport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

class BroadcastHandler {

    private String mUsername;
    private DatagramSocket mSocket;
    private int mBroadPort;
    private BroadcastCallback mCallback;
    private boolean inLoop;

    BroadcastHandler(String username, BroadcastCallback callback){
        mUsername = username;
        mCallback = callback;
    }

    boolean startListen(int port){

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

                    // cmd(30) + username
                    String data = new String(packet.getData()).trim();
                    if (data.length() <= 30){
                        continue;   // 防止字符串过短而导致程序关闭
                    }
                    String cmd = data.substring(0, 30);
                    String username = data.substring(30);

                    InetAddress remoteAddr = packet.getAddress();

                    // todo:此处应该判断地址，而不应该判断用户名，修改。这是程序薄弱之处。
                    if (username.equals(mUsername)){
                        continue;
                    }

                    switch (cmd) {
                        case Cs.CMD_LOGIN_STR:
                            mCallback.gotClientOffline(username);
                            mCallback.gotClientOnline(username, remoteAddr);
                            break;
                        case Cs.CMD_LOGOUT_STR:
                            mCallback.gotClientOffline(username);
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

    void exit(){
        inLoop = false;
    }

    void broadcast(){
        // complete the method that broadcast this client's msg to other client
        String strSend = Cs.CMD_LOGIN_STR + mUsername;
        byte[] data = strSend.getBytes();
        try {
            InetAddress address = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(data, data.length, address, mBroadPort);
            mSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void requestBroadcast(){

        // complete the method that broadcast this client's msg to other client
        String strSend = Cs.CMD_BROADCAST_REQUEST + mUsername;
        byte[] data = strSend.getBytes();
        try {
            InetAddress address = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(data, data.length, address, mBroadPort);
            mSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
