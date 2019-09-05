package com.alvkeke.tools.filetp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {

    private ArrayList<File> mFileList;
    private LayoutInflater mInflater;

    FileListAdapter(Context context, ArrayList<File> fileList){
        mFileList = fileList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
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
            convertView = mInflater.inflate(R.layout.file_view_item_layout, null);

            holder.icon = convertView.findViewById(R.id.file_view_icon);
            holder.name = convertView.findViewById(R.id.file_view_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        File file = mFileList.get(position);

        if (file == null || !file.exists()){
            return null;
        }

        holder.name.setText(file.getName());
        if (file.isDirectory()){
            holder.icon.setBackgroundColor(Color.BLUE);
        } else {
            holder.icon.setBackgroundColor(Color.GRAY);
        }


        return convertView;
    }
}
