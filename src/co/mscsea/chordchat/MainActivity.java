package co.mscsea.chordchat;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;

public class MainActivity extends ListActivity implements OnClickListener {

	private static final String CHORD_CHANNEL = "co.mscsea.chordchat.channel";
	private static final String CHORD_MESSAGE_TYPE = "co.mscsea.chordchat.type.text";
	
	private Button connect;
	private Button disconnect;
	private Button send;
	private EditText message;
	
	private ArrayAdapter<String> adapter;
	
	private SchordManager chordManager;
	private SchordChannel chordChannel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connect = (Button) findViewById(R.id.connect);
		connect.setOnClickListener(this);
		
		disconnect = (Button) findViewById(R.id.disconnect);
		disconnect.setOnClickListener(this);
		
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(this);
		
		message = (EditText) findViewById(R.id.message);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(adapter);
		
		Schord chord = new Schord();
		try {
			chord.initialize(this);
		} catch (SsdkUnsupportedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		disconnect();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.connect: {
				boolean connected = connect();
				updateConnectionState(connected);
				break;
			}
			case R.id.disconnect: {
				disconnect();
				updateConnectionState(false);
				break;
			}
			case R.id.send: {
				String text = message.getText().toString().trim();
				if (!TextUtils.isEmpty(text)) {
					sendMessage(text);
					message.setText("");
				}
				break;
			}
		}
	}
	
	private void sendMessage(String text) {
		displayMessage(String.format(getString(R.string.me), text));
		if (chordChannel != null) {
			byte[][] bytes = new byte[1][];
			bytes[0] = text.getBytes();
			chordChannel.sendDataToAll(CHORD_MESSAGE_TYPE, bytes);
		}
	}
	
	private void displayMessage(String text) {
		adapter.add(text);
		adapter.notifyDataSetChanged();
	}
	
	private void joinChannel() {
		try {
			chordChannel = chordManager.joinChannel(CHORD_CHANNEL, channelListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean connect() {
		chordManager = new SchordManager(this);
		List<Integer> interfaceList = chordManager.getAvailableInterfaceTypes();
		if (interfaceList.isEmpty()) {
			displayDialog(getString(R.string.no_interface));
			return false;
		}
		
		// For simplicity we will connect to the first interface available
		try {
			chordManager.start(interfaceList.get(0).intValue(), managerListener);
		} catch (Exception e) {
			e.printStackTrace();
			displayDialog(getString(R.string.error_start_network));
			return false;
		}
		
		return true;
	}
	
	private void disconnect() {
		if (chordManager != null) {
			chordManager.stop();
			chordManager.close();
			chordManager = null;
		}
	}
	
	private void updateConnectionState(boolean connected) {
		disconnect.setVisibility(connected ? View.VISIBLE : View.GONE);
		connect.setVisibility(connected ? View.GONE : View.VISIBLE);
	}
	
	private void displayDialog(String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(message);
		dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	private SchordManager.StatusListener managerListener = new SchordManager.StatusListener() {
		
		@Override
		public void onStopped(int reason) {
			if (reason == STOPPED_BY_USER) {
				updateConnectionState(false);
			}
		}
		
		@Override
		public void onStarted(String name, int reason) {
			if (reason == STARTED_BY_USER) {
				joinChannel();
			}
		}
	};
	
	private SchordChannel.StatusListener channelListener = new SchordChannel.StatusListener() {
		
		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			displayMessage(String.format(getString(R.string.has_left), fromNode));
		}
		
		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {
			displayMessage(String.format(getString(R.string.has_joined), fromNode));
		}
		
		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload) {
			if (payloadType.equals(CHORD_MESSAGE_TYPE)) {
				String text = new String(payload[0]);
				displayMessage(String.format(getString(R.string.they), fromNode, text));
			}
		}
		
		@Override
		public void onMultiFilesWillReceive(String fromNode, String fromChannel, String fileName, String taskId, int totalCount, String fileType, long fileSize) {
			
		}
		
		@Override
		public void onMultiFilesSent(String toNode, String toChannel, String fileName, String taskId, int index, String fileType) {
			
		}
		
		@Override
		public void onMultiFilesReceived(String fromNode, String fromChannel, String fileName, String taskId, int index, String fileType, long fileSize, String tmpFilePath) {
			
		}
		
		@Override
		public void onMultiFilesFinished(String node, String channel, String taskId, int reason) {
			
		}
		
		@Override
		public void onMultiFilesFailed(String node, String channel, String fileName, String taskId, int index, int reason) {
			
		}
		
		@Override
		public void onMultiFilesChunkSent(String toNode, String toChannel, String fileName, String taskId, int index, String fileType, long fileSize, long offset, long chunkSize) {
			
		}
		
		@Override
		public void onMultiFilesChunkReceived(String fromNode, String fromChannel, String fileName, String taskId, int index, String fileType, long fileSize, long offset) {
			
		}
		
		@Override
		public void onFileWillReceive(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize) {
			
		}
		
		@Override
		public void onFileSent(String toNode, String toChannel, String fileName, String hash, String fileType, String exchangeId) {
			
		}
		
		@Override
		public void onFileReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, String tmpFilePath) {
			
		}
		
		@Override
		public void onFileFailed(String node, String channel, String fileName, String hash, String exchangeId, int reason) {
			
		}
		
		@Override
		public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, long offset, long chunkSize) {
			
		}
		
		@Override
		public void onFileChunkReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, long offset) {
			
		}
	};
}
