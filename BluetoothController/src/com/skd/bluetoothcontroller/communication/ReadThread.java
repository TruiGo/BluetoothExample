package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.skd.bluetoothcontroller.MainActivity.IncomingMessageHandler;

public class ReadThread extends CommunicationThread {

	private final static String tag = ReadThread.class.getSimpleName();

	private final static int READ_BUFFER_SIZE = 4096;

	private InputStream mInput;
	private IncomingMessageHandler mHandler;
	private byte[] mBuffer;

	public ReadThread(BluetoothSocket mSocket, IncomingMessageHandler handler) throws IOException {
		super(mSocket);
		mInput = mSocket.getInputStream();
		mHandler = handler;
		mBuffer = new byte[READ_BUFFER_SIZE];
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				
				int count = mInput.read(mBuffer);
				String message = new String(Arrays.copyOfRange(mBuffer, 0, count));
				Log.d(tag, "Reader <<< " + message);
				mHandler.obtainMessage(0, message).sendToTarget();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Log.d(tag, "Read thread was interrupted - closing stream.");

		try {
			mInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
