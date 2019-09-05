package com.alvkeke.tools.filetp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileListAdapter extends BaseAdapter {

    private ArrayList<File> mDirList;
    private ArrayList<File> mFileList;
    private LayoutInflater mInflater;

    private boolean showHideFile;

    FileListAdapter(Context context, ArrayList<File> dirList, ArrayList<File> fileList){
        mDirList = dirList;
        mFileList = fileList;
        mInflater = LayoutInflater.from(context);
    }

    boolean setPath(File path){

        if (!path.exists()){
            return false;
        }
        boolean isPath = path.isDirectory();

        if (isPath){
            File[] children = path.listFiles();
            if (children == null) return false;
            for (File e : children){
                if (e.isDirectory()){
                    mDirList.add(e);
                } else if (e.isFile()){
                    mFileList.add(e);
                }
            }
        }

        return isPath;
    }

    public void setShowHideFile(boolean showHideFile) {
        this.showHideFile = showHideFile;
    }

    public void rankList(){

        // todo: rank directories and files by name
        Collections.sort(mDirList);
        Collections.sort(mFileList);
    }

    @Override
    public int getCount() {
        return mFileList.size() + mDirList.size();
    }

    @Override
    public File getItem(int position) {
        if (position < mDirList.size()) {
            return mDirList.get(position);
        }else {
            return mFileList.get(position - mDirList.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        ImageView icon;
        TextView name;
        View divider;
    }

    private void setVisible(View convertView, ViewHolder holder, int Visibility){
        convertView.setVisibility(Visibility);
        holder.icon.setVisibility(Visibility);
        holder.name.setVisibility(Visibility);
        holder.divider.setVisibility(Visibility);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.file_view_item_layout, null);

            holder.icon = convertView.findViewById(R.id.file_view_icon);
            holder.name = convertView.findViewById(R.id.file_view_name);
            holder.divider = convertView.findViewById(R.id.file_view_divider);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        File file = getItem(position);

        if (file == null || !file.exists()){
            setVisible(convertView, holder, View.GONE);
            return convertView;
        }

        if (!showHideFile && file.getName().substring(0,1).equals(".")){
            setVisible(convertView, holder, View.GONE);
            return convertView;
        }

        holder.name.setText(file.getName());
        if (file.isDirectory()){
            holder.icon.setImageResource(R.drawable.ic_folder);
        } else {
            holder.icon.setImageResource(R.drawable.ic_file);
        }

        setVisible(convertView, holder, View.VISIBLE);
        return convertView;
    }
}
