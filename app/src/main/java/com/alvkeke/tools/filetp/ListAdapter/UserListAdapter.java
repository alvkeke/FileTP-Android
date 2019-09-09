package com.alvkeke.tools.filetp.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvkeke.tools.filetp.R;

import java.net.InetAddress;
import java.util.ArrayList;

public class UserListAdapter extends BaseAdapter {

    private ArrayList<UserItem> mOnlineUsers;
    private LayoutInflater mInflater;

    private int currentSelectPos;

    public UserListAdapter(Context context){
        mOnlineUsers = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    public void setCurrentSelectPos(int pos){
        currentSelectPos = pos;
    }

    @Override
    public int getCount() {
        return mOnlineUsers.size();
    }

    @Override
    public UserItem getItem(int position) {

        return mOnlineUsers.get(position);
    }

    public String getName(int pos){

        UserItem item = mOnlineUsers.get(pos);
        if (item != null){
            return item.getDeviceName();
        }
        else {
            return null;
        }
    }

    public InetAddress getSelectAddress(){
        if (getCount() <= currentSelectPos){
            return null;
        }
        UserItem item = mOnlineUsers.get(currentSelectPos);
        if (item != null){
            return item.getAddress();
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
            holder.icon = convertView.findViewById(R.id.tasks_icon);
            holder.name = convertView.findViewById(R.id.tasks_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserItem item = mOnlineUsers.get(position);

        if (item != null){
            holder.name.setText(item.getDeviceName());
            if (position == currentSelectPos){
                holder.icon.setImageResource(R.drawable.ic_file_list_checked);
            }else {
                holder.icon.setImageResource(R.drawable.ic_file_list_unchecked);
            }
        }

        return convertView;
    }

    public void addUser(String deviceName, InetAddress address) {
        mOnlineUsers.add(new UserItem(address, deviceName));
    }

    public void removeUser(String deviceName) {

        for (UserItem u : mOnlineUsers){
            if (u.getDeviceName().endsWith(deviceName)){
                mOnlineUsers.remove(u);
                return;
            }
        }
    }
}
