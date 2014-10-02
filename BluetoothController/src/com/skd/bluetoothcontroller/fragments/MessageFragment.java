package com.skd.bluetoothcontroller.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.skd.bluetoothcontroller.MessageSubscriber;
import com.skd.bluetoothcontroller.Messenger;
import com.skd.bluetoothcontroller.R;
import com.skd.bluetoothcontroller.adapters.MessageAdapter;
import com.skd.bluetoothcontroller.entity.BtMessage;

public class MessageFragment extends Fragment implements OnClickListener, MessageSubscriber {

	private EditText mEditMessage;
	private ListView mListMessages;

	private Messenger mMessenger;
	private MessageAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Messenger) {
			mMessenger = (Messenger) activity;
			mMessenger.subscribe(this);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mMessenger != null) {
			mMessenger.unsubscribe(this);
			mMessenger = null;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_client, container, false);
		mEditMessage = (EditText) view.findViewById(R.id.editMessage);
		mListMessages = (ListView) view.findViewById(R.id.listMessages);
		view.findViewById(R.id.buttonSend).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonSend) {
			sendMessage();
		}
	}

	private void sendMessage() {
		String message = mEditMessage.getText().toString();
		if (!TextUtils.isEmpty(message)) {
			mMessenger.sendMessage(message);
			mEditMessage.setText("");
			mAdapter.addMessage(new BtMessage(message, true));
		} else {
			Toast.makeText(getActivity(), R.string.error_please_enter_a_message, Toast.LENGTH_SHORT).show();
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
		mListMessages.setAdapter(mAdapter);
	}

	@Override
	public void onMessageReceived(BtMessage message) {
		mAdapter.addMessage(message);
	}
}
