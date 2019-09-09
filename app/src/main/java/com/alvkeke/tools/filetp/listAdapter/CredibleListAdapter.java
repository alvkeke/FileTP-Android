package com.alvkeke.tools.filetp.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvkeke.tools.filetp.R;

import java.util.Set;

public class CredibleListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Set<String> mUserList;

    public CredibleListAdapter(Context context, Set<String> userList){
        mInflater = LayoutInflater.from(context);
        mUserList = userList;
    }

    public void addUser(String name){
        mUserList.add(name);
    }

    public void delUser(String name){
        for (String s: mUserList){
            if (s.equals(name)){
                mUserList.remove(s);
                return;
            }
        }
    }

    public void clear(){
        mUserList.clear();
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public String getItem(int position) {

        for (String s : mUserList){
            if (position == 0){
                return s;
            }
            position--;
        }

        return null;
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
            convertView = mInflater.inflate(R.layout.user_item_layout, null);
            holder.name = convertView.findViewById(R.id.users_name);
            holder.icon = convertView.findViewById(R.id.users_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setVisibility(View.GONE);


        String name = getItem(position);
        if (name != null) {
            holder.name.setText(name);
        }

        return convertView;
    }

    public Set<String> getList(){
        return mUserList;
    }
}
