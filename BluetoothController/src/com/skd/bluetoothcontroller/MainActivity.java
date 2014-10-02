package com.skd.bluetoothcontroller;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.skd.bluetoothcontroller.communication.CommunicationThread;
import com.skd.bluetoothcontroller.connection.ClientSocketConnectionThread;
import com.skd.bluetoothcontroller.connection.ConnectionThread.SocketConnectionListener;
import com.skd.bluetoothcontroller.connection.ServerSocketConnectionThread;
import com.skd.bluetoothcontroller.entity.BtMessage;
import com.skd.bluetoothcontroller.fragments.MessageFragment;

public class MainActivity extends Activity implements OnClickListener, Messenger {
	
	private class DefaultConnectionListener implements SocketConnectionListener {

		@Override
		public void onSocketConnectionFailed(final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					mProgressDialog.hide();
					if(!TextUtils.isEmpty(message)){
						Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		@Override
		public void onSocketAcquired(final BluetoothSocket socket) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgressDialog.hide();
					mSocket = socket;
					
					onClientConnectionEstablished(socket);
				}
			});
		}
	}
	
	@SuppressWarnings("unused")
	private final static String tag = MainActivity.class.getSimpleName();
	
	private final static String END_SESSION_REQUEST = "*END*";
	
	private final static int REQUEST_ENABLE_BT = 9001;
	private final static int REQUEST_ENABLE_DISCOVER = 9002;

	private Button mButtonSwitchBluetooth;
	private Button mButtonUseAsClient; 
	private Button mButtonUseAsServer;
	private Button mButtonCloseConnection;

	private BluetoothAdapter mBtAdapter;

	private boolean mBluetoothEnabled;
	private boolean mIsConnected;
	
	private IntentFilter mBluetoothEventsFilter;
	private ProgressDialog mProgressDialog;

	private BluetoothSocket mSocket;
	private List<MessageSubscriber> mSubscribers;
	private List<BluetoothDevice> mDiscoveredDevices;
	
	private IncomingMessageHandler mIncomingMessageHandler;

	private CommunicationThread mCommunicationThread;
	
	private BroadcastReceiver mBtEventsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				mDiscoveredDevices.clear();
				mProgressDialog.setMessage(getString(R.string.progress_discovering));
				mProgressDialog.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mProgressDialog.hide();
				onDiscoverDevicesFinished();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDiscoveredDevices.add(device);
			}
		}
	};

	
	public static class IncomingMessageHandler extends Handler {

		WeakReference<Messenger> mMessenger;
		
		public IncomingMessageHandler(Messenger messenger) {
			mMessenger = new WeakReference<Messenger>(messenger);
		}

		@Override
		public void dispatchMessage(Message msg) {
			Messenger messenger = mMessenger.get();
			if(messenger != null){
				String message = (String) msg.obj;
				messenger.notifySubscribers(new BtMessage(message, false));
				if(message.equals(END_SESSION_REQUEST)){
					messenger.onEndSessionRequest();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDiscoveredDevices = new ArrayList<BluetoothDevice>();
		mSubscribers = new ArrayList<MessageSubscriber>();
		mIncomingMessageHandler = new IncomingMessageHandler(this);
		
		mBluetoothEventsFilter = new IntentFilter();
		mBluetoothEventsFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mBluetoothEventsFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mBluetoothEventsFilter.addAction(BluetoothDevice.ACTION_FOUND);

		mButtonSwitchBluetooth = (Button) findViewById(R.id.buttonSwitchBluetooth);
		mButtonSwitchBluetooth.setOnClickListener(this);

		mButtonUseAsClient = (Button) findViewById(R.id.buttonUseAsClient);
		mButtonUseAsClient.setOnClickListener(this);

		mButtonUseAsServer = (Button) findViewById(R.id.buttonUseAsServer);
		mButtonUseAsServer.setOnClickListener(this);
		
		mButtonCloseConnection = (Button) findViewById(R.id.buttonCloseConnection);
		mButtonCloseConnection.setOnClickListener(this);
		
		mProgressDialog = new ProgressDialog(this);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter != null) {
			mBluetoothEnabled = mBtAdapter.isEnabled();
			onBtStateChanged();
		} else {
			finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mBtEventsReceiver, mBluetoothEventsFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mBtEventsReceiver);
		ensureProgressDialogHidden();
	}

	private void enableBt() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, REQUEST_ENABLE_BT);
	}
	
	private void requestDiscoverable(){
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(intent, REQUEST_ENABLE_DISCOVER);
	}

	private void updateViews() {
		mButtonSwitchBluetooth.setVisibility(mBluetoothEnabled ? View.GONE : View.VISIBLE);
		mButtonUseAsClient.setVisibility(mBluetoothEnabled && !mIsConnected ? View.VISIBLE : View.GONE);
		mButtonUseAsServer.setVisibility(mBluetoothEnabled && !mIsConnected ? View.VISIBLE : View.GONE);
		mButtonCloseConnection.setVisibility(mIsConnected ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonSwitchBluetooth:
				switchBluetooth();
				break;
			case R.id.buttonUseAsClient:
				becomeClient();
				break;
			case R.id.buttonUseAsServer:
				becomeServer();
				break;
			case R.id.buttonCloseConnection:
				closeCurrentSession();
				break;
		}
	}

	private void switchBluetooth() {
		enableBt();
	}

	private void becomeClient() {
		mBtAdapter.startDiscovery();
	}
	
	private void becomeServer() {
		requestDiscoverable();
	}
	
	private void closeCurrentSession(){
		mCommunicationThread.addMessageToQueue(END_SESSION_REQUEST);
		discardConnection();
	}
	
	private void onDiscoverDevicesFinished(){
		mDiscoveredDevices.addAll(mBtAdapter.getBondedDevices());
		if(!mDiscoveredDevices.isEmpty()){
			showDiscoveredDevices();
		} else {
			Toast.makeText(this, R.string.error_no_devices_were_found, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void showDiscoveredDevices(){
		CharSequence[] items = new CharSequence[mDiscoveredDevices.size()];
		for(int i = 0, max = mDiscoveredDevices.size(); i < max; i++){
			items[i] = mDiscoveredDevices.get(i).getName();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_a_device_to_connect);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				connectWithServer(mDiscoveredDevices.get(which));
			}
		});
		
		builder.create().show();
	}
	
	private void connectWithServer(BluetoothDevice server){
		mProgressDialog.setMessage(getString(R.string.progress_connecting));
		mProgressDialog.show();
		new ClientSocketConnectionThread(server, new DefaultConnectionListener()).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
			mBluetoothEnabled = true;
			onBtStateChanged();
		} else if (requestCode == REQUEST_ENABLE_DISCOVER && resultCode != RESULT_CANCELED) {
			onDiscoverAllowed();
		}
	}

	private void onBtStateChanged() {
		updateViews();
	}

	private void onDiscoverAllowed(){
		final ServerSocketConnectionThread connectionThread = new ServerSocketConnectionThread(mBtAdapter, new DefaultConnectionListener());
		connectionThread.start();

		OnCancelListener cancelListener = new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				connectionThread.cancel();
			}
		};
		
		mProgressDialog.setOnCancelListener(cancelListener);
		mProgressDialog.setMessage(getString(R.string.progress_waiting_for_connection));
		mProgressDialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		discardConnection();
	}

	@Override
	public void sendMessage(String message) {
		if(mCommunicationThread != null){
			mCommunicationThread.addMessageToQueue(message);
		}
	}

	@Override
	public void subscribe(MessageSubscriber subscriber) {
		mSubscribers.add(subscriber);
	}

	@Override
	public void unsubscribe(MessageSubscriber subscriber) {
		for(Iterator<MessageSubscriber> iterator = mSubscribers.iterator(); iterator.hasNext(); ){
			if(iterator.next().equals(subscriber)){
				iterator.remove();
			}
		}
	}

	@Override
	public void notifySubscribers(com.skd.bluetoothcontroller.entity.BtMessage message) {
		for(Iterator<MessageSubscriber> iterator = mSubscribers.iterator(); iterator.hasNext(); ){
			iterator.next().onMessageReceived(message);
		}
	}
	
	private void ensureProgressDialogHidden(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.hide();
		}
	}
	
	private void loadFragment(Fragment fragment){
		String tag = fragment.getClass().getSimpleName();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, fragment, tag);
		transaction.addToBackStack(tag);
		transaction.commit();
	}

	private void onClientConnectionEstablished(BluetoothSocket socket){
		try {
			mIsConnected = true;
			updateViews();
			
			loadFragment(new MessageFragment());
			
			mCommunicationThread = new CommunicationThread(socket, mIncomingMessageHandler);
			mCommunicationThread.start();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void discardConnection(){
		mIsConnected = false;
		updateViews();
		
		if(mCommunicationThread != null){
			mCommunicationThread.interrupt();
		}
		
		if(mSocket != null && mSocket.isConnected()){
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		getFragmentManager().popBackStack();
	}

	@Override
	public void onEndSessionRequest() {
		discardConnection();
	}
}
