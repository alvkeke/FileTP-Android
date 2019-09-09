package com.alvkeke.tools.filetp.fileTransport;

import java.net.InetAddress;

public interface BroadcastCallback {
    void gotClientOnline(String deviceName, InetAddress address);
    void gotClientOffline(String deviceName);
}
