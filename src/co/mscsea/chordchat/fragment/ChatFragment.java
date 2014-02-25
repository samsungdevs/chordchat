package co.mscsea.chordchat.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import co.mscsea.chordchat.R;
import co.mscsea.chordchat.activity.MainActivity;
import co.mscsea.chordchat.app.App;

public class ChatFragment extends ListFragment implements OnClickListener {
	
	private ArrayAdapter<String> mAdapter;
	private Button mSendButton;
	private EditText mMessageEditor;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
	
		App app = (App) getActivity().getApplication();
		
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, app.getChatMessageList());
		setListAdapter(mAdapter);
		
		mSendButton = (Button) rootView.findViewById(R.id.send);
		mSendButton.setOnClickListener(this);
		
		mMessageEditor = (EditText) rootView.findViewById(R.id.message);
		
		return rootView;
	}
	
	public void displayMessage(String message) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			String message = mMessageEditor.getText().toString().trim();
			if (!TextUtils.isEmpty(message)) {
				((MainActivity) getActivity()).sendMessage(message);
				mMessageEditor.setText("");
			}
		}
	}
	
	public void setSendButtonEnabled(boolean enabled) {
		mSendButton.setEnabled(enabled);
	}
}
