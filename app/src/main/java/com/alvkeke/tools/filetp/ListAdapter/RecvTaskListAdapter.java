package com.alvkeke.tools.filetp.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.alvkeke.tools.filetp.FileTransport.FileRecvCallback;
import com.alvkeke.tools.filetp.R;

import java.util.ArrayList;

public class RecvTaskListAdapter extends BaseAdapter {

    private ArrayList<RecvTaskItem> mTasksList;

    private FileRecvCallback mCallback;

    private LayoutInflater mInflater;

    public RecvTaskListAdapter(Context context, FileRecvCallback callback){

        mInflater = LayoutInflater.from(context);

        mCallback = callback;

        mTasksList = new ArrayList<>();

    }

    public void addTask(RecvTaskItem task){
        task.setState(RecvTaskItem.STATE_RUNNING);
        mTasksList.add(task);
    }

    public void finishTask(RecvTaskItem task){

        task.setState(RecvTaskItem.STATE_FINISHED);
    }

    public void setTaskError(RecvTaskItem task){

        task.setState(RecvTaskItem.STATE_ERROR);
    }

    public void clearFinishedTask(){
        ArrayList<RecvTaskItem> taskItems = new ArrayList<>();
        for (RecvTaskItem t : mTasksList){
            if (t.getState() == RecvTaskItem.STATE_FINISHED){
                taskItems.add(t);
            }
        }

        mTasksList.removeAll(taskItems);
    }

    public void removeTask(RecvTaskItem task){

        mTasksList.remove(task);
    }

    private int getTasksCountByState(int state){
        int count = 0;
        for (RecvTaskItem t : mTasksList){
            if (t.getState() == state){
                count++;
            }
        }

        return count;
    }

    public int getFinishedTaskCount(){
        return getTasksCountByState(RecvTaskItem.STATE_FINISHED);
    }

    public int getRunningTasksCount(){
        return getTasksCountByState(RecvTaskItem.STATE_RUNNING);
    }

    @Override
    public int getCount() {
        return mTasksList.size();
    }

    @Override
    public RecvTaskItem getItem(int position) {
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

        RecvTaskItem task = getItem(position);

        String title = "[" + task.getDeviceName() + "]: " + task.getFilename();
        holder.name.setText(title);

        switch (task.getState()){
            case RecvTaskItem.STATE_RUNNING:
                holder.icon.setImageResource(R.drawable.ic_task_receive);
                break;
            case RecvTaskItem.STATE_ERROR:
                holder.icon.setImageResource(R.drawable.ic_task_error);
                break;
            case RecvTaskItem.STATE_FINISHED:
                holder.icon.setImageResource(R.drawable.ic_task_finished);
                break;
        }

        holder.progress.setProgress((int)task.getProgress());

        return convertView;
    }

}
