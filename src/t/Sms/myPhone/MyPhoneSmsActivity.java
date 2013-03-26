package t.Sms.myPhone;

//import java.util.ArrayList;    
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import t.Sms.myPhone.ReplyService.LocalBinder;

import android.app.Activity;    
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

//import android.content.BroadcastReceiver;
//import android.content.ContentResolver;    
//import android.content.Context;
//import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
//import android.database.Cursor;    
import android.net.Uri;
import android.os.Bundle;    
import android.os.IBinder;
//import android.provider.ContactsContract;  
//import android.provider.ContactsContract.PhoneLookup;  
import android.telephony.*;    
//import android.util.Log;
import android.view.View;    
import android.view.View.OnClickListener;
import android.widget.*;  

public class MyPhoneSmsActivity extends Activity {
	/** Called when the activity is first created. */
	private EditText PhoneNumber = null;  
	private EditText Key1 = null;  
	private EditText Reply1 = null;  
	private EditText Key2 = null;  
	private EditText Reply2 = null;  
	private EditText Key3 = null;  
	private EditText Reply3 = null;  
	private Button startButton = null;  
	private Button closeButton = null;  
	private TextView instruction = null;  
	private CheckBox MOE = null;
	private CheckBox MGENERATER = null;
	private boolean mpoint = false;    
	private boolean mBound = false;  

	private Map<String, String> replies = new HashMap<String, String>();


	private SmsManager smsManager = null;  

	private String incomingNumber = null;  
	private boolean isMonitoring = false;  
	private ReplyService mService;

	//Intent intent = null;

