package com.alvkeke.tools.filetp.FileTransport;

import com.alvkeke.tools.filetp.ListAdapter.TaskItem;

import java.io.File;

public interface FileSenderCallback {
    void sendFileFailed(TaskItem task);
    void sendFileSuccess(TaskItem task);
    void sendFileInProcess();
}
