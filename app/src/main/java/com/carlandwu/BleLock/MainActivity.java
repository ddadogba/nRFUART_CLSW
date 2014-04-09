/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carlandwu.BleLock;




import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend,btnCarl,btnLi,btnKa,btnKi,btnHoi,btnFai;
    private Switch swhLock, swhUnlock;
    private MenuItem actionBtnConnect,viewHideLog;
    private EditText edtMessage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        /*btnCarl=(Button) findViewById(R.id.button);
        btnLi=(Button) findViewById(R.id.button2);
        btnKa=(Button) findViewById(R.id.button3);
        btnKi=(Button) findViewById(R.id.button4);*/
        btnHoi=(Button) findViewById(R.id.button6);
        btnFai=(Button) findViewById(R.id.button7);
        swhLock=(Switch) findViewById(R.id.switch1);
        swhUnlock=(Switch) findViewById(R.id.switch2);
        actionBtnConnect=(MenuItem) findViewById(R.id.action_connect);
        viewHideLog=(MenuItem) findViewById(R.id.action_log);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

     
       
        // Handler Disconnect & Connect button
        /*btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }
            }
        });*/

        swhLock.setChecked(true);   // set the switch to ON
// attach a listener to check for changes in state
        swhLock.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {

                    String message = "AutolockEnabled";
                    byte[] value;
                    try{
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                    String message = "AutolockDisabled";
                    byte[] value;
                    try{
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }});

        swhUnlock.setChecked(true);   // set the switch to ON
// attach a listener to check for changes in state
        swhUnlock.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {

                    String message = "AutoUnlockEnabled";
                    byte[] value;
                    try{
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                    String message = "AutoUnlockDisabled";
                    byte[] value;
                    try{
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        /*btnCarl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View u) {
                String message = "AutolockEnabled";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    //
                    e.printStackTrace();
                }
            }
        });

        btnLi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View w) {
                String message = "AutolockDisabled";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    //
                    e.printStackTrace();
                }
            }
        });

        btnKa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View q) {
                String message = "AutoUnlockEnabled";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    //
                    e.printStackTrace();
                }
            }
        });

        btnKi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View i) {
                String message = "AutoUnlockDisabled";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    //
                    e.printStackTrace();
                }
            }
        });*/


        btnHoi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View k) {
                String message = "Lock";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        btnFai.setOnClickListener(new View.OnClickListener() {
            public void onClick(View jji) {
                String message = "Unlock";
                byte[] value;
                try{
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // Handler Send button  
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        });
     
        // Set initial UI state
        
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        if (messageListView.getVisibility()==View.VISIBLE) {
            MenuItem viewHideLog = menu.findItem(R.id.action_log);
            viewHideLog.setTitle("Hide Log");
            if (btnConnectDisconnect.getText().equals("Disconnect")) {
                MenuItem conDisCon2 = menu.findItem(R.id.action_connect);
                conDisCon2.setTitle("DISCONNECT");
                if (btnConnectDisconnect.getText().equals("Connect")) {
                    MenuItem conDisCon3 = menu.findItem(R.id.action_connect);
                    conDisCon3.setTitle("CONNECT");
                }
            }
        } else if (messageListView.getVisibility()==View.GONE) {
            MenuItem viewHideLog = menu.findItem(R.id.action_log);
            viewHideLog.setTitle("View Log");
            if (btnConnectDisconnect.getText().equals("Disconnect")) {
                MenuItem conDisCon2 = menu.findItem(R.id.action_connect);
                conDisCon2.setTitle("DISCONNECT");
                if (btnConnectDisconnect.getText().equals("Connect")) {
                    MenuItem conDisCon3 = menu.findItem(R.id.action_connect);
                    conDisCon3.setTitle("CONNECT");
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //handle action buttons
    public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {

        case R.id.action_connect:
            if (!mBtAdapter.isEnabled()) {
                Log.i(TAG, "onClick - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
            else {
                if (btnConnectDisconnect.getText().equals("Connect")){

                    //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                    Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                } else {
                    //Disconnect button pressed
                    if (mDevice!=null)
                    {
                        mService.disconnect();

                    }
                }
            }
            return true;

        case R.id.action_log:
            if (messageListView.getVisibility()==View.VISIBLE) {
                messageListView.setVisibility(View.GONE);
                edtMessage.setVisibility(View.GONE);
                btnSend.setVisibility(View.GONE);
                /*btnCarl.setVisibility(View.GONE);
                btnLi.setVisibility(View.GONE);
                btnKa.setVisibility(View.GONE);
                btnKi.setVisibility(View.GONE);*/
                invalidateOptionsMenu();
            } else if (messageListView.getVisibility()==View.GONE) {
                messageListView.setVisibility(View.VISIBLE);
                edtMessage.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);
                /*btnCarl.setVisibility(View.VISIBLE);
                btnLi.setVisibility(View.VISIBLE);
                btnKa.setVisibility(View.VISIBLE);
                btnKi.setVisibility(View.VISIBLE);*/
                invalidateOptionsMenu();
            }   return true;

        default:
            return super.onOptionsItemSelected(item);
    }
}


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             invalidateOptionsMenu();
                             edtMessage.setEnabled(true);
                             btnSend.setEnabled(true);
                             /*btnCarl.setEnabled(true);
                             btnLi.setEnabled(true);
                             btnKa.setEnabled(true);
                             btnKi.setEnabled(true);*/
                             btnHoi.setEnabled(true);
                             btnFai.setEnabled(true);
                             swhLock.setEnabled(true);
                             swhUnlock.setEnabled(true);
                             ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                             listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             mState = UART_PROFILE_CONNECTED;

                         String message = mBtAdapter.getAddress();
                         byte[] value;
                         try{
                             value = message.getBytes("UTF-8");
                             mService.writeRXCharacteristic(value);
                             //Update the log with time stamp
                             //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                             messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             edtMessage.setText("");
                         } catch (UnsupportedEncodingException lm) {
                             // TODO Auto-generated catch block
                             lm.printStackTrace();
                         }

                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             invalidateOptionsMenu();
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                         	String text = new String(txValue, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        	 	listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        	
                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
                            

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
}
