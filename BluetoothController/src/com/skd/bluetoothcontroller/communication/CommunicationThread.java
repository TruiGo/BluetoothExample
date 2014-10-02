package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class CommunicationThread extends Thread {
	
	protected BluetoothSocket mSocket;
	
	private InputStream mInput;
	private OutputStream mOutput;

	public CommunicationThread(BluetoothSocket mSocket) throws IOException {
		mInput = mSocket.getInputStream();
		mOutput = mSocket.getOutputStream();
	}

	@Override
	public void run() {
		while(!isInterrupted()){
			
		}
	}
}
