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
import java.util.HashMap;

public class OnlineListAdapter extends BaseAdapter {

    private HashMap<String, InetAddress> mOnlineUsers;
    private LayoutInflater mInflater;

    private String currentTargetDevice;

    public OnlineListAdapter(Context context){
        mOnlineUsers = new HashMap<>();
        mInflater = LayoutInflater.from(context);
    }

    public void setCurrentTargetDevice(String name){
        currentTargetDevice = name;
    }

    @Override
    public int getCount() {
        return mOnlineUsers.size();
    }

    @Override
    public Object getItem(int position) {

        int i = 0;
        for (InetAddress address : mOnlineUsers.values()){
            if (i++ == position){
                return address;
            }
        }

        return null;
    }

    public String getName(int pos){

        int i = 0;
        for (String s : mOnlineUsers.keySet()){
            if (i++ == pos){
                return s;
            }
        }

        return null;
    }

    public InetAddress getSelectAddress(){
        if (currentTargetDevice == null){
            return null;
        }
        return mOnlineUsers.get(currentTargetDevice);
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

        String name = getName(position);

        if (name != null){
            holder.name.setText(name);
            if (name.equals(currentTargetDevice)){
                holder.icon.setImageResource(R.drawable.ic_checked);
            }else {
                holder.icon.setImageResource(R.drawable.ic_unchecked);
            }
        }

        return convertView;
    }

    public void addUser(String deviceName, InetAddress address) {
        mOnlineUsers.put(deviceName, address);
    }

    public void removeUser(String deviceName) {
        mOnlineUsers.remove(deviceName);
    }
}
