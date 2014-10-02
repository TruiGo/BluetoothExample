package com.skd.bluetoothcontroller;

public interface Messenger {

	void sendMessage(String message);
	void subscribe(MessageSubscriber subscriber);
	void unsubscribe(MessageSubscriber subscriber);
	
}
