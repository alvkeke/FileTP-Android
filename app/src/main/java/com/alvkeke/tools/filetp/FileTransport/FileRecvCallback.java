package com.alvkeke.tools.filetp.FileTransport;

import com.alvkeke.tools.filetp.ListAdapter.RecvTaskItem;

public interface FileRecvCallback {
    boolean isCredible(String deviceName);
    RecvTaskItem recvFileBegin(String deviceName, String fileName, long fileLength);
    void recvFileInProcess(RecvTaskItem task, float percentage);
    void recvFileSuccess(RecvTaskItem task, String fileLocation);
    void recvFileFailed(RecvTaskItem task, byte Reason, String param);
}
