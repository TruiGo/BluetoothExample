package com.skd.bluetoothcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.skd.bluetoothcontroller.adapters.DevicesListAdapter;
import com.skd.bluetoothcontroller.communication.ReadThread;
import com.skd.bluetoothcontroller.communication.WriteThread;
import com.skd.bluetoothcontroller.connection.ClientSocketConnectionThread;
import com.skd.bluetoothcontroller.connection.ConnectionThread.SocketConnectionListener;
import com.skd.bluetoothcontroller.connection.ServerSocketConnectionThread;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	@SuppressWarnings("unused")
	private final static String tag = MainActivity.class.getSimpleName();
	
	private final static int REQUEST_ENABLE_BT = 9001;
	private final static int REQUEST_ENABLE_DISCOVER = 9002;

	private Button mButtonSwitchBluetooth;
	private Button mButtonScanDevices;
	private Button mButtonStartPlay; 
	private Button mButtonStartServer;

	private ListView mListPairedDevices;

	private BluetoothAdapter mBtAdapter;

	private boolean mBluetoothEnabled;
	private boolean mControllerEnabled;
	private boolean mServerStarted;
	
	private IntentFilter mBluetoothEventsFilter;
	private ProgressDialog mProgressDialog;

	private DevicesListAdapter mAdapterDevices;
	private BluetoothSocket mSocket;
	
	private BroadcastReceiver mBtEventsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				mProgressDialog.setMessage(getString(R.string.progress_discovering));
				mProgressDialog.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mProgressDialog.hide();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mAdapterDevices.addDevice(device);
			}
		}
	};
	
	private SocketConnectionListener mSocketClientConnectionListener = new SocketConnectionListener() {
		
		@Override
		public void onSocketConnectionFailed(final String message) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mProgressDialog.hide();
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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
					
					new WriteThread(mSocket).start();
				}
			});
		}
	};
	
	private SocketConnectionListener mSocketServerConnectionListener = new SocketConnectionListener() {
		
		@Override
		public void onSocketConnectionFailed(final String message) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mProgressDialog.hide();
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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
					
					new ReadThread(mSocket).start();
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBluetoothEventsFilter = new IntentFilter();
		mBluetoothEventsFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mBluetoothEventsFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mBluetoothEventsFilter.addAction(BluetoothDevice.ACTION_FOUND);

		mButtonSwitchBluetooth = (Button) findViewById(R.id.buttonSwitchBluetooth);
		mButtonSwitchBluetooth.setOnClickListener(this);

		mButtonStartPlay = (Button) findViewById(R.id.buttonSwitchController);
		mButtonStartPlay.setOnClickListener(this);

		mButtonScanDevices = (Button) findViewById(R.id.buttonScanDevices);
		mButtonScanDevices.setOnClickListener(this);
		
		mButtonStartServer = (Button) findViewById(R.id.buttonStartServer);
		mButtonStartServer.setOnClickListener(this);
		
		mListPairedDevices = (ListView) findViewById(R.id.listDevices);
		mListPairedDevices.setOnItemClickListener(this);
		
		mProgressDialog = new ProgressDialog(this);
		
		mAdapterDevices = new DevicesListAdapter();
		mListPairedDevices.setAdapter(mAdapterDevices);

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
		mButtonScanDevices.setVisibility(mBluetoothEnabled ? View.VISIBLE : View.GONE);
		mButtonStartPlay.setVisibility(mBluetoothEnabled ? View.VISIBLE : View.GONE);

		int buttonControllerTextId = mControllerEnabled ? R.string.label_stop_play : R.string.label_start_play;
		mButtonStartPlay.setText(buttonControllerTextId);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonSwitchBluetooth:
				switchBluetooth();
				break;
			case R.id.buttonSwitchController:
				switchController();
				break;
			case R.id.buttonScanDevices:
				startScanDevices();
				break;
			case R.id.buttonStartServer:
				becomeServer();
				break;
		}
	}

	private void switchBluetooth() {
		enableBt();
	}

	private void switchController() {
		Toast.makeText(this, "switch controller", Toast.LENGTH_SHORT).show();
	}

	private void startScanDevices() {
		mBtAdapter.startDiscovery();
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
		if (mBluetoothEnabled) {
			fillPairedDevices();
		}
	}

	private void fillPairedDevices() {
		List<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>(mBtAdapter.getBondedDevices());
		mAdapterDevices.addDevices(pairedDevices);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BluetoothDevice device = mAdapterDevices.getItem(position);
		mProgressDialog.setMessage(getString(R.string.progress_connecting));
		mProgressDialog.show();
		new ClientSocketConnectionThread(device, mSocketClientConnectionListener).start();
	}
	
	private void becomeServer() {
		requestDiscoverable();
	}
	
	private void onDiscoverAllowed(){
		mProgressDialog.setMessage(getString(R.string.progress_waiting_for_connection));
		mProgressDialog.show();
		new ServerSocketConnectionThread(mBtAdapter, mSocketServerConnectionListener).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mSocket != null && mSocket.isConnected()){
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
