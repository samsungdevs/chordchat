package co.mscsea.chordchat.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;

public class App extends Application {

	private NetworkService mNetworkService;
	private ArrayList<String> mChatMessageList = new ArrayList<String>();
	private ArrayList<String> mChatUserList = new ArrayList<String>();
	private HashMap<String, String> mUserMap = new HashMap<String, String>();
	private String mUsername = "";

	@Override
	public void onCreate() {
		super.onCreate();

		mNetworkService = new NetworkService(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		mNetworkService.disconnect();
		mNetworkService = null;
	}
	
	public NetworkService getNetworkService() {
		return mNetworkService;
	}

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
