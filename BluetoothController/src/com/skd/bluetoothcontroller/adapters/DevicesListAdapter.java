package com.skd.bluetoothcontroller.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DevicesListAdapter extends BaseAdapter {
	
	private List<BluetoothDevice> mDevices;
	
	public DevicesListAdapter() {
		mDevices = new ArrayList<BluetoothDevice>();
	}
	
	public void addDevices(Collection<BluetoothDevice> devices){
		mDevices.addAll(devices);
		notifyDataSetChanged();
	}
	
	public void addDevice(BluetoothDevice device){
		mDevices.add(device);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mDevices != null ? mDevices.size() : 0;
	}

	@Override
	public BluetoothDevice getItem(int position) {
		return mDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		
		BluetoothDevice device = getItem(position);
		((TextView)convertView).setText(device.getName());
		
		return convertView;
	}

}
