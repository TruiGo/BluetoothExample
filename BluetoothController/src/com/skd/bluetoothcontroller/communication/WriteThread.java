package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class WriteThread extends CommunicationThread {

	private final static String tag = WriteThread.class.getSimpleName();

	private OutputStream mOutput;
	private Queue<String> mMessageQueue;

	public WriteThread(BluetoothSocket mSocket) throws IOException {
		super(mSocket);
		mOutput = mSocket.getOutputStream();
		mMessageQueue = new LinkedList<String>();
	}

	public synchronized void addMessageToQueue(String message) {
		mMessageQueue.add(message);
	}

	@Override
	public void run() {
		while (!isInterrupted()) {

			String message = mMessageQueue.poll();
			if (message != null) {
				try {
					mOutput.write(message.getBytes());
					mOutput.flush();

					Log.i(tag, "Writer <<< " + message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Log.i(tag, "Write thread was interrupted - close stream.");

		try {
			mOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
