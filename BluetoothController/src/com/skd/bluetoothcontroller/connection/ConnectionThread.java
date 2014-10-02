package com.skd.bluetoothcontroller.connection;

import java.util.UUID;

import android.bluetooth.BluetoothSocket;

public class ConnectionThread extends Thread {
	
	public interface SocketConnectionListener {
		void onSocketAcquired(BluetoothSocket socket);
		void onSocketConnectionFailed(String message);
	}
	
	public final static String SDP_SERVICE_NAME = "BluetoothController";
//	public final static UUID SERVICE_UUID = UUID.fromString("7f520440-496a-11e4-916c-0800200c9a66");
	public final static UUID SERVICE_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb"); // for htc phones

}
