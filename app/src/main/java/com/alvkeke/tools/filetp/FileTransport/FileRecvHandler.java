package com.alvkeke.tools.filetp.FileTransport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class FileRecvHandler {

    private ServerSocket serverSocket;
    private boolean inLoop;
    private FileRecvCallback mCallback;

    FileRecvHandler(FileRecvCallback callback){
        mCallback = callback;
    }

    boolean startListen(int port){

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
                try {   //todo: complete the method
                    Socket socketAccept = serverSocket.accept();
                    System.out.println("got an client want to send file.");
                    new Thread(new FileRecvThread(mCallback, socketAccept)).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void exit(){
        inLoop = false;
    }

}
