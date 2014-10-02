package com.skd.bluetoothcontroller.communication;

import android.bluetooth.BluetoothSocket;

public class CommunicationThread extends Thread {
	
	protected final static int MESSAGES_COUNT = 10;

	protected BluetoothSocket mSocket;

	public CommunicationThread(BluetoothSocket mSocket) {
		this.mSocket = mSocket;
	}
	
	
}
