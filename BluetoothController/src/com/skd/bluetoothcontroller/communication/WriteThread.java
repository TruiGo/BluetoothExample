package com.skd.bluetoothcontroller.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import android.bluetooth.BluetoothSocket;
import android.text.format.DateFormat;
import android.util.Log;

public class WriteThread extends CommunicationThread {

	private final static String tag = WriteThread.class.getSimpleName();

	private OutputStream mOutput;

	public WriteThread(BluetoothSocket mSocket) {
		super(mSocket);
		try {
			mOutput = mSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (mOutput != null) {
			for (int i = 0; i < MESSAGES_COUNT; i++) {
				Calendar now = Calendar.getInstance();
				String nowAsString = DateFormat.format("dd.MM.yyyy hh:mm:ss", now).toString();
				try {
					mOutput.write(nowAsString.getBytes());
					mOutput.flush();
					Log.i(tag, "Writer <<< " + nowAsString);
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
				mOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
