package com.alvkeke.tools.filetp.listAdapter;

public class RecvTaskItem{

    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_ERROR = 3;

    private float mPercentage;

    private String mDeviceName, mFilename;
    private long mFileLength;
    private int mState;

    public RecvTaskItem(String deviceName, String filename, long fileLength){
        mDeviceName = deviceName;
        mFilename = filename;
        mFileLength = fileLength;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public int getState() {
        return mState;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getFilename() {
        return mFilename;
    }

    public long getFileLength() {
        return mFileLength;
    }

    public void setProgress(float percentage){
        mPercentage = percentage;
    }

    public float getProgress(){
        return mPercentage;
    }

}
