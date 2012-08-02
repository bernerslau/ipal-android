package com.ipalandroid.common;

import com.ipalandroid.R;
import com.ipalandroid.R.id;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.TextView;

/**
 * This class defines some static methods and constants.
 * 
 * @author Tao Qian, DePauw Open Source Development Team
 */
public class Utilities {

	
	/**
	 * This constant specifies the sender id used to register the phone with GCM (Google Cloud for Messaging) service.
	 */
	public final static String SENDER_ID = "462734233257";
	
	/**
	 * These two constants are used as flags to indicate that the format of the
	 * user inputed text is wrong.
	 */
	public final static String STRING_FORMAT_ERROR = " format error ";
	public final static int INT_FORMAT_ERROR = -1;
	
	/**
	 * This interface stores the keys used to access the preference of this app.
	 */
	public interface SharedPreferenceKeys {
		public final static String NAME = "info";
		public final static String USERNAME = "username";
		public final static String URL = "url";
		public final static String IS_VALID = "isvalid";
	}

	/**
	 * This interface stores the identification code returned by a connection.
	 */
	public interface ConnectionResult {
		public final static int CONNECTION_ERROR = -1;
		public final static int RESULT_NOT_FOUND = 0;
		public final static int RESULT_FOUND = 1;
	}
	


	/**
	 * This method sets the text of a header.
	 * 
	 * @param header
	 *            the header used
	 * @param text
	 */
	public static void setHeaderContent(View header, String text) {
		TextView headerText = (TextView) header
				.findViewById(R.id.headerTextView);
		headerText.setText(text);
	}

	/**
	 * This method updates the shared preference. Each parameter will be stored
	 * in its respective field in the shared preference.
	 * 
	 * @param prefs
	 *            the shared preference instance used.
	 * @param isValid
	 *            be stored with the key IS_VALID.
	 * @param username
	 *            to be stored with the key USERNAME.
	 * @param url
	 *            to be stored with the key URL.
	 */
	public static synchronized void setPreference(SharedPreferences prefs,
			Boolean isValid, String username, String url) {
		Editor editor = prefs.edit();
		editor.putBoolean(SharedPreferenceKeys.IS_VALID, isValid);
		editor.putString(SharedPreferenceKeys.USERNAME, username);
		editor.putString(SharedPreferenceKeys.URL, url);
		editor.commit();
	}
}