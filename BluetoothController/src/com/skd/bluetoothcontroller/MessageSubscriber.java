package com.skd.bluetoothcontroller;

import com.skd.bluetoothcontroller.entity.Message;

public interface MessageSubscriber {

	void onMessageReceived(Message message);
	
}
