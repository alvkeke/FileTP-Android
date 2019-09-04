package com.alvkeke.tools.filetp.FileTransport;

import java.net.InetAddress;

public interface BroadcastCallback {
    void gotClientOnline(String user, InetAddress address);
    void gotClientOffline(String user);
}
