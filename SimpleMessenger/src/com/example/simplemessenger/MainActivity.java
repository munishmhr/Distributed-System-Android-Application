package com.example.simplemessenger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity{
	TextView textOut;
	Button buttonSend;
	EditText tE;
	String emulatorNo;
	int localPort;
	final static Integer serverPort = 10000;
	String TAG = "SimpleMessenger Log :";
	ServerSocket serverSocket = null;
	Socket responseFromClient ;
	String msg;
	ObjectInputStream ois;
	ObjectOutputStream objectOutputStream;
	
	void updateViewSection(View view){
		String temp = textOut.getText().toString();
		
	}
	
	String getLocalPort(){
		TelephonyManager tel = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);	
	    return portStr;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		textOut = (TextView) findViewById(R.id.setTV);
		textOut.setMovementMethod(new ScrollingMovementMethod());
		buttonSend = (Button) findViewById(R.id.bt);
		tE = (EditText) findViewById(R.id.eDT);
		
		// Server code will create new server thread for every request(Server port is fixed)
		new Thread(new Runnable(){
			public void run() {
				try {
					serverSocket = new ServerSocket(serverPort);
					while(true){
						responseFromClient = serverSocket.accept();
						ois = new ObjectInputStream(responseFromClient.getInputStream());
                		final String messageFromClient = (String)ois.readObject();
						final String temp = textOut.getText().toString();
						textOut.post(new Runnable() {
                            public void run() {
                            	textOut.setText(temp+"\n"+messageFromClient);
                            }
                        });
                		
                	}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}finally{
					try {
						serverSocket.close();
						responseFromClient.close();
						ois.close();												
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}).start();
		
		//Client call
		buttonSend.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new client().execute();
			}
		});		
	}
	
	private class client extends AsyncTask<String, String, String>{
		DataOutputStream out;
		Socket socketClient;
		String temp=null;
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			emulatorNo =getLocalPort();
			if(emulatorNo.equals("5554")){
				localPort = 11112;
			}
			else if(emulatorNo.equals("5556")){
				localPort = 11108;
			}
			else if(!emulatorNo.equals("5554") && !emulatorNo.equals("5556")){
				Log.v(TAG, "AVD portStr is neither 5554 nor 5556");
			}
			try {
				socketClient = new Socket("10.0.2.2",localPort);
				objectOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
				msg = tE.getText().toString();
				objectOutputStream.writeObject(msg);
				temp = textOut.getText().toString();
        		
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					socketClient.close();
					objectOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
			return temp;	
		}		
		 protected void onPostExecute(String messageForServer){
	   	    	TextView textView = (TextView) findViewById(R.id.setTV);
	   	    	textView.setText(messageForServer);
	   	    	textOut.setText(temp+"\n"+msg);
				tE.setText("");
	   	    }
		   
	}
}
