package com.alvkeke.tools.filetp.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.alvkeke.tools.filetp.FileTransport.FileSender;
import com.alvkeke.tools.filetp.FileTransport.FileSendCallback;
import com.alvkeke.tools.filetp.R;

import java.net.InetAddress;
import java.util.ArrayList;

public class SendTaskListAdapter extends BaseAdapter {

    private ArrayList<SendTaskItem> mTasksList;
    private int mAllowThreadNumber;

    private FileSendCallback mCallback;

    private LayoutInflater mInflater;

    public SendTaskListAdapter(Context context, FileSendCallback callback){

        mInflater = LayoutInflater.from(context);

        mCallback = callback;

        mTasksList = new ArrayList<>();

    }

    public void setAllowThreadNumber(int mAllowThreadNumber) {
        this.mAllowThreadNumber = mAllowThreadNumber;
    }

    public void addTask(SendTaskItem task){
        task.setState(SendTaskItem.STATE_WAITING);
        mTasksList.add(task);
    }

    public void finishTask(SendTaskItem task){

        task.setState(SendTaskItem.STATE_FINISHED);
    }

    public void setTaskError(SendTaskItem task){

        task.setState(SendTaskItem.STATE_ERROR);
    }

    public void clearFinishedTask(){
        ArrayList<SendTaskItem> taskItems = new ArrayList<>();
        for (SendTaskItem t : mTasksList){
            if (t.getState() == SendTaskItem.STATE_FINISHED){
                taskItems.add(t);
            }
        }

        mTasksList.removeAll(taskItems);
    }

    public void removeTask(SendTaskItem task){

        mTasksList.remove(task);
    }

    private int getTasksCountByState(int state){
        int count = 0;
        for (SendTaskItem t : mTasksList){
            if (t.getState() == state){
                count++;
            }
        }

        return count;
    }

    public int getFinishedTaskCount(){
        return getTasksCountByState(SendTaskItem.STATE_FINISHED);
    }

    public int getWaitingTasksCount(){
        return getTasksCountByState(SendTaskItem.STATE_WAITING);
    }

    public int getRunningTasksCount(){
        return getTasksCountByState(SendTaskItem.STATE_RUNNING);
    }

    public void checkWaitingTasks(InetAddress targetAddress, int port, String localDeviceName){

        for (SendTaskItem t : mTasksList){
            if (getRunningTasksCount() >= mAllowThreadNumber && mAllowThreadNumber>0){
                break;
            }
            if (t.getState() == SendTaskItem.STATE_WAITING){
                FileSender sender = new FileSender(mCallback, localDeviceName, targetAddress, port);
                t.setState(SendTaskItem.STATE_RUNNING);
                sender.send(t);
            }
        }
    }

    @Override
    public int getCount() {
        return mTasksList.size();
    }

    @Override
    public SendTaskItem getItem(int position) {
        return mTasksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.task_item_layout, null);

            holder.icon = convertView.findViewById(R.id.tasks_icon);
            holder.name = convertView.findViewById(R.id.tasks_name);
            holder.progress = convertView.findViewById(R.id.tasks_progress);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SendTaskItem task = getItem(position);

        holder.name.setText(task.getName());

        switch (task.getState()){
            case SendTaskItem.STATE_WAITING:
                holder.icon.setImageResource(R.drawable.ic_task_waiting);
                break;
            case SendTaskItem.STATE_RUNNING:
                holder.icon.setImageResource(R.drawable.ic_task_send);
                break;
            case SendTaskItem.STATE_ERROR:
                holder.icon.setImageResource(R.drawable.ic_task_error);
                break;
            case SendTaskItem.STATE_FINISHED:
                holder.icon.setImageResource(R.drawable.ic_task_finished);
                break;
        }

        holder.progress.setProgress((int)task.getProgress());

        return convertView;
    }

}
