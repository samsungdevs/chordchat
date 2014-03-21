/*
 * Copyright 2014 Samsung Developer Relations Team (MSCSEA)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mscsea.chordchat.app;

import java.util.ArrayList;

import android.content.Context;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;

public class NetworkService {

	public static final String CHORD_CHANNEL = "co.mscsea.chordchat.channel";
	public static final String CHORD_TYPE_MESSAGE = "co.mscsea.chordchat.type.message";
	public static final String CHORD_TYPE_USERNAME = "co.mscsea.chordchat.type.username";
	
	public static final int OK = 0;
	public static final int ERROR_CONNECT_ALREADY_CONNECTED = 1;
	public static final int ERROR_CONNECT_INVALID_INTERFACE = 2;

	public enum ConnectionState {
		CONNECTED, CONNECTING, DISCONNECTED
	}

	private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;
	private boolean mIsInitialized = false;

	private SchordManager mChordManager;
	private SchordChannel mChordChannel;
	private ArrayList<SchordManager.StatusListener> mManagerListeners = new ArrayList<SchordManager.StatusListener>();
	private ArrayList<SchordChannel.StatusListener> mChannelListeners = new ArrayList<SchordChannel.StatusListener>();
	private ArrayList<SchordManager.NetworkListener> mNetworkListeners = new ArrayList<SchordManager.NetworkListener>();
	private Context mContext;
	
	public NetworkService(Context context) {
		mContext = context;
		
		initialize(mContext);
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
	
	public void addNetworkListener(SchordManager.NetworkListener listener) {
		mNetworkListeners.add(listener);
	}
	
	public void removeNetworkListener(SchordManager.NetworkListener listener) {
		mNetworkListeners.remove(listener);
	}

	private void initialize(Context context) {
		if (mIsInitialized)
			return;

		// TODO: Intialize Chord
		// <task>
		Schord chord = new Schord();
		try {
			chord.initialize(context);
		} catch (SsdkUnsupportedException e) {
			e.printStackTrace();
		}
		// </task>
		
		// TODO: Create chord manager
		// <task>
		mChordManager = new SchordManager(mContext);
		mChordManager.setLooper(mContext.getMainLooper());
		mChordManager.setNetworkListener(networkListener);
		// </task>
	}

	public int connect(int networkInterface) {
		if (mConnectionState != ConnectionState.DISCONNECTED)
			return ERROR_CONNECT_ALREADY_CONNECTED;

		mConnectionState = ConnectionState.CONNECTING;

		// TODO: Connect to the network interface
		// <task>
		try {
			mChordManager.start(networkInterface, managerListener);
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
			return ERROR_CONNECT_INVALID_INTERFACE;
		}
		// </task>
		
		return OK;
	}

	public void disconnect() {
		if (mChordManager != null) {
			// TODO: Stop the connection
			// <task>
			mChordManager.stop();
			// </task>
		}

		mConnectionState = ConnectionState.DISCONNECTED;
	}

	private void joinChannel() {
		// TODO: Join channel
		// <task>
		try {
			mChordChannel = mChordManager.joinChannel(CHORD_CHANNEL, channelListener);
			mConnectionState = ConnectionState.CONNECTED;
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
		// </task>
	}
	
	public SchordManager getManager() {
		return mChordManager;
	}

	public SchordChannel getChannel() {
		return mChordChannel;
	}

	public ConnectionState getState() {
		return mConnectionState;
	}

	public void destroy() {
		// TODO: Stop the connection
		// <task>
		if (mChordManager != null) {
			mChordManager.close();
		}
		// </task>
	}
	
	private SchordManager.NetworkListener networkListener = new SchordManager.NetworkListener() {
		
		@Override
		public void onDisconnected(int interfaceType) {
			for (SchordManager.NetworkListener listener : mNetworkListeners) {
				listener.onDisconnected(interfaceType);
			}
		}
		
		@Override
		public void onConnected(int interfaceType) {
			for (SchordManager.NetworkListener listener : mNetworkListeners) {
				listener.onConnected(interfaceType);
			}
		}
	};

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
		public void onMultiFilesWillReceive(String fromNode, String fromChannel, String fileName, String taskId,
				int totalCount, String fileType, long fileSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesWillReceive(fromNode, fromChannel, fileName, taskId, totalCount, fileType,
						fileSize);
			}
		}

		@Override
		public void onMultiFilesSent(String toNode, String toChannel, String fileName, String taskId, int index,
				String fileType) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesSent(toNode, toChannel, fileName, taskId, index, fileType);
			}
		}

		@Override
		public void onMultiFilesReceived(String fromNode, String fromChannel, String fileName, String taskId,
				int index, String fileType, long fileSize, String tmpFilePath) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesReceived(fromNode, fromChannel, fileName, taskId, index, fileType, fileSize,
						tmpFilePath);
			}
		}

		@Override
		public void onMultiFilesFinished(String node, String channel, String taskId, int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesFinished(node, channel, taskId, reason);
			}
		}

		@Override
		public void onMultiFilesFailed(String node, String channel, String fileName, String taskId, int index,
				int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesFailed(node, channel, fileName, taskId, index, reason);
			}
		}

		@Override
		public void onMultiFilesChunkSent(String toNode, String toChannel, String fileName, String taskId, int index,
				String fileType, long fileSize, long offset, long chunkSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesChunkSent(toNode, toChannel, fileName, taskId, index, fileType, fileSize, offset,
						chunkSize);
			}
		}

		@Override
		public void onMultiFilesChunkReceived(String fromNode, String fromChannel, String fileName, String taskId,
				int index, String fileType, long fileSize, long offset) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onMultiFilesChunkReceived(fromNode, fromChannel, fileName, taskId, index, fileType, fileSize,
						offset);
			}
		}

		@Override
		public void onFileWillReceive(String fromNode, String fromChannel, String fileName, String hash,
				String fileType, String exchangeId, long fileSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileWillReceive(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize);
			}
		}

		@Override
		public void onFileSent(String toNode, String toChannel, String fileName, String hash, String fileType,
				String exchangeId) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileSent(toNode, toChannel, fileName, hash, fileType, exchangeId);
			}
		}

		@Override
		public void onFileReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, String tmpFilePath) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize,
						tmpFilePath);
			}
		}

		@Override
		public void onFileFailed(String node, String channel, String fileName, String hash, String exchangeId,
				int reason) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileFailed(node, channel, fileName, hash, exchangeId, reason);
			}
		}

		@Override
		public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, long offset, long chunkSize) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileChunkSent(toNode, toChannel, fileName, hash, fileType, exchangeId, fileSize, offset,
						chunkSize);
			}
		}

		@Override
		public void onFileChunkReceived(String fromNode, String fromChannel, String fileName, String hash,
				String fileType, String exchangeId, long fileSize, long offset) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onFileChunkReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize,
						offset);
			}
		}

		@Override
		public void onUdpDataDelivered(String toNode, String toChannel, String reqId) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onUdpDataDelivered(toNode, toChannel, reqId);
			}
		}

		@Override
		public void onUdpDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload,
				String sessionName) {
			for (SchordChannel.StatusListener listener : mChannelListeners) {
				listener.onUdpDataReceived(fromNode, fromChannel, payloadType, payload, sessionName);
			}
		}
	};
}
