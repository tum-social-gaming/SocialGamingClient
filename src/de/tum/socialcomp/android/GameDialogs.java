package de.tum.socialcomp.android;


import org.json.JSONException;
import org.json.JSONObject;

import de.tum.socialcomp.android.webservices.util.HttpPoster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

/**
 * This class is used for convenience to create
 * the various Dialogs that are shown within the app; 
 * these are mostly triggered by external events.
 * 
 * @author Niklas Klügel
 *
 */

public class GameDialogs {
	
	/**
	 * This Dilog is shown once a request to play a game is received.
	 * Depending on the user actions it either sends a message to accept or
	 * abort the game.
	 * 
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createGameRequestDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog requestDialog = builder
		        .setMessage("Do you want to play?\n" + gameMessage.getString("user1Name") + " vs. " + gameMessage.getString("user2Name"))
		        .setNegativeButton("No", new DialogInterface.OnClickListener() {    

		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	try {
							String gameID = gameMessage.getString("gameID");
							
			            	new HttpPoster().execute(new String[]{"games", 
			            			gameID,
        							act.getFacebookID(act.getBaseContext()),
									"abort"});
			            	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		      

		            }
		        })

		        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {


		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	try {
							String gameID = gameMessage.getString("gameID");
							
			            	new HttpPoster().execute(new String[]{"games", 
			            			gameID,
        							act.getFacebookID(act.getBaseContext()),
									"accept"});
			            	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

		            }
		        })
		        .create();
		
		return requestDialog;
	}

	/**
	 * Shows an informational Dialog that the game was aborted due to users request.
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createUserAbortDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("Game aborted: \n" + gameMessage.getString("aborterName") + " gave up!")
		        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .create();
		
		return abortDialog;
	}

	/**
	 * Dialog shown once the game is established; this is used to indicate that the game
	 * waits for short term social interaction.
	 * 
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createGameStartDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("Game established, go shake the phone!")
		        .create();
		
	
		return abortDialog;
	}

	/**
	 * Informational dialog that the user has won.
	 * 
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createUserWonDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("You won against " + gameMessage.getString("opponent") + "! Your score is now: "+ gameMessage.getDouble("score"))
		        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .create();
		
		return abortDialog;
	}
	
	/**
	 * Informational dialog that the user has lost.
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createUserLostDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("You lost against " + gameMessage.getString("opponent") + "! Your score is now: "+ gameMessage.getDouble("score"))
		        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .create();
		
		return abortDialog;
	}
	
	/**
	 * Informational Dialog that the the game was a draw.
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createUserDrawDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("Draw!")
		        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .create();
		
		return abortDialog;
	}
	
	/**
	 * Dialog that allows to send a "poke" message to another user selected from the map fragment.
	 * @param act
	 * @param userFacebookID
	 * @param recipentName
	 * @param recipentFacebookID
	 * @return
	 */
	public static Dialog createUserPokeDialog(Activity act, final String userFacebookID, String recipentName, final String recipentFacebookID){
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog pokeDialog = builder
		        .setMessage("Do you want to poke " + recipentName +"?")
		        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .setPositiveButton("Poke", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {

		            	new HttpPoster().execute(new String[]{"games", 
		            			userFacebookID,
    							recipentFacebookID,
								"poke"});
		            }
		        
		        })
		        .create();
		
		return pokeDialog;
	}
	
	/**
	 * Informational dialog that indicates that the user was poked by another user.
	 * @param gameMessage
	 * @param act
	 * @return
	 * @throws JSONException
	 */
	public static Dialog createUserWasPokedDialog(final JSONObject gameMessage, final MainActivity act) throws JSONException {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
		        .setMessage("You were poked by "+gameMessage.getString("senderName"))
		        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface arg0, int arg1) {
		            	// do nothing for now
		            }
		        
		        })
		        .create();
		
		return abortDialog;
	}
}
