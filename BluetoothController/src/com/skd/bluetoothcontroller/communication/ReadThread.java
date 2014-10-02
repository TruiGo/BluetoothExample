package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ReadThread extends CommunicationThread {
	
	private final static String tag = ReadThread.class.getSimpleName();
	private final static int READ_BUFFER_SIZE = 4096;

	private InputStream mInput;
	
	public ReadThread(BluetoothSocket mSocket) {
		super(mSocket);
		try {
			mInput = mSocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if(mInput != null){
			byte[] buffer = new byte[READ_BUFFER_SIZE];
			for(int i = 0; i < MESSAGES_COUNT; i++){
				try {
					int count = mInput.read(buffer);
					Log.i(tag, "Reader >>> " + new String(Arrays.copyOfRange(buffer, 0, count)));
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			try {
				mInput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
