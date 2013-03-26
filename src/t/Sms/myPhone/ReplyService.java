package t.Sms.myPhone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class ReplyService extends Service  {

	private static final Class[] mStartForegroundSignature = new Class[] {
		int.class, Notification.class};
	private static final Class[] mStopForegroundSignature = new Class[] {
		boolean.class};
	private NotificationManager mNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];
	private Map<String, String> replies = new HashMap<String, String>();
	private boolean mgenerator = false;
	SmsReceiver shortMessageReceiver;
	
    public class LocalBinder extends Binder {  
        ReplyService getService() {  
            // ����Activity��������Service����������Activity��Ϳɵ���Service���һЩ���÷����͹�������  
            return ReplyService.this;  
        }  
    }  
    
	public void Register()
	{
		registerReceiver(shortMessageReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		Toast.makeText(this, "ע��ɹ�", Toast.LENGTH_LONG).show();
	}

	public void Unregister()
	{
		unregisterReceiver(shortMessageReceiver);
		Toast.makeText(this, "ע���ɹ�", Toast.LENGTH_LONG).show();
	}
	@Override
	public IBinder onBind(Intent intent) {

		Bundle extras = intent.getExtras();
		if(extras != null)
		{
			this.replies = (Map<String, String>) extras.get("REPLY");
		}
		this.mgenerator = extras.getBoolean("MOE");
		
		// ���ǲ�����ҪΪ notification.flags ���� FLAG_ONGOING_EVENT����Ϊ
		// ǰ̨����� notification.flags ����Ĭ�ϰ������Ǹ���־λ
		Notification notification = new Notification(R.drawable.ic_launcher, "������׼������.",
				System.currentTimeMillis());
		
		//��Class.forName������������´���
		Intent contentIntent = null;
		try {
			contentIntent = new Intent(getApplicationContext(),
					Class.forName("t.Sms.myPhone.MyPhoneSmsActivity"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				contentIntent, 0);
		notification.setLatestEventInfo(this, "�����ҵ��Զ��ظ�",
				"��Ů������.", pendingIntent);
		// ע��ʹ��  startForeground ��id Ϊ 0 ��������ʾ notification
		startForegroundCompat(1, notification);

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			mStartForeground = ReplyService.class.getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = ReplyService.class.getMethod("stopForeground", mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			mStartForeground = mStopForeground = null;
		}

	}

	
	@Override
	public void onDestroy() {
		Unregister();
		super.onDestroy();
		stopForegroundCompat(1);
	}

	// �Լ����Է�ʽ��ʼǰ̨����
	private void startForegroundCompat(int id, Notification n){
		if(mStartForeground != null){
			mStartForegroundArgs[0] = id;
			mStartForegroundArgs[1] = n;

			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

//			this.storeMsg("38997", "����");
			ContentResolver cr = this.getContentResolver();

			Random rand = new Random();
			shortMessageReceiver = new SmsReceiver(replies, mgenerator, rand, cr);

			Register();
			
			return;
		}
		setForeground(true);
		mNM.notify(id, n);
	}

	// �Լ����Է�ʽֹͣǰ̨����
	private void stopForegroundCompat(int id){
		if(mStopForeground != null){
			mStopForegroundArgs[0] = Boolean.TRUE;

			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return;
		}

		//  �� setForeground ֮ǰ���� cancel����Ϊ�����п�����ȡ��ǰ̨����֮��
		//  ����һ˲�䱻kill�������ʱ�� notification ����Զ�����֪ͨһ���Ƴ�
		mNM.cancel(id);
		setForeground(false);
	}

}