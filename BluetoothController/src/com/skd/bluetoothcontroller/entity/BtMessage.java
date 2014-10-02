package com.skd.bluetoothcontroller.entity;

import java.util.Calendar;

import android.text.format.DateFormat;

public class BtMessage {

	private String mMessage;
	private String mSentAt;
	private boolean mMy;
	
	public BtMessage(String message, boolean my) {
		mMessage = message;
		mMy = my;
		mSentAt = DateFormat.format("dd.MM.yyyy hh:mm:ss", Calendar.getInstance()).toString();
	}

	public String getMessage() {
		return mMessage;
	}

	public String getSentAt() {
		return mSentAt;
	}

	public boolean isMy() {
		return mMy;
	}
}
