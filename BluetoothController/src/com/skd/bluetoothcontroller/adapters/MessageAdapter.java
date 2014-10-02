package com.skd.bluetoothcontroller.adapters;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skd.bluetoothcontroller.R;
import com.skd.bluetoothcontroller.entity.BtMessage;

public class MessageAdapter extends BaseAdapter {
	
	static class ViewHolder {
		
		TextView mTextMessage;
		TextView mTextSentAt;
		LinearLayout mLayout;
		
		public ViewHolder(View view) {
			mTextMessage = (TextView) view.findViewById(R.id.textMessageText);
			mTextSentAt = (TextView) view.findViewById(R.id.textSentAt);
			mLayout = (LinearLayout) view.findViewById(R.id.layoutMessageItem);
		}
	}
	
	private List<BtMessage> mMessages;
	
	public MessageAdapter() {
		mMessages = new ArrayList<BtMessage>();
	}
	
	public void addMessage(BtMessage message){
		mMessages.add(message);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMessages != null ? mMessages.size() : 0;
	}

	@Override
	public BtMessage getItem(int position) {
		return mMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = createNewView(parent);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		BtMessage message = getItem(position);
		
		holder.mTextMessage.setText(message.getMessage());
		holder.mTextSentAt.setText(message.getSentAt());
		holder.mLayout.setBackgroundColor(message.isMy() ? Color.GREEN : Color.RED);
		
		return convertView;
	}

	private View createNewView(ViewGroup parent){
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.list_item_message, parent, false);
		ViewHolder holder = new ViewHolder(view);
		view.setTag(holder);
		return view;
	}
}
