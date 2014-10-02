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
import android.widget.Toast;

import com.skd.bluetoothcontroller.Messenger;
import com.skd.bluetoothcontroller.R;

public class ClientFragment extends Fragment implements OnClickListener {

	private EditText mEditMessage;
	
	private Messenger mMessenger;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof Messenger){
			mMessenger = (Messenger) activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mMessenger = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_client, container, false);
		mEditMessage = (EditText) view.findViewById(R.id.editMessage);
		view.findViewById(R.id.buttonSend).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.buttonSend){
			sendMessage();
		}
	}

	private void sendMessage(){
		String message = mEditMessage.getText().toString();
		if(!TextUtils.isEmpty(message)){
			mMessenger.sendMessage(message);
			mEditMessage.setText("");
		} else {
			Toast.makeText(getActivity(), R.string.error_please_enter_a_message, Toast.LENGTH_SHORT).show();
		}
	}
}
