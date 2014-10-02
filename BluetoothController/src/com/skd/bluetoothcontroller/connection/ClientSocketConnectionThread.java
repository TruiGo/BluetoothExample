package com.skd.bluetoothcontroller.connection;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ClientSocketConnectionThread extends ConnectionThread {

	private SocketConnectionListener mListener;

	private BluetoothSocket mSocket;

	public ClientSocketConnectionThread(BluetoothDevice device, SocketConnectionListener socketConnectionListener) {
		mListener = socketConnectionListener;
		try {
			mSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
		} catch (IOException e) {
			e.printStackTrace();
			mListener.onSocketConnectionFailed(e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			mSocket.connect();
			mListener.onSocketAcquired(mSocket);
		} catch (IOException e) {
			e.printStackTrace();

			try {
				mSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			mListener.onSocketConnectionFailed(e.getMessage());
		}
	}

	public void cancel() {
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
