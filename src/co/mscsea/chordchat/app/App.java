package co.mscsea.chordchat.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class App extends Application {

	private boolean mIsServiceBound = false;
	private NetworkService mNetworkService;
	private ArrayList<String> mChatMessageList = new ArrayList<String>();
	private ArrayList<String> mChatUserList = new ArrayList<String>();
	private HashMap<String, String> mUserMap = new HashMap<String, String>();
	private String mUsername = "";

	@Override
	public void onCreate() {
		super.onCreate();

		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		if (mIsServiceBound) {
			mNetworkService.destroy();
			unbindService(mServiceConnection);
		}
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mNetworkService = null;
			mIsServiceBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder binder) {
			mNetworkService = ((NetworkService.MyBinder) binder).getService();
			mIsServiceBound = true;
		}
	};

	public ArrayList<String> getChatMessageList() {
		return mChatMessageList;
	}

	public void addChatMessage(String message) {
		mChatMessageList.add(message);
	}

	public void removeAllChatMessages() {
		mChatMessageList.clear();
	}

	public ArrayList<String> getChatUserList() {
		return mChatUserList;
	}

	public void addChatUser(String id, String username) {
		if (!mUserMap.containsKey(id)) {
			mChatUserList.add(username);
		}
		mUserMap.put(id, username);
	}

	public void removeChatUser(String id) {
		String username = mUserMap.get(id);
		if (username != null) {
			mChatUserList.remove(username);
			mUserMap.remove(id);
		}
	}

	public void removeAllChatUsers() {
		mChatUserList.clear();
		mUserMap.clear();
	}

	public void clearChatData() {
		mChatMessageList.clear();
		mChatUserList.clear();
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public String getUsername() {
		return mUsername;
	}

	public String getUsername(String nodeId) {
		String username = mUserMap.get(nodeId);
		return username != null ? username : "";
	}
}
