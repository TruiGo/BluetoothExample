package com.skd.bluetoothcontroller.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;

import com.skd.bluetoothcontroller.MessageSubscriber;
import com.skd.bluetoothcontroller.Messenger;
import com.skd.bluetoothcontroller.adapters.MessageAdapter;
import com.skd.bluetoothcontroller.entity.BtMessage;

public class ServerFragment extends ListFragment implements MessageSubscriber {
	
	private final static String tag = ServerFragment.class.getSimpleName();

	private Messenger mMessenger;
	private MessageAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof Messenger){
			mMessenger = (Messenger) activity;
			mMessenger.subscribe(this);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(mMessenger != null){
			mMessenger.unsubscribe(this);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new MessageAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setAdapter(mAdapter);
	}

	@Override
	public void onMessageReceived(BtMessage message) {
		mAdapter.addMessage(message);
		setListShown(true);
	}
}