	//将新号码导入到号码栏
	private void addNum(EditText phoneNumber) {
		// TODO Auto-generated method stub
		
	}  
	
	
	public void showHome() {  
		Notification notification = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());  
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://hi.baidu.com/windwindwind"));
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent , 0);  
		notification.setLatestEventInfo(getApplicationContext(), "赛尔芬自动回复", "前往主人家", contentIntent);  

		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(  
				android.content.Context.NOTIFICATION_SERVICE);  
		notificationManager.notify(R.drawable.ic_launcher, notification);  
	}  
	
	public void storeMsg(String destinationAddress, String text)
	{
		ContentValues cv = new ContentValues();
		cv.put("address", destinationAddress);
		cv.put("preson", "");
		cv.put("protocol", "0");
		cv.put("read", "0");
		cv.put("status", "-1");
		cv.put("body", text);
		getContentResolver().insert(Uri.parse("content://sms/sent"), cv);
	}
	
	public void moeShow()
	{
		
		PhoneNumber.setText("18931649853");  	
		Key1.setText("回来");
		Reply1.setText("欢迎回来~");
		Key2.setText("晚安");
		Reply2.setText("晚安~");
		Key3.setText("");
		Reply3.setText("PARTY的政策亚克西~");
		MGENERATER.setChecked(true);
		
	}
	
	public void cleanAll()
	{
		PhoneNumber.getText().clear();  	
		Key1.getText().clear();
		Reply1.getText().clear();
		Key2.getText().clear();
		Reply2.getText().clear();
		Key3.getText().clear();
		Reply3.getText().clear();
		MGENERATER.setChecked(false);
	}
	
	public void initMap()
	{
		replies.put("repNumber", PhoneNumber.getText().toString());
		replies.put(Key1.getText().toString(), Reply1.getText().toString());
		if(replies.get(Key2.getText().toString()) == null)
		replies.put(Key2.getText().toString(), Reply2.getText().toString());
		if(replies.get(Key3.getText().toString()) == null)
			replies.put(Key3.getText().toString(), Reply3.getText().toString());
	}
	
	public void serviceControl(boolean isMonitoring){  
		if(!isMonitoring){  
			startService();  
		}else{  
			stopService();  
		}  
		this.isMonitoring = !this.isMonitoring;  
	}  

	public void startService(){  
		/*
        PhoneStateListener phoneListener = new PhoneStateListener()
        {  
            @Override  
            public void onCallStateChanged(int state,String incoming)
            {  
                if(state == TelephonyManager.CALL_STATE_RINGING && isMonitoring)
                {  
                    //这里的电话号码格式类似于“12345678910”  
                    //要将其格式化成“1-234-567-8910”这样的格式才能与numberList中的字符串匹配  
                    incomingNumber = PhoneNumberUtils.formatNumber(incoming);  
               //     sendSMS();  
                }  
            }  
        };  

        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);  
      //  smsManager = SmsManager.getDefault();  
		 * 
		 */
        //这里是最重要的代码，也可以放到onStart（）方法中，即绑定服务  
		initMap();
        Intent intent = new Intent(this, ReplyService.class);   
		Bundle bundle = new Bundle();
		bundle.putSerializable("REPLY", (Serializable)replies);
		bundle.putBoolean("MOE", MGENERATER.isChecked());
		intent.putExtras(bundle);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  

		/*
		shortMessageReceiver = new SmsReceiver(PhoneNumber.getText().toString(),
				Key1.getText().toString(),Reply1.getText().toString(),
				Key2.getText().toString(),Reply2.getText().toString());
		*/
		
		
		instruction.setText("自动回复已经启动");  
	}  

	public void stopService(){  
		/* 自动回复服务是否生效取决于isMonitoring的值 
		 * 不过这里可以做更多事情，例如将引用设置为null 
		 * */  

		unbindService(mConnection);
		
		
		replies.clear();
		//smsManager.sendTextMessage(incomingNumber, null, "ok", null, null);  
		instruction.setText("自动回复已经停止");  
	}  
    /** 定交ServiceConnection，用于绑定Service的*/  
    private ServiceConnection mConnection = new ServiceConnection() {  
  
        @Override  
        public void onServiceConnected(ComponentName className,  
                IBinder service) {  
            // 已经绑定了LocalService，强转IBinder对象，调用方法得到LocalService对象  
            LocalBinder binder = (LocalBinder) service;  
            mService = binder.getService();  
            mBound = true;  
        }  
  
        @Override  
        public void onServiceDisconnected(ComponentName arg0) {  
            mBound = false;  
        }  
    };  


	/** Called when the activity is first created. */  
	/*
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		
	}
	*/
	
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.main);  
		showHome();
		/* 这里就是和main.xml中的元素建立联系的时候啦！ 
		 * */  
		this.PhoneNumber = (EditText)findViewById(R.id.PhoneNumber);  
		this.Key1 = (EditText)findViewById(R.id.Key1);  
		this.Reply1 = (EditText)findViewById(R.id.Reply1);  
		this.Key2 = (EditText)findViewById(R.id.Key2);  
		this.Reply2 = (EditText)findViewById(R.id.Reply2);  
		this.Key3 = (EditText)findViewById(R.id.Key3);  
		this.Reply3 = (EditText)findViewById(R.id.Reply3);  
		this.startButton = (Button)findViewById(R.id.startButton);  
		this.closeButton = (Button)findViewById(R.id.closeButton);  
		this.instruction = (TextView)findViewById(R.id.instruction);  
		this.MOE = (CheckBox)findViewById(R.id.MOE);
		this.MGENERATER = (CheckBox)findViewById(R.id.MGENERATER);

		OnClickListener addnumListener = null;
		addnumListener = new OnClickListener()
		{
			@Override   
			public void onClick(View v)    
			{  
				addNum(PhoneNumber);
			}

		};

		
		MOE.setOnClickListener(new CheckBox.OnClickListener()
		{
			@Override   
			public void onClick(View v)    
			{  
				if(MOE.isChecked()){  
					moeShow();
				}
				else
				{
					cleanAll();
				}
			}  
		});
		/* 监听startButton点击事件的listener 
		 * 根据isMonitoring的布尔值来决定启动或者关闭服务 
		 * */  
		startButton.setOnClickListener(new Button.OnClickListener()    
		{    
			@Override   
			public void onClick(View v)    
			{  
				if(!isMonitoring){  
					startButton.setText("停止自动回复");  		
				}else{  
					startButton.setText("开始自动回复");  
				}  
				serviceControl(isMonitoring);  
			}  
		});  

		/* 关闭按钮，退出程序 
		 * */  
		closeButton.setOnClickListener(new Button.OnClickListener()    
		{    
			@Override   
			public void onClick(View v)    
			{  
				System.exit(0);  
			}  
		});  
	}  
}