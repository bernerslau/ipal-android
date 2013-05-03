package com.ipalandroid.login;

import java.io.IOException;

import org.jsoup.Jsoup;

import com.google.android.gcm.GCMRegistrar;
import com.ipalandroid.GCMRegistrationManager;
import com.ipalandroid.MoodleServerIntentService;
import com.ipalandroid.R;
import com.ipalandroid.common.Utilities;
import com.ipalandroid.common.Utilities.ConnectionResult;
import com.ipalandroid.common.Utilities.SharedPreferenceKeys;
import com.ipalandroid.questionview.QuestionViewActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//Comment for commit.
/**
 * This activity is responsible for displaying an account setting form and
 * reading user input.
 * 
 * @author Tao Qian, DePauw Open Source Development Team
 * @author Ngoc Nguyen, DePauw Open Source Development Team
 */
public class LoginActivity extends Activity {

	//String constant used to replace the real password in the UI.
	private static final String VALID_PASSWORD = "password";
	//Keys used to identify extra data passed with intent.
	public static final String PASSCODE_EXTRA = "passcode_extra";
	public static final String URL_EXTRA = "url_extral";
	public static final String USERNAME_EXTRA = "username_extra";
	//Preference used to store user data.
	private SharedPreferences prefs;
	//UI elements
	private TextView loginInfoTextView;
	private EditText userNameEditText;
	private EditText urlEditText;
	private EditText passwordEditText;
	private EditText passcodeEditText;
	private CheckBox saveInfoCheckBox;
	private Button confirmButton;
	private Button applyButton;
	//User input. 
	private static String url;
	private static String username;
	private String password;
	private Boolean isValid;
	private UserValidater userValidater;
	private static int currentPasscode; 
	//String used in UI
	private String LOGIN_INFO_VALID;
	private String LOGIN_INFO_INVALID;
	private String APPLY_BUTTON_APPLY;
	private String APPLY_BUTTON_CHANGE;
	private String INVALID_URL_MESSAGE;
	private String INVALID_USER_MESSAGE;
	private String INVALID_PASSCODE_FORMAT_MESSAGE;
	private String CONNECTION_ERROR_MESSAGE;
	private String CHECKING_USER_MESSAGE;
	private RelativeLayout loginLayout;
	private InputMethodManager inputManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		getHistorySettings();
		initializeUIElements();
		getStringResources();
		setUpElements();
		userValidater = null;
		
	}

	/**
	 * This method sets up the UI elements dynamically. It should be called
	 * after calling setContentView(), getHistorySettings(),
	 * initializeUIElements() and getStringResources().
	 */
	private void setUpElements() {
		// Set the text of the header.
		Utilities.setHeaderContent(findViewById(R.id.header),
				getString(R.string.login_header_text));

		// If the user name and password pair was already validated.
		if (isValid) {
			saveInfoCheckBox.setChecked(true);
			password = VALID_PASSWORD;
		}

		reloadForm(isValid);
		
		
		//Creates an input manager. Added by Kevin Courtade
		inputManager = (InputMethodManager) LoginActivity.this.
				getSystemService(LoginActivity.this.INPUT_METHOD_SERVICE); 
		
		loginLayout.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v){
				//Requests focus from other elements
				loginLayout.requestFocus();
				//Minimizes the virtual keyboard.
				inputManager.hideSoftInputFromWindow(
				        LoginActivity.this.getCurrentFocus().getWindowToken(),
				        InputMethodManager.HIDE_NOT_ALWAYS); 
			}
			
		});
		

		applyButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// If the user is going to change account settings.
				if (isValid) {
					isValid = false;
					username = "";
					password = "";
					reloadForm(isValid);
					return;
				}

				// If the user is attempting to validate/save the account
				// settings.
				// Get settings from the UI.
				username = userNameEditText.getText().toString();
				password = passwordEditText.getText().toString();
				url = urlEditText.getText().toString();
				//Check whether the user data is valid in a separate thread.
				UserChecker userChecker = new UserChecker();
				userChecker.execute(username,password,url);
			}
		});

		confirmButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Check whether the user is valid, if not, notify the user.
				if (!isValid) {
					Toast.makeText(LoginActivity.this, INVALID_USER_MESSAGE,
							Toast.LENGTH_SHORT).show();
					return;
				}
				// Start the QuestionViewActivity
				int passcode = UserValidater.validatePasscode(passcodeEditText.getText().toString());
				if(passcode<0)
				{
					Toast.makeText(getApplicationContext(), INVALID_PASSCODE_FORMAT_MESSAGE, Toast.LENGTH_SHORT).show();
					return;
				}
				
				//Register the device to GCM server when the user have a valid passcode and username.
				//GCMRegistrationManager.unregisterGCM(v.getContext());
				GCMRegistrationManager.registerGCM(v.getContext());
				//Don't need to send to Server here. Send it in onResume() of QuestionViewActivity
				//sendToServer();
				Intent intent = new Intent(LoginActivity.this,
						QuestionViewActivity.class);
				intent.putExtra(PASSCODE_EXTRA, passcode);
				intent.putExtra(USERNAME_EXTRA, username);
				intent.putExtra(URL_EXTRA, url);
				startActivity(intent);
			}
		});
	}

	/**
	 * This methods initiates the preference instance and retrieve history
	 * settings from the preference.
	 */
	private void getHistorySettings() {
		prefs = getPreferences(MODE_PRIVATE);
		username = prefs.getString(SharedPreferenceKeys.USERNAME, "");
		url = prefs.getString(SharedPreferenceKeys.URL, "");
		isValid = prefs.getBoolean(SharedPreferenceKeys.IS_VALID, false);
		password = "";
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// Store user info on exit.
		if (!saveInfoCheckBox.isChecked())
			isValid = false;
		Utilities.setPreference(prefs, isValid, username, url);
		super.onDestroy();
	}

	/**
	 * This method load date into the form. It also enable/disable text fields
	 * depending on whether the user data is valid.
	 * 
	 * @param isValid
	 *            true if the user data is valid. false otherwise.
	 */
	private void reloadForm(Boolean isValid) {
		/**
		 * Because of a bug in Android,
		 * we have to use setFocusable in combination with setEnabled
		 * to disable an EditText.
		 * However, because of another bug, setFocusable(true) will
		 * not make a EditText focusable again, we need to use
		 * setFocusableInTouchMode(true) instead.
		 */
		userNameEditText.setEnabled(!isValid);
		urlEditText.setEnabled(!isValid);
		passwordEditText.setEnabled(!isValid);
		if (isValid) {
			userNameEditText.setFocusable(false);
			urlEditText.setFocusable(false);
			passwordEditText.setFocusable(false);
			applyButton.setText(APPLY_BUTTON_CHANGE);
			loginInfoTextView.setText(LOGIN_INFO_VALID + username);
			passcodeEditText.requestFocus();
		} else {
			userNameEditText.setFocusableInTouchMode(true);
			urlEditText.setFocusableInTouchMode(true);
			passwordEditText.setFocusableInTouchMode(true);
			userNameEditText.requestFocus();
			applyButton.setText(APPLY_BUTTON_APPLY);
			loginInfoTextView.setText(LOGIN_INFO_INVALID);
		}
		userNameEditText.setText(username);
		urlEditText.setText(url);
		passwordEditText.setText(password);
	}

	/**
	 * This method initializes all UI elements.
	 */
	private void initializeUIElements() {
		loginInfoTextView = (TextView) findViewById(R.id.loginInfoTextView);
		userNameEditText = (EditText) findViewById(R.id.userNameEditText);
		urlEditText = (EditText) findViewById(R.id.urlEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		saveInfoCheckBox = (CheckBox) findViewById(R.id.saveInfoCheckBox);
		confirmButton = (Button) findViewById(R.id.confirmButton);
		applyButton = (Button) findViewById(R.id.applyAccountSettingsButton);
		passcodeEditText = (EditText) findViewById(R.id.passcodeEditText);
		loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
	}

	/**
	 * This method gets the string resources used in this activity.
	 */
	private void getStringResources() {
		LOGIN_INFO_VALID = getString(R.string.login_info_valid_message);
		LOGIN_INFO_INVALID = getString(R.string.login_info_invalid_message);
		APPLY_BUTTON_APPLY = getString(R.string.apply_button_apply_text);
		APPLY_BUTTON_CHANGE = getString(R.string.apply_button_change_text);
		INVALID_URL_MESSAGE = getString(R.string.invalid_url_message);
		INVALID_USER_MESSAGE = getString(R.string.invalid_account_settings_message);
		INVALID_PASSCODE_FORMAT_MESSAGE = getString(R.string.invalid_passcode_format_message);
		CONNECTION_ERROR_MESSAGE = getString(R.string.connection_problem_message);
		CHECKING_USER_MESSAGE = getString(R.string.checking_user_message);
	}
	
	/**
	 * AsyncTask class used for validating the user. It displays
	 * a progress dialog while checking at the background.
	 */
	private class UserChecker extends AsyncTask<String, Void, Integer >
	{
		Boolean dialogCanceled;
		ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setMessage(CHECKING_USER_MESSAGE);
			dialogCanceled = false;
			progressDialog.setOnCancelListener(new OnCancelListener() {
				
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					dialogCanceled = true;
				}
			});
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			userValidater = new UserValidater(params[0], params[1],params[2]);
			return userValidater.validateUser();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(dialogCanceled)
				return;
			isValid = false;
			if(result == ConnectionResult.RESULT_FOUND)
				isValid = true;
			progressDialog.dismiss();
			// If not valid, notify the user.
			if (isValid)
			{
				username = userValidater.getUsername();
				url = userValidater.getURL();
				reloadForm(isValid);
			}
			else if( result == ConnectionResult.RESULT_NOT_FOUND)
				Toast.makeText(LoginActivity.this, INVALID_USER_MESSAGE,
						Toast.LENGTH_SHORT).show();
			else if(result == ConnectionResult.INVALID_URL)
				Toast.makeText(LoginActivity.this, INVALID_URL_MESSAGE, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(LoginActivity.this, CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/**
	 * This method send the username, passcode and the GCm reg ID to the server 
	 * using MoodleServerIntentService. The GCM regId is used to send push 
	 * notifications to the phone.
	 */
	private void sendToServer() {
		String regId = GCMRegistrar.getRegistrationId(this);
		if (!regId.equals("")) {
			Intent removeIntent = new Intent(this, MoodleServerIntentService.class);
			removeIntent.putExtra(MoodleServerIntentService.JOB, MoodleServerIntentService.JOB_SEND);
			removeIntent.putExtra(MoodleServerIntentService.URL, url);
			removeIntent.putExtra(MoodleServerIntentService.PASSCODE, currentPasscode+"");
			removeIntent.putExtra(MoodleServerIntentService.USERNAME, username);

			removeIntent.putExtra(MoodleServerIntentService.REGID, regId);
			startService(removeIntent);
		}
	}
	
	/**
	 * This method send the username, passcode and the GCm reg ID DIRECTLY to the server. It is used by
	 * The GCMIntentService.
	 * 
	 * @param regId
	 * @return
	 */
	public static boolean sendToServer(String regId) {
		
		try {
			Jsoup.connect(url+"/mod/ipal/tempview.php")
			.data("user", username)
			.data("p", currentPasscode+"")
			.data("r", regId)
			.get();
			Log.w("LoginActivity", "Send RegId to server for user: " + username);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}