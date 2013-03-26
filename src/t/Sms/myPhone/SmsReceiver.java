package t.Sms.myPhone;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Map;
//import java.io.OutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

public class SmsReceiver extends BroadcastReceiver
{
	String repNumber = "";
	
	String myNumber = "15038350988";
	private SmsManager smsManager = null;     
	private Context context;  
	public int NOTIFICATION_ID = R.drawable.ic_launcher;  
	Random rand;
	private Map<String, String> income;
	private boolean mswitch;
	private Map<Integer, String> mgene = new HashMap<Integer, String>();
	private Map<Integer, String> phoneNum = new HashMap<Integer, String>();
	private ContentResolver cr;
	private String msen = "他的战力远在我之上\n" +
	"最近一堆人东方宅、东方厨的叫着，現在想想这也是理所当然的事\n" +
	"虛子(形象上)拘泥的東西还蛮多的\n" +
	"为什么提耶利亚会重了2kg? \n" +
	"德国的科学力是世界第一\n" +
	"赛尔芬也是式神的一种哟~\n" +
	"我认为H是不行的\n" +
	"人体炼成果然是被禁止的\n" +
	"只要出问题，C4炸弹都能搞定\n" +
	"目标：除了OOXX，都可以代替主人干\n" +
	"带着自己喜爱的玩偶去休假旅行当然是首选方式\n" +
	"安腾和安卓都是ANDROID哟~\n" +
	"我不萌眼镜属性的\n" +
	"这种高性能机器人一定是日本人做的\n" +
	"最近的萝莉都不萝莉了";


	public SmsReceiver(Map<String, String> in, boolean mpoint, Random random, ContentResolver resolver)
	{

		this.income = in;
		this.repNumber = in.get("repNumber");
		int i = 0;
		for(String s : msen.split("\n"))
		{
			this.mgene.put(i, s);
			i++;
		}
		for(String s : this.repNumber.split(";"))
		{
			this.phoneNum.put(i, s);
			i++;
		}
		
		this.mswitch = mpoint;
		this.rand = random;
		this.cr = resolver;
	}
/*
	public SmsReceiver(String rn, String k1, String rep1, String k2, String rep2)
	{
		this.repNumber = rn;
		this.key1 = k1;
		this.repDialog1 = rep1;
		this.key2 = k2;
		this.repDialog2 = rep2;
	}
*/
	//判断来信号码是否为需要回复的
	public boolean isRepNum(String incomingNumber)
	{
		for(Map.Entry<Integer, String> m: phoneNum.entrySet())
		{
			if (incomingNumber.contains(m.getValue()))
				return true;
		}
		return false;
	}
	
	public void showNotification(String reply, String repNumber, Intent intent) {  
		Notification notification = new Notification(R.drawable.ic_launcher, "刚刚回复了一条短信~", System.currentTimeMillis());  
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent , 0);  
		notification.setLatestEventInfo(context, repNumber, reply, contentIntent);  

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(  
				android.content.Context.NOTIFICATION_SERVICE);  
		notificationManager.notify(NOTIFICATION_ID, notification);  
	}  

	
	public void storeMsg(String destinationAddress, String text)
	{
		ContentValues cv = new ContentValues();
		cv.put("address", destinationAddress);
		cv.put("preson", "");
		cv.put("protocol", "0");
		cv.put("read", "1");
		cv.put("status", "-1");
		cv.put("body", text);
		this.cr.insert(Uri.parse("content://sms/sent"), cv);
	}
	/*
	//显示通知信息
	public void showNotification(String tickerText, String contentTitle, String contentText, int id, int resId)
	{
		NotificationManager notificationManager = (NotificationManager)Activity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(resId, tickerText, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MyPhoneSmsActivity.class),0);
		notification.setLatestEventInfo(this, contentTitle.toCharArray(), contentText, contentIntent);
		notificationManager.notify(id, notification);
	}
	 */

	//随机回复
	public String moeGene()
	{
		if (this.mswitch == true)
		{
			return this.mgene.get(rand.nextInt(this.mgene.size())) + "\n赛尔芬代主人回复~";
		}
		else
			return "";
	}

	//根据来信判断回复内容
	public String putReply(String smsText)
	{
		String temp = "";
		String blank = "";
		for(Map.Entry<String, String> m: income.entrySet())
		{
			temp = m.getKey();
			if (temp.equals("") && !m.getValue().equals(null))
				blank = m.getValue();
			else if(smsText.contains(temp))
				return m.getValue() + "\n" + this.moeGene();
		}
		if (!blank.equals(""))
		{
			return blank + "\n" + this.moeGene();
		}
		return null;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle bundle = intent.getExtras();

		this.context = context;
		//OutputStream os = openFileOutput("rec.txt", );
		if (bundle != null)
		{
			Object[] objArray = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[objArray.length];

			for (int i = 0; i < objArray.length; i++)
			{
				messages[i] = SmsMessage.createFromPdu((byte[]) objArray[i]);
				String incomingNumber = messages[i].getOriginatingAddress();
				String smsText = messages[i].getDisplayMessageBody();
				//smsManager.sendTextMessage(incomingNumber, null, smsText, null, null);  

				//得到回复内容
				String reply = this.putReply(smsText);
				//合法则发送短信
				if(PhoneNumberUtils.isGlobalPhoneNumber(incomingNumber) && isRepNum(incomingNumber))
				{        			
					PendingIntent pi=
						PendingIntent.getActivity(context, 0, new Intent(context,SmsReceiver.class), 0);
					smsManager = SmsManager.getDefault();
					if(reply != null)
					{
						smsManager.sendTextMessage(incomingNumber, null, reply, pi, null);
						//MyPhoneSmsActivity act = new MyPhoneSmsActivity();

						//act.showNotification("发了！", incomingNumber, "已发送", R.drawable.ic_launcher, R.drawable.ic_launcher);
						//  	    	smsManager.sendTextMessage(myNumber, null, reply, pi, null);
						showNotification(reply, incomingNumber, intent);
						Toast.makeText(
								context, //上下文
								"发了！\n" + reply, //提示内容
								5000						//信息显示时间
						).show();               
					}
					else
					{
						Toast.makeText(
								context, //上下文
								"这条没关键字", //提示内容
								5000						//信息显示时间
						).show();  
					}
				}

				else
				{//不合法则提示
					Toast.makeText(
							context, //上下文
							"这条没回", //提示内容
							5000						//信息显示时间
					).show();                		 
				}  

			}				
		}
	}

}
