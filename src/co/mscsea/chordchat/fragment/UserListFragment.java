package co.mscsea.chordchat.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import co.mscsea.chordchat.R;
import co.mscsea.chordchat.app.App;

public class UserListFragment extends ListFragment {
	
	private ArrayAdapter<String> mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		
		View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
		
		App app = (App) getActivity().getApplication();
		
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, app.getChatUserList());
		setListAdapter(mAdapter);
		
		return rootView;
	}
	
	public void refreshUserList() {
		mAdapter.notifyDataSetChanged();
	}
}
