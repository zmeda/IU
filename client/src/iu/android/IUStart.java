package iu.android;

import iu.android.explore.ExploreMode;
import iu.android.gui.PracticeMode;
import iu.android.network.NetworkResources;
import iu.android.network.explore.ExploreClient;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

/**
 * The activity entry point.-
 * 
 * @author luka
 * 
 */
public class IUStart extends Activity
{
	
	ExploreClient						client;
	boolean								enteredBattleMode			= false;

	private Button						btnLogIn;
	private EditText					txtUsername;
	private EditText					txtPassword;
	private EditText					txtPassword2;
	private Spinner					serverChooser;
	private CheckBox					checkBoxTutorial;
	private Button						btnClose;
	private Button						btnPractice;

	private static final String[]	serverIPs					= {"10.106.3.2", "192.168.1.3"};

	private static final String[]	serverNames					= {"Forest", "Hills"};

	public static int					DefaultServerSelection	= 0;
	public static String				DefaultUsername			= "google1";
	public static String				DefaultPassword			= "google1";

	public static String				ServerIP						= IUStart.serverIPs[IUStart.DefaultServerSelection];


	@Override
	protected void onCreate (final Bundle icicle)
	{
		super.onCreate (icicle);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		this.getWindow ( ).setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Show the map view
		this.setContentView (iu.android.R.layout.iu_start);

		this.init ( );
	}


	private void init ( )
	{
		ImageView backgound = new ImageView (this.getBaseContext ( ));
		backgound.setBackgroundResource (iu.android.R.drawable.start_image);

		// User name text field
		this.txtUsername = (EditText) this.findViewById (iu.android.R.id.txt_username);
		this.txtUsername.setText (IUStart.DefaultUsername);

		// Password text field
		this.txtPassword = (EditText) this.findViewById (iu.android.R.id.txt_password);
		this.txtPassword.setText (IUStart.DefaultPassword);

		this.checkBoxTutorial = (CheckBox) this.findViewById (iu.android.R.id.box_tutorial);
		this.checkBoxTutorial.setVisibility (View.VISIBLE);

		// Server names
		this.serverChooser = (Spinner) this.findViewById (iu.android.R.id.server_name);

		ArrayAdapter<String> adapterColor = new ArrayAdapter<String> (this,
				android.R.layout.simple_spinner_item, IUStart.serverNames);
		adapterColor.setDropDownViewResource (android.R.layout.simple_spinner_dropdown_item);
		this.serverChooser.setAdapter (adapterColor);
		this.serverChooser.setSelection (IUStart.DefaultServerSelection);

		// Log in button
		this.btnLogIn = (Button) this.findViewById (iu.android.R.id.btn_login);

		this.btnLogIn.setOnClickListener (new OnClickListener ( ) {
			public void onClick (final View arg0)
			{

				IUStart.this.login ( );

			}

		});

		this.btnClose = (Button) this.findViewById (iu.android.R.id.btn_close);

		this.btnClose.setOnClickListener (new OnClickListener ( ) {
			public void onClick (final View arg0)
			{

				IUStart.this.finish ( );
			}

		});

		this.btnPractice = (Button) this.findViewById (iu.android.R.id.btn_practice);

		this.btnPractice.setOnClickListener (new OnClickListener ( ) {
			public void onClick (final View arg0)
			{
				IUStart.this.practice ( );
			}

		});

		// Sign in text - HIDDEN
		// TextView signInLabel = (TextView) this.findViewById (R.id.label_signin);
		// signInLabel.setVisibility (View.INVISIBLE);

		// Sign in button - HIDDEN
		// this.btnSignIn = (Button) this.findViewById (R.id.btn_signin);
		// this.btnSignIn.setVisibility (View.INVISIBLE);

		// this.btnSignIn.setOnClickListener (new OnClickListener ( ) {
		// public void onClick (final View arg0)
		// {
		//
		// IUStart.this.signIn ( );
		//
		// }
		// });
	}


	/**
	 * Utility function for showing a message with an OK button
	 * 
	 * @param message
	 */
	private void showLoginErrorMessage (final String message)
	{
		AlertDialog.Builder errorDialog = new AlertDialog.Builder (IUStart.this);
		
		errorDialog.setIcon (0);
		errorDialog.setTitle ("Error");
		errorDialog.setPositiveButton ("ok", null);
		errorDialog.setMessage ("Login error");
		errorDialog.show ( );
	}


	private boolean setupServerConnection (final String serverName)
	{
		boolean res;

		if (serverName == null)
		{
			// this.showAlert ("Sign in error.", "You must enter the server name.", "ok", null, false, null);
			this.showLoginErrorMessage ("You must choose the server name.");
			return false;
		}

		//
		// Connect to server
		//
		try
		{
			this.client = new ExploreClient ( );

			if (this.client.joinToServer (serverName))
			{
				// this.client.getPortToSend ( );
				// this.client.informServerOfOurListenPort ( );
				// this.client.getTCPPort ( );
				// // this.client.startTCPThread();

				// this.client.measureLatency ( ); // DJ

				res = true;
			}
			else
			{
				// String strErrorText = "Error connecting to '" + serverName + "'";
				// this.showAlert ("Connection error", strErrorText, "ok", null, false, null);

				// If not joined to server
				this.showLoginErrorMessage ("Error connecting to '" + serverName + "'");

				res = false;

				throw new IOException ( );
			}
		}
		catch (IOException ioe)
		{
			res = false;
		}

		return res;
	}


