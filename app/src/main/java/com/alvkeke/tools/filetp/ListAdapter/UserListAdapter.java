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
import java.util.HashMap;

public class UserListAdapter extends BaseAdapter {

//    private HashMap<String, InetAddress> mOnlineUsers;
    private ArrayList<UserItem> mOnlineUsers;
    private LayoutInflater mInflater;

//    private String currentTargetDevice;
    private int currentSelectPos;

    public UserListAdapter(Context context){
//        mOnlineUsers = new HashMap<>();
        mOnlineUsers = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

//    public void setCurrentTargetDevice(String name){
//        currentTargetDevice = name;
//    }

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
//        int i = 0;
//        for (InetAddress address : mOnlineUsers.values()){
//            if (i++ == position){
//                return address;
//            }
//        }
//
//        return null;
    }

    public String getName(int pos){

        UserItem item = mOnlineUsers.get(pos);
        if (item != null){
            return item.getDeviceName();
        }
        else {
            return null;
        }
//        int i = 0;
//        for (String s : mOnlineUsers.keySet()){
//            if (i++ == pos){
//                return s;
//            }
//        }
//
//        return null;
    }

    public InetAddress getSelectAddress(){
//        if (currentTargetDevice == null){
//            return null;
//        }
//        return mOnlineUsers.get(currentTargetDevice);
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

//        String name = getName(position);

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
//        mOnlineUsers.put(deviceName, address);
        mOnlineUsers.add(new UserItem(address, deviceName));
    }

    public void removeUser(String deviceName) {
//        mOnlineUsers.remove(deviceName);

        for (UserItem u : mOnlineUsers){
            if (u.getDeviceName().endsWith(deviceName)){
                mOnlineUsers.remove(u);
                return;
            }
        }
    }
}
