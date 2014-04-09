package de.tum.socialcomp.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.tum.socialcomp.android.MainActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * This class is used to dispatch messages that have been received
 * via the Google Cloud Messaging Service. This indend service
 * will then redirect messages back to the MainActivity
 * where the logic of most reactions is implemented.
 * 
 * @author Niklas Klügel
 *
 */
public class GCMIntentService extends IntentService {

	public GCMIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		

	        String action = intent.getAction();
	        if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
	            handleRegistration(intent);
	        } else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
	            handleMessage(intent);
	        }
		
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent); 
        //GCMBroadcastReceiver.completeWakefulIntent(intent);
		
	}

	private void handleMessage(Intent intent) {
		
		try {
			JSONObject gcmMessage = new JSONObject(intent.getStringExtra("message"));
			
			Log.v("Received GCM message", gcmMessage.toString());
			
			if(gcmMessage.has("type")){
				if(gcmMessage.getString("type").equals("game")){
					
					MainActivity.getInstance().receivedGameMessage(gcmMessage);
					
				} else if(gcmMessage.getString("type").equals("server")){
					MainActivity.getInstance().showLogMessage("Server (GCM): "+gcmMessage.getString("subtype"));
				}

			} 
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void handleRegistration(Intent intent) {
		Log.v(this.getClass().getName(), "registered");
		
	}
	
}
