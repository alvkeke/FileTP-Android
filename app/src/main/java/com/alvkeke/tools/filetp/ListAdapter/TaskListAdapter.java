package com.alvkeke.tools.filetp.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvkeke.tools.filetp.FileTransport.FileSender;
import com.alvkeke.tools.filetp.FileTransport.FileSenderCallback;
import com.alvkeke.tools.filetp.R;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;

public class TaskListAdapter extends BaseAdapter {

    private ArrayList<TaskItem> mTasksList;
    private int mAllowThreadNumber;

    private FileSenderCallback mCallback;

    private LayoutInflater mInflater;

    public TaskListAdapter(Context context, FileSenderCallback callback){

        mInflater = LayoutInflater.from(context);

        mCallback = callback;

        mTasksList = new ArrayList<>();

    }

    public void setAllowThreadNumber(int mAllowThreadNumber) {
        this.mAllowThreadNumber = mAllowThreadNumber;
    }

    public void addTask(TaskItem task){
//        TaskItem task = new TaskItem(file);
        task.setState(TaskItem.STATE_WAITING);
        mTasksList.add(task);
    }

    public void finishTask(TaskItem task){
//        for (TaskItem t : mTasksList){
//            if (t.equals(task)){
//                t.setState(TaskItem.STATE_FINISHED);
//                return;
//            }
//        }
        task.setState(TaskItem.STATE_FINISHED);
    }

    public void setTaskError(TaskItem task){

//        for (TaskItem t : mTasksList){
//            if (t.getFile().equals(file)){
//                t.setState(TaskItem.STATE_ERROR);
//                return;
//            }
//        }

        task.setState(TaskItem.STATE_ERROR);
    }

    public void clearFinishedTask(){
        ArrayList<TaskItem> taskItems = new ArrayList<>();
        for (TaskItem t : mTasksList){
            if (t.getState() == TaskItem.STATE_FINISHED){
                taskItems.add(t);
            }
        }

        mTasksList.removeAll(taskItems);
    }

    public void removeTask(TaskItem task){
//        TaskItem tmp = null;
//        for (TaskItem t : mTasksList){
//            if (t.getFile().equals(file)){
//                tmp = t;
//                break;
//            }
//        }
//        if (tmp != null)
//        mTasksList.remove(tmp);
        mTasksList.remove(task);
    }

    private int getTasksCountByState(int state){
        int count = 0;
        for (TaskItem t : mTasksList){
            if (t.getState() == state){
                count++;
            }
        }

        return count;
    }

    public int getFinishedTaskCount(){
        return getTasksCountByState(TaskItem.STATE_FINISHED);
    }

    public int getWaitingTasksCount(){
        return getTasksCountByState(TaskItem.STATE_WAITING);
    }

    public int getRunningTasksCount(){
        return getTasksCountByState(TaskItem.STATE_RUNNING);
    }

    public void checkWaitingTasks(InetAddress targetAddress, int port, String localDeviceName){

        for (TaskItem t : mTasksList){
            if (getRunningTasksCount() >= mAllowThreadNumber){
                break;
            }
            if (t.getState() == TaskItem.STATE_WAITING){
                FileSender sender = new FileSender(mCallback, localDeviceName, targetAddress, port);
                t.setState(TaskItem.STATE_RUNNING);
//                TaskItem fileToSend = t;
                sender.send(t);
            }
        }
    }

    @Override
    public int getCount() {
        return mTasksList.size();
    }

    @Override
    public TaskItem getItem(int position) {
        return mTasksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        ImageView icon;
        TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.drawer_list_item_layout, null);

            holder.icon = convertView.findViewById(R.id.tasks_icon);
            holder.name = convertView.findViewById(R.id.tasks_name);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TaskItem task = getItem(position);

        holder.name.setText(task.getName());

        switch (task.getState()){
            case TaskItem.STATE_WAITING:
                holder.icon.setImageResource(R.drawable.ic_waiting);
                break;
            case TaskItem.STATE_RUNNING:
                holder.icon.setImageResource(R.drawable.ic_transporting);
                break;
            case TaskItem.STATE_ERROR:
                holder.icon.setImageResource(R.drawable.ic_error);
                break;
            case TaskItem.STATE_FINISHED:
                holder.icon.setImageResource(R.drawable.ic_finished);
                break;

        }

        return convertView;
    }
//
//    @Override
//    public void sendFileFailed(TaskItem task) {
//        mCallback.sendFileFailed(task);
//    }
//
//    @Override
//    public void sendFileSuccess(TaskItem task) {
//        mCallback.sendFileSuccess(task);
//    }
//
//    @Override
//    public void sendFileInProcess() {
//        mCallback.sendFileInProcess();
//    }
}
