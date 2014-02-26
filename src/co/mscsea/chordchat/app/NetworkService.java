package co.mscsea.chordchat.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;

public class NetworkService extends Service {

	public static final String CHORD_CHANNEL = "co.mscsea.chordchat.channel";
	public static final String CHORD_TYPE_MESSAGE = "co.mscsea.chordchat.type.message";
	public static final String CHORD_TYPE_USERNAME = "co.mscsea.chordchat.type.username";
	
	public enum ConnectionState {
		CONNECTED,
		CONNECTING,
		DISCONNECTED
	}
	
	private final IBinder mBinder = new MyBinder();
	
	private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;
	private boolean mIsInitialized = false;
    
	private SchordManager mChordManager;
	private SchordChannel mChordChannel;
	private ArrayList<SchordManager.StatusListener> mManagerListeners = new ArrayList<SchordManager.StatusListener>();
	private ArrayList<SchordChannel.StatusListener> mChannelListeners = new ArrayList<SchordChannel.StatusListener>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class MyBinder extends Binder {
		public NetworkService getService() {
			return NetworkService.this;
		}
	}
	
	public void addManagerListener(SchordManager.StatusListener listener) {
		mManagerListeners.add(listener);
	}
	
	public void removeManagerListener(SchordManager.StatusListener listener) {
		mManagerListeners.remove(listener);
	}
	
	public void addChannelListener(SchordChannel.StatusListener listener) {
		mChannelListeners.add(listener);
	}
	
	public void removeChannelListener(SchordChannel.StatusListener listener) {
		mChannelListeners.remove(listener);
	}
	
	private void initialize() {
		if (mIsInitialized) return;
		
		//<task>
		Schord chord = new Schord();
		try {
			chord.initialize(this);
		} catch (SsdkUnsupportedException e) {
			e.printStackTrace();
		}
		//</task>
	}
	
	public void connect() {
		if (mConnectionState != ConnectionState.DISCONNECTED) return;
		
		mConnectionState = ConnectionState.CONNECTING;
		
		initialize();
		
		if (mChordManager == null) {
			// Create chord manager
			//<task>
			mChordManager = new SchordManager(this);
			mChordManager.setLooper(getApplication().getMainLooper());
			//</task>
		}
		
		List<Integer> interfaceList = null;
		// Get available interfaces
		//<task>
		interfaceList = mChordManager.getAvailableInterfaceTypes();
		//</task>
		if (interfaceList.isEmpty()) {
			disconnect();
			return;
		}
		
		// For simplicity we will connect to the first interface available
		//<task>
		try {
			mChordManager.start(interfaceList.get(0).intValue(), managerListener);
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
			return;
		}
		//</task>
	}
	
	public void disconnect() {
		if (mChordManager != null) {
			// Stop the connection
			//<task>
			mChordManager.stop();
			//</task>
		}
		
		mConnectionState = ConnectionState.DISCONNECTED;
	}
	
	private void joinChannel() {
		// Join channel
		//<task>
		try {
			mChordChannel = mChordManager.joinChannel(CHORD_CHANNEL, channelListener);
			mConnectionState = ConnectionState.CONNECTED;
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
		//</task>
	}
	
	public SchordChannel getChannel() {
		return mChordChannel;
	}
	
	public ConnectionState getState() {
		return mConnectionState;
	}
	
	public void destroy() {
		// Stop the connection
		//<task>
		if (mChordManager != null) {
			mChordManager.close();
		}
		//</task>
	}
	
	private SchordManager.StatusListener managerListener = new SchordManager.StatusListener() {
		
		@Override
		public void onStopped(int reason) {
			for (SchordManager.StatusListener listener : mManagerListeners) {
				listener.onStopped(reason);
			}
		}
		
		@Override
		public void onStarted(String name, int reason) {
			if (reason == STARTED_BY_USER) {
				joinChannel();
			}
			
			for (SchordManager.StatusListener listener : mManagerListeners) {
				listener.onStarted(name, reason);
			}
		}
	};
	
	private SchordChannel.StatusListener channelListener = new SchordChannel.StatusListener() {
		
		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onNodeLeft(fromNode, fromChannel);
			}
		}
		
		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onNodeJoined(fromNode, fromChannel);
			}
		}
		
		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onDataReceived(fromNode, fromChannel, payloadType, payload);
			}
		}
		
		@Override
		public void onMultiFilesWillReceive(String fromNode, String fromChannel, String fileName, String taskId, int totalCount, String fileType, long fileSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesWillReceive(fromNode, fromChannel, fileName, taskId, totalCount, fileType, fileSize);
			}
		}
		
		@Override
		public void onMultiFilesSent(String toNode, String toChannel, String fileName, String taskId, int index, String fileType) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesSent(toNode, toChannel, fileName, taskId, index, fileType);
			}
		}
		
		@Override
		public void onMultiFilesReceived(String fromNode, String fromChannel, String fileName, String taskId, int index, String fileType, long fileSize, String tmpFilePath) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesReceived(fromNode, fromChannel, fileName, taskId, index, fileType, fileSize, tmpFilePath);
			}
		}
		
		@Override
		public void onMultiFilesFinished(String node, String channel, String taskId, int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesFinished(node, channel, taskId, reason);
			}
		}
		
		@Override
		public void onMultiFilesFailed(String node, String channel, String fileName, String taskId, int index, int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesFailed(node, channel, fileName, taskId, index, reason);
			}
		}
		
		@Override
		public void onMultiFilesChunkSent(String toNode, String toChannel, String fileName, String taskId, int index, String fileType, long fileSize, long offset, long chunkSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesChunkSent(toNode, toChannel, fileName, taskId, index, fileType, fileSize, offset, chunkSize);
			}
		}
		
		@Override
		public void onMultiFilesChunkReceived(String fromNode, String fromChannel, String fileName, String taskId, int index, String fileType, long fileSize, long offset) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesChunkReceived(fromNode, fromChannel, fileName, taskId, index, fileType, fileSize, offset);
			}
		}
		
		@Override
		public void onFileWillReceive(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileWillReceive(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize);
			}
		}
		
		@Override
		public void onFileSent(String toNode, String toChannel, String fileName, String hash, String fileType, String exchangeId) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileSent(toNode, toChannel, fileName, hash, fileType, exchangeId);
			}
		}
		
		@Override
		public void onFileReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, String tmpFilePath) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize, tmpFilePath);
			}
		}
		
		@Override
		public void onFileFailed(String node, String channel, String fileName, String hash, String exchangeId, int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileFailed(node, channel, fileName, hash, exchangeId, reason);
			}
		}
		
		@Override
		public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, long offset, long chunkSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileChunkSent(toNode, toChannel, fileName, hash, fileType, exchangeId, fileSize, offset, chunkSize);
			}
		}
		
		@Override
		public void onFileChunkReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType, String exchangeId, long fileSize, long offset) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileChunkReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize, offset);
			}
		}
		
		@Override
		public void onUdpDataDelivered(String toNode, String toChannel, String reqId) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onUdpDataDelivered(toNode, toChannel, reqId);
			}
		}

		@Override
		public void onUdpDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload, String sessionName) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onUdpDataReceived(fromNode, fromChannel, payloadType, payload, sessionName);
			}
		}
	};
}
