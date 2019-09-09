package com.alvkeke.tools.filetp.FileTransport;

import com.alvkeke.tools.filetp.ListAdapter.SendTaskItem;

public interface FileSendCallback {
    void sendFileFailed(SendTaskItem task);
    void sendFileSuccess(SendTaskItem task);
    void sendFileInProcess(SendTaskItem task, float percentage);
}
