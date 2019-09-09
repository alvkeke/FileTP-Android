package com.alvkeke.tools.filetp.FileTransport;

import com.alvkeke.tools.filetp.ListAdapter.TaskItem;

public interface FileSenderCallback {
    void sendFileFailed(TaskItem task);
    void sendFileSuccess(TaskItem task);
    void sendFileInProcess(TaskItem task, float percentage);
}
