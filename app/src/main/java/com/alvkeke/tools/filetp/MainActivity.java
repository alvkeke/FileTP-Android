package com.alvkeke.tools.filetp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    DatagramSocket socket = new DatagramSocket(10000);

                    while (true) {
                        try {
                            byte[] buf = new byte[1024];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            Log.e("debug", "ready receive");
                            socket.receive(packet);
                            Log.e("debug", "got data");

                            String data = new String(packet.getData()).trim();
                            String sAddr = packet.getAddress().toString();
//                            SocketAddress addr = packet.getSocketAddress();
                            Log.e("data", data);
                            Log.e("addr", sAddr);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }
}
