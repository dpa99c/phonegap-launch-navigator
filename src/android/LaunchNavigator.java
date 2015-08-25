/*
 * LaunchNavigator Plugin for Phonegap
 *
 * Copyright (c) 2014 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2014 Working Edge Ltd. (http://www.workingedge.co.uk)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package uk.co.workingedge.phonegap.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


public class LaunchNavigator extends CordovaPlugin {

	private static final String LOG_TAG = "LaunchNavigator";
	private static final String NO_GOOGLE_NAVIGATOR = "No Activity found to handle Intent";
	private static final String MAPS_PROTOCOL = "http://maps.google.com/maps?";
	private static final String TURN_BY_TURN_PROTOCOL = "google.navigation:";

	@Override
	public boolean execute(String action, JSONArray args,
		CallbackContext callbackContext) throws JSONException {
		boolean result;

		if ("navigate".equals(action)){
			result = this.navigate(args, callbackContext);
		}else {
			String msg = "Invalid action";
			Log.e(LOG_TAG, msg);
			callbackContext.error(msg);
			result = false;
		}
		
		if(result == true){
			callbackContext.success();
		}
		return result;
	}

	private boolean navigate(JSONArray args, CallbackContext callbackContext){
		boolean result;
		try {
			String destination;
			String start = null;

			String dType = args.getString(0);
			if(dType.equals("pos")){
				JSONArray pos = args.getJSONArray(1);

				String dLat = pos.getString(0);
	        	String dLon = pos.getString(1);
				if (dLat == null || dLat.length() == 0 || dLon == null || dLon.length() == 0) {
					String msg = "Expected two non-empty string arguments for destination lat/lon.";
                	Log.e(LOG_TAG, msg);
                	callbackContext.error(msg);
					return false;
	            }
				destination = dLat + "," + dLon;
			}else{
				String dName = args.getString(1);
				if (dName == null || dName.length() == 0) {
					String msg = "Expected non-empty string argument for destination name.";
					Log.e(LOG_TAG, msg);
					callbackContext.error(msg);
		        	return false;
		        }
				destination = dName;
			}

			String sType = args.getString(2);
			if(sType.equals("pos")){
				JSONArray pos = args.getJSONArray(3);

				String sLat = pos.getString(0);
	        	String sLon = pos.getString(1);
				if (sLat == null || sLat.length() == 0 || sLon == null || sLon.length() == 0) {
					String msg = "Expected two non-empty string arguments for start lat/lon.";
					Log.e(LOG_TAG, msg);
					callbackContext.error(msg);
					return false;
	            }
				start = sLat + "," + sLon;
			}else if(sType.equals("name")){
				String sName = args.getString(3);
				if (sName == null || sName.length() == 0) {
					String msg = "Expected non-empty string argument for start name.";
					Log.e(LOG_TAG, msg);
					callbackContext.error(msg);
		        	return false;
		        }
				start = sName;
			}
			String transportMode = args.getString(4);
			boolean turnByTurnMode = args.getBoolean(5);

			String logMsg = "Navigating to "+destination;
			String url;

			if(turnByTurnMode){
				logMsg += " in turn-by-turn mode";
				url = TURN_BY_TURN_PROTOCOL + "q=" + destination;
				if(transportMode != null){
					logMsg += " by transportMode=" + transportMode;
					url += "&mode=" + transportMode;
				}
			}else{
				logMsg += " in maps mode";
				url = MAPS_PROTOCOL + "daddr=" + destination;
				if(start != null){
					logMsg += " from " + start;
					url += "&saddr=" + start;
				}else{
					logMsg += " from current location";
				}
			}
			Log.d(LOG_TAG, logMsg);

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			if(!turnByTurnMode){
				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			}
			this.cordova.getActivity().startActivity(intent);
			result = true;
		}catch( JSONException e ) {
			String msg = e.getMessage();
			if(msg.contains(NO_GOOGLE_NAVIGATOR)){
				msg = "Google Maps app is not installed on this device";
			}
			Log.e(LOG_TAG, "Exception occurred: ".concat(msg));
			callbackContext.error(msg);
        	result = false;
		}
        return result;
    }
}