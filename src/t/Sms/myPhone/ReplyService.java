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
            // 返回Activity所关联的Service对象，这样在Activity里，就可调用Service里的一些公用方法和公用属性  
            return ReplyService.this;  
        }  
    }  
    
	public void Register()
	{
		registerReceiver(shortMessageReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		Toast.makeText(this, "注册成功", Toast.LENGTH_LONG).show();
	}

	public void Unregister()
	{
		unregisterReceiver(shortMessageReceiver);
		Toast.makeText(this, "注销成功", Toast.LENGTH_LONG).show();
	}
	@Override
	public IBinder onBind(Intent intent) {

		Bundle extras = intent.getExtras();
		if(extras != null)
		{
			this.replies = (Map<String, String>) extras.get("REPLY");
		}
		this.mgenerator = extras.getBoolean("MOE");
		
		// 我们并不需要为 notification.flags 设置 FLAG_ONGOING_EVENT，因为
		// 前台服务的 notification.flags 总是默认包含了那个标志位
		Notification notification = new Notification(R.drawable.ic_launcher, "赛尔芬准备就绪.",
				System.currentTimeMillis());
		
		//用Class.forName（），不会打开新窗口
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
		notification.setLatestEventInfo(this, "赛尔芬的自动回复",
				"少女工作中.", pendingIntent);
		// 注意使用  startForeground ，id 为 0 将不会显示 notification
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

	// 以兼容性方式开始前台服务
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

//			this.storeMsg("38997", "哈哈");
			ContentResolver cr = this.getContentResolver();

			Random rand = new Random();
			shortMessageReceiver = new SmsReceiver(replies, mgenerator, rand, cr);

			Register();
			
			return;
		}
		setForeground(true);
		mNM.notify(id, n);
	}

	// 以兼容性方式停止前台服务
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

		//  在 setForeground 之前调用 cancel，因为我们有可能在取消前台服务之后
		//  的那一瞬间被kill掉。这个时候 notification 便永远不会从通知一栏移除
		mNM.cancel(id);
		setForeground(false);
	}

}