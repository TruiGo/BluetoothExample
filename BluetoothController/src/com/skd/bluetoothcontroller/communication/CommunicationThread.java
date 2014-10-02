package com.skd.bluetoothcontroller.communication;

import android.bluetooth.BluetoothSocket;

public class CommunicationThread extends Thread {
	
	protected BluetoothSocket mSocket;

	public CommunicationThread(BluetoothSocket mSocket) {
		this.mSocket = mSocket;
	}
	
	
}
