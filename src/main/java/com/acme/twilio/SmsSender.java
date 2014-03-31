package com.acme.twilio;

import java.util.HashMap;
import java.util.Map;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Sms;

public class SmsSender
{
	public static final int THREAD_COUNT = 5;
	public static final int MESSAGE_COUNT = 100;

	public static void main(String[] args) throws Exception
	{
		SmsSender sender = new SmsSender();
		sender.go();
	}

	private void go() throws TwilioRestException
	{
		long startTime = System.currentTimeMillis();

		ThreadGroup group = new ThreadGroup("Senders");

		for (int i = 0; i < THREAD_COUNT; i++)
		{
			Thread t = new Thread(group, new SenderThread("Thread " + i, MESSAGE_COUNT));
			t.start();
		}

		while (group.activeCount() > 0)
			;

		long elapsedTime = System.currentTimeMillis() - startTime;

		System.out.println(String.format("Done sending, elapsed time was  %dms (%d messages/second)", elapsedTime, (1000 * THREAD_COUNT * MESSAGE_COUNT) / elapsedTime));
	}

	public static class SenderThread implements Runnable
	{
		private int count;

		private String name;

		private SmsFactory smsFactory;

		public SenderThread(String name, int count)
		{
			this.count = count;

			this.name = name;

			String accountSid = System.getProperty("ACCOUNT_SID");

			String authToken = System.getProperty("AUTH_TOKEN");
			
			// Create a rest client
			TwilioRestClient client = new TwilioRestClient(accountSid, authToken);

			// Get the main account (The one we used to authenticate the client)
			Account mainAccount = client.getAccount();

			smsFactory = mainAccount.getSmsFactory();
		}

		public void run()
		{
			for (int i = 0; i < count; i++)
			{
				try
				{
					sendMessage("+15005550006", "6128675309", "This is a test message " + i + "!");
				}
				catch (TwilioRestException e)
				{
					System.err.println("An exception occurred: " + e.getErrorMessage());
				}
			}
		}

		private void sendMessage(String from, String to, String body) throws TwilioRestException
		{
			final long startTime = System.currentTimeMillis();

			Map<String, String> smsParams = new HashMap<String, String>();
			smsParams.put("To", to);
			smsParams.put("From", from);
			smsParams.put("Body", body);
			Sms message = smsFactory.create(smsParams);

			System.out.println(String.format("%s: message sent to %s got SID %s, elapsed time was  %dms", name, to, message.getSid(), System.currentTimeMillis() - startTime));
		}
	}
}
