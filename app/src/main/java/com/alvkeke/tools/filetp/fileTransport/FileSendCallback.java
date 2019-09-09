package com.alvkeke.tools.filetp.fileTransport;

import com.alvkeke.tools.filetp.listAdapter.SendTaskItem;

public interface FileSendCallback {
    void sendFileFailed(SendTaskItem task);
    void sendFileSuccess(SendTaskItem task);
    void sendFileInProcess(SendTaskItem task, float percentage);
}
