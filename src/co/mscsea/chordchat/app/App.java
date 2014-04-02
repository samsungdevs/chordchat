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
import java.util.HashMap;

import com.samsung.android.sdk.groupplay.Sgp;
import com.samsung.android.sdk.groupplay.SgpGroupPlay;
import com.samsung.android.sdk.groupplay.SgpGroupPlay.SgpConnectionStatusListener;

import android.app.Application;

public class App extends Application implements SgpConnectionStatusListener {

	private NetworkService mNetworkService;
	private ArrayList<String> mChatMessageList = new ArrayList<String>();
	private ArrayList<String> mChatUserList = new ArrayList<String>();
	private HashMap<String, String> mUserMap = new HashMap<String, String>();
	private String mUsername = "";
	
	private Sgp sgp;
	private SgpGroupPlay sdk;

	@Override
	public void onCreate() {
		super.onCreate();

		mNetworkService = new NetworkService(this);
		
		sgp = new Sgp();
		try {
			sgp.initialize(getApplicationContext());
			new SgpGroupPlay(this).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public SgpGroupPlay getGroupPlay() {
		return sdk;
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

	@Override
	public void onConnected(SgpGroupPlay sdk) {
		this.sdk = sdk;
		if (sdk.hasSession()) {
			sdk.setParticipantInfo(true);
		}
	}

	@Override
	public void onDisconnected() {
		if (sdk != null) {
			sdk.setParticipantInfo(false);
			sdk = null;
		}
	}
}
