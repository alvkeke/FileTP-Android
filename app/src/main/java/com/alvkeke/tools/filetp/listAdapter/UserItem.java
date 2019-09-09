package com.alvkeke.tools.filetp.listAdapter;

import java.net.InetAddress;

public class UserItem {

    private InetAddress mAddress;
    private String mDeviceName;

    public UserItem(InetAddress address, String deviceName){
        mAddress = address;
        mDeviceName = deviceName;
    }

    public InetAddress getAddress(){
        return mAddress;
    }

    public String getDeviceName(){
        return mDeviceName;
    }

}
