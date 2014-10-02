package com.skd.bluetoothcontroller.entity;

import java.util.Calendar;

import android.text.format.DateFormat;

public class BtMessage {

	private String mMessage;
	private String mSentAt;
	
	public BtMessage(String mMessage) {
		this.mMessage = mMessage;
		this.mSentAt = DateFormat.format("dd.MM.yyyy hh:mm:ss", Calendar.getInstance()).toString();
	}

	public String getMessage() {
		return mMessage;
	}

	public String getSentAt() {
		return mSentAt;
	}
}
