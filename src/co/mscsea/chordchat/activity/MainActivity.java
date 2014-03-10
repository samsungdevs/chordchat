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

package co.mscsea.chordchat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import co.mscsea.chordchat.R;
import co.mscsea.chordchat.app.App;
import co.mscsea.chordchat.app.NetworkService;
import co.mscsea.chordchat.app.NetworkService.ConnectionState;
import co.mscsea.chordchat.fragment.ChatFragment;
import co.mscsea.chordchat.fragment.UserListFragment;

import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;

public class MainActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ChatFragment mChatFragment;
	private UserListFragment mUserListFragment;

	private boolean mIsServiceBound = false;
	private NetworkService mNetworkService;
	private boolean mIsBackKeyPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
					R.string.drawer_close) {
				public void onDrawerClosed(View view) {
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					invalidateOptionsMenu();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}

		mChatFragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.chat_fragment);
		mUserListFragment = (UserListFragment) getFragmentManager().findFragmentById(R.id.drawer_fragment);

		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (mDrawerLayout != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mDrawerLayout != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerLayout != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_connect: {
			askForUsername();
			return true;
		}
		case R.id.action_disconnect: {
			disconnect();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mIsBackKeyPressed) {
			((App) getApplication()).removeAllChatMessages();
			((App) getApplication()).removeAllChatUsers();
			((App) getApplication()).setUsername("");
			mNetworkService.disconnect();
		}

		mNetworkService.removeChannelListener(mChannelListener);
		mNetworkService.removeManagerListener(mManagerListener);

		if (mIsServiceBound) {
			unbindService(mServiceConnection);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		mIsBackKeyPressed = true;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mNetworkService = null;
			mIsServiceBound = false;

			mNetworkService.removeChannelListener(mChannelListener);
			mNetworkService.removeManagerListener(mManagerListener);
			
			invalidateOptionsMenu();
		}

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder binder) {
			mNetworkService = ((NetworkService.MyBinder) binder).getService();
			mIsServiceBound = true;

			mNetworkService.addChannelListener(mChannelListener);
			mNetworkService.addManagerListener(mManagerListener);
			
			invalidateOptionsMenu();
		}
	};

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemConnect = menu.findItem(R.id.action_connect);
		MenuItem itemDisconnect = menu.findItem(R.id.action_disconnect);

		ConnectionState state = ConnectionState.DISCONNECTED;
		if (mNetworkService != null) {
			state = mNetworkService.getState();
		}
		
		switch (state) {
			case CONNECTED: {
				itemConnect.setVisible(false);
				itemConnect.setEnabled(true);
				itemDisconnect.setVisible(true);
				break;
			}
			case CONNECTING: {
				itemConnect.setVisible(true);
				itemConnect.setEnabled(false);
				itemDisconnect.setVisible(false);
				break;
			}
			case DISCONNECTED: {
				itemConnect.setVisible(true);
				itemConnect.setEnabled(true);
				itemDisconnect.setVisible(false);
				break;
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
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

	private void connect() {
		((App) getApplication()).clearChatData();
		((App) getApplication()).removeAllChatUsers();
		mChatFragment.setSendButtonEnabled(true);
		mNetworkService.connect();
		invalidateOptionsMenu();
	}

	private void disconnect() {
		mChatFragment.setSendButtonEnabled(false);
		mNetworkService.disconnect();
		invalidateOptionsMenu();
	}

	private void askForUsername() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.input_username));

		final EditText input = new EditText(this);
		input.setText(((App) getApplication()).getUsername());
		builder.setView(input);

		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String username = input.getText().toString();
				if (username.trim().length() > 0) {
					((App) getApplication()).setUsername(username);
					connect();
				} else {
					displayDialog(getString(R.string.error_empty_username));
				}
			}
		});

		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}

	public boolean sendData(String toNode, String type, String data) {
		SchordChannel channel = mNetworkService.getChannel();
		if (channel == null)
			return false;

		byte[][] bytes = new byte[1][];
		bytes[0] = data.getBytes();
		// Send data to the destination node specified by toNode.
		// Data will be sent to all connected nodes if toNode is null.
		// <task>
		if (toNode == null) {
			channel.sendDataToAll(type, bytes);
		} else {
			channel.sendData(toNode, type, bytes);
		}
		// </task>

		return true;
	}

	public boolean sendData(String type, String data) {
		return sendData(null, type, data);
	}

	public void sendMessage(String message) {
		if (sendData(NetworkService.CHORD_TYPE_MESSAGE, message)) {
			displayMessage(String.format(getString(R.string.me), message));
		}
	}

	public void sendUsername(String toNode, String username) {
		sendData(toNode, NetworkService.CHORD_TYPE_USERNAME, username);
	}

	private SchordManager.StatusListener mManagerListener = new SchordManager.StatusListener() {

		@Override
		public void onStopped(int reason) {
			if (reason == STOPPED_BY_USER) {
				invalidateOptionsMenu();
			}
		}

		@Override
		public void onStarted(String name, int reason) {
			if (reason == STARTED_BY_USER) {
				invalidateOptionsMenu();
			}
		}
	};

	private void displayMessage(String message) {
		((App) getApplication()).addChatMessage(message);
		mChatFragment.displayMessage(message);
	}

	private void userJoin(final String id, final String username) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((App) getApplication()).addChatUser(id, username);
				mUserListFragment.refreshUserList();
			}
		});
	}

	private void userLeft(final String id) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((App) getApplication()).removeChatUser(id);
				mUserListFragment.refreshUserList();
			}
		});
	}

	private SchordChannel.StatusListener mChannelListener = new SchordChannel.StatusListener() {

		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			String username = ((App) getApplication()).getUsername(fromNode);
			displayMessage(String.format(getString(R.string.has_left), username));
			userLeft(fromNode);
		}

		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {
			sendUsername(fromNode, ((App) getApplication()).getUsername());
		}

		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload) {
			if (payloadType.equals(NetworkService.CHORD_TYPE_MESSAGE)) {
				String message = new String(payload[0]);
				String username = ((App) getApplication()).getUsername(fromNode);
				displayMessage(String.format(getString(R.string.they), username, message));
			} else if (payloadType.equals(NetworkService.CHORD_TYPE_USERNAME)) {
				String username = new String(payload[0]);
				userJoin(fromNode, username);
				displayMessage(String.format(getString(R.string.has_joined), username));
			}
		}

		@Override
		public void onMultiFilesWillReceive(String fromNode, String fromChannel, String fileName, String taskId,
				int totalCount, String fileType, long fileSize) {

		}

		@Override
		public void onMultiFilesSent(String toNode, String toChannel, String fileName, String taskId, int index,
				String fileType) {

		}

		@Override
		public void onMultiFilesReceived(String fromNode, String fromChannel, String fileName, String taskId,
				int index, String fileType, long fileSize, String tmpFilePath) {

		}

		@Override
		public void onMultiFilesFinished(String node, String channel, String taskId, int reason) {

		}

		@Override
		public void onMultiFilesFailed(String node, String channel, String fileName, String taskId, int index,
				int reason) {

		}

		@Override
		public void onMultiFilesChunkSent(String toNode, String toChannel, String fileName, String taskId, int index,
				String fileType, long fileSize, long offset, long chunkSize) {

		}

		@Override
		public void onMultiFilesChunkReceived(String fromNode, String fromChannel, String fileName, String taskId,
				int index, String fileType, long fileSize, long offset) {

		}

		@Override
		public void onFileWillReceive(String fromNode, String fromChannel, String fileName, String hash,
				String fileType, String exchangeId, long fileSize) {

		}

		@Override
		public void onFileSent(String toNode, String toChannel, String fileName, String hash, String fileType,
				String exchangeId) {

		}

		@Override
		public void onFileReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, String tmpFilePath) {

		}

		@Override
		public void onFileFailed(String node, String channel, String fileName, String hash, String exchangeId,
				int reason) {

		}

		@Override
		public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, long offset, long chunkSize) {

		}

		@Override
		public void onFileChunkReceived(String fromNode, String fromChannel, String fileName, String hash,
				String fileType, String exchangeId, long fileSize, long offset) {

		}

		@Override
		public void onUdpDataDelivered(String toNode, String toChannel, String reqId) {

		}

		@Override
		public void onUdpDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload,
				String sessionName) {

		}
	};
}
