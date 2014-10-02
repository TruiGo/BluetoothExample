package com.skd.bluetoothcontroller;

import com.skd.bluetoothcontroller.entity.BtMessage;

public interface MessageSubscriber {

	void onMessageReceived(BtMessage message);
	
}
