package com.alvkeke.tools.filetp.FileTransport;

public interface FileRecvCallback {
    boolean isCredible(String deviceName);
    void recvFileBegin();
    void recvFileInProcess();
    void gotFile(String fileLocation);
    void recvFileFailed(byte Reason, String param);
}
