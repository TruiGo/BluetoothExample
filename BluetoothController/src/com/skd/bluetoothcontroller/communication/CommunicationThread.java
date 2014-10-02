package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.skd.bluetoothcontroller.MainActivity.IncomingMessageHandler;

public class CommunicationThread extends Thread {

	private final static String tag = CommunicationThread.class.getSimpleName();

	private InputStream mInput;
	private OutputStream mOutput;

	private IncomingMessageHandler mHandler;

	private Queue<String> mMessageQueue;
	private byte[] mBuffer = new byte[4096];

	public CommunicationThread(BluetoothSocket mSocket, IncomingMessageHandler handler) throws IOException {
		mHandler = handler;
		mInput = mSocket.getInputStream();
		mOutput = mSocket.getOutputStream();
		mMessageQueue = new LinkedList<String>();
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				tryRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			mInput.close();
			mOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void tryRead() throws IOException {
		int count = mInput.read(mBuffer);
		String message = new String(Arrays.copyOfRange(mBuffer, 0, count));
		Log.d(tag, "Reader >>> " + message);
		mHandler.obtainMessage(0, message).sendToTarget();
	}

	private void tryWrite() throws IOException {
		String message = mMessageQueue.poll();
		if (message != null) {
			mOutput.write(message.getBytes());
			mOutput.flush();
			Log.i(tag, "Writer <<< " + message);
		}
	}

	public synchronized void addMessageToQueue(String message) {
		mMessageQueue.add(message);
		try {
			tryWrite();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
