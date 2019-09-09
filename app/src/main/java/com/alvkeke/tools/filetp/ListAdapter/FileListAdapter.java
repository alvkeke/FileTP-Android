package com.alvkeke.tools.filetp.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvkeke.tools.filetp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FileListAdapter extends BaseAdapter {

    private File mCurrentPath;
    private ArrayList<File> mDirList;
    private ArrayList<File> mFileList;
    private LayoutInflater mInflater;

    private Set<Integer> mSelectItems;

    private boolean showHideFile;

    public FileListAdapter(Context context){
        mDirList = new ArrayList<>();
        mFileList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);

        mSelectItems = new HashSet<>();
    }

    public boolean setPath(File path){

        if (!path.exists()){
            return false;
        }
        boolean isPath = path.isDirectory();

        if (isPath){
            File[] children = path.listFiles();
            if (children == null) return false;
            mCurrentPath = path;
            mDirList.clear();
            mFileList.clear();
            for (File e : children){
                if (e.isDirectory()){
                    mDirList.add(e);
                } else if (e.isFile()){
                    mFileList.add(e);
                }
            }
        }

        rankList();

        return isPath;
    }

    public boolean isSelected(int pos){
        return mSelectItems.contains(pos);
    }

    public void selectItem(int pos){
        if (!isSelected(pos))
            mSelectItems.add(pos);
    }

    public void unselectItem(int pos){
        mSelectItems.remove(pos);
    }

    public void toggleSelectState(int pos){
        if (mSelectItems.contains(pos)){
            mSelectItems.remove(pos);
        } else {
            mSelectItems.add(pos);
        }

    }

    public boolean isSelectAll(){
        return mSelectItems.size() == mFileList.size();
    }

    public void selectAll(){
        for (int i = getDirsCount(); i<getCount(); i++){
            selectItem(i);
        }
    }

    public void unselectAll(){
        mSelectItems.clear();
    }

    public boolean hasSelected(){
        return !mSelectItems.isEmpty();
    }

    public Set<File> getSelectFiles(){
        Set<File> set = new HashSet<>();
        for (int i : mSelectItems){
            set.add(getItem(i));
        }

        return set;
    }

    public boolean moveToLastPath(){
        return setPath(mCurrentPath.getParentFile());
    }

    public void setShowHideFile(boolean showHideFile) {
        this.showHideFile = showHideFile;
    }

    public void rankList(){

        // rank directories and files by name
        Collections.sort(mDirList);
        Collections.sort(mFileList);
    }

    @Override
    public int getCount() {
        return mFileList.size() + mDirList.size();
    }

    public int getDirsCount(){
        return mDirList.size();
    }

    public int getFielsCount(){
        return mFileList.size();
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
        ImageView checker;
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
            holder.checker = convertView.findViewById(R.id.file_view_check);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        File file = getItem(position);

        if (file == null || !file.exists()){
            setVisible(convertView, holder, View.GONE);
            return convertView;
        }

        if (!showHideFile && file.isHidden()){
            setVisible(convertView, holder, View.GONE);
            return convertView;
        }

        holder.checker.setVisibility(View.GONE);
        holder.name.setText(file.getName());
        if (file.isDirectory()){
            holder.icon.setImageResource(R.drawable.ic_file_list_folder);
        } else {
            holder.icon.setImageResource(R.drawable.ic_file_list_file);
            if (hasSelected()){
                holder.checker.setVisibility(View.VISIBLE);
            }
        }

        if (isSelected(position)){
            holder.checker.setImageResource(R.drawable.ic_file_list_checked);
        } else {
            holder.checker.setImageResource(R.drawable.ic_file_list_unchecked);
        }

        setVisible(convertView, holder, View.VISIBLE);
        return convertView;
    }
}
