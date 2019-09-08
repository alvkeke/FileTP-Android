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
import java.util.ArrayList;

public class ProcessListAdapter extends BaseAdapter implements FileSenderCallback {

    private ArrayList<File> mWaitingTasks;
    private ArrayList<File> mRunningTasks;
    private int mAllowThreadNumber;

    private FileSenderCallback mCallback;

    private LayoutInflater mInflater;

    public ProcessListAdapter(Context context, FileSenderCallback callback){

        mInflater = LayoutInflater.from(context);

        mCallback = callback;

        mWaitingTasks = new ArrayList<>();
        mRunningTasks = new ArrayList<>();

    }

    public void setAllowThreadNumber(int mAllowThreadNumber) {
        this.mAllowThreadNumber = mAllowThreadNumber;
    }

    public void addTask(File file){
        mWaitingTasks.add(file);
    }

    public void removeTask(File file){
        mRunningTasks.remove(file);
    }

    public void checkWaitingTasks(InetAddress targetAddress, int port, String localDeviceName){

        ArrayList<File> tmp = new ArrayList<>();
        for (File file : mWaitingTasks){
            if (mRunningTasks.size() >= mAllowThreadNumber && mAllowThreadNumber>0){
                break;
            }

//            FileSender sender = new FileSender(this, localDeviceName, targetAddress, port);
//            sender.send(file);
//            mRunningTasks.add(file);
            tmp.add(file);
        }

        mWaitingTasks.removeAll(tmp);
        for (File e : tmp){
            FileSender sender = new FileSender(this, localDeviceName, targetAddress, port);
            mRunningTasks.add(e);
            sender.send(e);
        }
    }

    @Override
    public int getCount() {
        return mWaitingTasks.size() + mRunningTasks.size();
    }

    @Override
    public File getItem(int position) {
        if (position<mRunningTasks.size())
            return mRunningTasks.get(position);
        else
            return mWaitingTasks.get(position-mRunningTasks.size());
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

            holder.icon = convertView.findViewById(R.id.process_icon);
            holder.name = convertView.findViewById(R.id.process_name);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File file = getItem(position);

        holder.name.setText(file.getName());

        if (position < mRunningTasks.size()){
            holder.icon.setImageResource(R.drawable.ic_transporting);
        } else {
            holder.icon.setImageResource(R.drawable.ic_waiting);
        }

        return convertView;
    }

    @Override
    public void sendFileFailed(File file) {
        mCallback.sendFileFailed(file);
    }

    @Override
    public void sendFileSuccess(File file) {
        mCallback.sendFileSuccess(file);
    }

    @Override
    public void sendFileInProcess() {
        mCallback.sendFileInProcess();
    }
}
