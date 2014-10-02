package com.skd.bluetoothcontroller;

import com.skd.bluetoothcontroller.entity.BtMessage;

public interface Messenger {

	void sendMessage(String message);
	void subscribe(MessageSubscriber subscriber);
	void unsubscribe(MessageSubscriber subscriber);
	void notifySubscribers(BtMessage message);
	
}
