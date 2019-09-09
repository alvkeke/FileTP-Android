package com.alvkeke.tools.filetp.ListAdapter;

import java.io.File;

public class TaskItem extends File {

    static final int STATE_WAITING = 0;
    static final int STATE_RUNNING = 1;
    static final int STATE_FINISHED = 2;
    static final int STATE_ERROR = 4;

    private int state;

    public TaskItem(String pathname) {
        super(pathname);
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

}