	/**
	 * If log in or sign in unsuccessfull
	 */
	private void closeServerConnection ( )
	{
		if (!this.enteredBattleMode)
		{
			this.client.logout ( );
		}
	}


	/*
	 * @Override protected void onActivityResult (final int requestCode, final int resultCode, final String
	 * data, final Bundle extras) { super.onActivityResult (requestCode, resultCode, data, extras); }
	 */

	protected void login ( )
	{
		// do login

		String username = this.txtUsername.getText ( ).toString ( );
		if (username == null || username.length ( ) < 3)
		{
			this.showLoginErrorMessage ("Username must be at least 3 characters long.");
			return;
		}

		String password = this.txtPassword.getText ( ).toString ( );
		if (password == null || password.length ( ) < 6)
		{
			this.showLoginErrorMessage ("Password must be at least 6 characters long.");

			return;
		}

		IUStart.ServerIP = IUStart.serverIPs[this.serverChooser.getSelectedItemPosition ( )];

		// Show some info to the user
		// DialogInterface connectingDialog = this.showAlert ("IU", "Connecting ...", null, false);
		DialogInterface connectingDialog = ProgressDialog.show (this, "IU", "Connecting ...");

		// Setup the connection to the server and login
		if (this.setupServerConnection (IUStart.ServerIP))
		{
			if (this.client.sendLoginData (username, password))
			{
				this.startExploreMode (username);
			}
			else
			{
				// Close connection to server
				this.closeServerConnection ( );

				// Close the 'connecting' message view
				connectingDialog.dismiss ( );

				this.showLoginErrorMessage ("Username and password are invalid.");
			}

		}

		connectingDialog.dismiss ( );

		// Socket socket = null;
		// try
		// {
		//
		// socket = ConnectionProvider.login(username, password);
		//
		// }
		// catch (UnknownHostException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return;
		// }
		// catch (IOException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return;
		// }
		//
		// if (socket == null)
		// {
		// // authentication to server failed
		// // username or password is incorrect
		// Debug.Joshua.print("Socket is null");
		// // return;
		// }

		this.startCommunicationService ( );
	}


	protected void signIn ( )
	{
		AlertDialog errorDialog = new AlertDialog.Builder (this.getBaseContext ( )).setIcon (0).setTitle (
				"Error").setPositiveButton ("ok", null).create ( );

		// do login
		String username = this.txtUsername.getText ( ).toString ( );
		if (username == null || username.length ( ) < 3)
		{
			errorDialog.setMessage ("Username must be at least 3 characters long.");
			errorDialog.show ( );
			return;
		}

		String password = this.txtPassword.getText ( ).toString ( );
		if (password == null || password.length ( ) < 6)
		{
			errorDialog.setMessage ("Password must be at least 6 characters long.");
			errorDialog.show ( );
			return;
		}

		String password2 = this.txtPassword2.getText ( ).toString ( );
		if (password2 == null || !password.equals (password2))
		{
			errorDialog.setMessage ("Password and reentered password must be equal.");
			errorDialog.show ( );
			return;
		}

		IUStart.ServerIP = IUStart.serverIPs[this.serverChooser.getSelectedItemPosition ( )];

		// Setup the connection to the server and signin
		if (this.setupServerConnection (IUStart.ServerIP))
		{
			// Show some info to the user
			ProgressDialog.show (this, "IU", "Connecting ...");

			if (this.client.sendSignInData (username, password))
			{

				this.startExploreMode (username);
			}
			else
			{
				// Close connection to server
				this.closeServerConnection ( );

				// Show an error message
				errorDialog.setMessage ("This username already exists - select a different one.");
				errorDialog.show ( );
			}
		}

		// Socket socket = null;
		// try
		// {
		//
		// socket = ConnectionProvider.login(username, password);
		//
		// }
		// catch (UnknownHostException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return;
		// }
		// catch (IOException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return;
		// }
		//
		// if (socket == null)
		// {
		// // authentication to server failed
		// // username or password is incorrect
		// Debug.Joshua.print("Socket is null");
		// // return;
		// }

		this.startCommunicationService ( );
	}


	/**
	 * Starts the service that sends location updates to the server
	 */
	private void startCommunicationService ( )
	{
	}


	private void startExploreMode (final String username)
	{
		//
		// Close the original connection to the server
		//

		// TODO - Start sender and receiver threads (un-comment this)
		// this.client.start();
		// this.client.closeInitializationStream();

		// Add the client to the container so we can get him from the sub-activities
		NetworkResources.setClient (this.client);

		//
		// Start sub-activity explore mode
		//
		Intent intent = new Intent (this, ExploreMode.class);

		intent.putExtra ("username", username);
		intent.putExtra ("tutorialMode", this.checkBoxTutorial.isChecked ( ));

		// this.startSubActivity (intent, 0);
		this.startActivity (intent);
		this.finish ( );

		this.enteredBattleMode = true;
	}


	protected void practice ( )
	{
		Intent intent = new Intent (this, PracticeMode.class);

		// this.startSubActivity (intent, PracticeMode.PRACTICE_MODE_REQUEST_CODE);
		this.startActivity (intent);
	}
}
