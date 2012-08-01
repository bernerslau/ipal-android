package com.ipalandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.ipalandroid.Utilities.SharedPreferenceKeys;
import com.ipalandroid.login.LoginActivity;
import com.ipalandroid.questionview.QuestionViewActivity;

public class GCMIntentService extends GCMBaseIntentService {
	
	public GCMIntentService() {
        super(Utilities.SENDER_ID);
        //Log.d("GCMIntentService", senderId);
    }
	
	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context c, Intent arg1) {
		// TODO Auto-generated method stub
		//Send an intent to QuestionView activity.
		//So that it will refresh
		//TODO: need to check whether the users are logged in or not
		//Intent refreshIntent = new Intent(c,QuestionViewActivity.class);
		//c.startActivity(refreshIntent);
		// This is a bug since we can't call startActivity from outside an activity context
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		//TODO: send the regID to the server
		LoginActivity.sendToServer(regId);
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.w("RegID Unregister", arg1+"   ");
		
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// TODO Auto-generated method stub
		return super.onRecoverableError(context, errorId);
	}
	
	
	
}
