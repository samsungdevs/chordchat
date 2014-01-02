package co.mscsea.chordchat;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ListActivity implements OnClickListener {

	private Button connect;
	private Button disconnect;
	private Button send;
	private EditText message;
	
	private ArrayAdapter<String> adapter;
	
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.connect: {
				
				break;
			}
			case R.id.disconnect: {
				
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
		displayMessage(text);
	}
	
	private void displayMessage(String text) {
		adapter.add(text);
		adapter.notifyDataSetChanged();
	}
}
