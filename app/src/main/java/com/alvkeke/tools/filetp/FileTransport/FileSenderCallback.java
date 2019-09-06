package com.alvkeke.tools.filetp.FileTransport;

import java.io.File;

public interface FileSenderCallback {
    void sendFileFailed(File file);
    void sendFileSuccess(File file);
    void sendFileInProcess();
}
