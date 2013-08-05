/*
 * PhoneNavigator Plugin for Phonegap
 *
 * Copyright (c) 2013 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2013 Working Edge Ltd. (http://www.workingedge.co.uk)
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
package org.apache.cordova.plugin;

import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;

public class PhoneNavigator extends CordovaPlugin {

	private static final String LOG_TAG = "PhoneNavigator";

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		boolean result;
		Log.d(LOG_TAG, "Executing plugin");
		if ("doNavigate".equals(action)){
			result = this.doNavigate(args);
		} else {
			Log.d(LOG_TAG, "Invalid action");
			result = false;
		}
		if(result == true){
			callbackContext.success();
		}
		return result;
	}

	private boolean doNavigate(JSONArray args){
		boolean result;

		try {
			String lat = args.getString(0);
        	String lon = args.getString(1);

        	if (lat != null && lat.length() > 0 && lon != null && lon.length() > 0) {
        		Log.d(LOG_TAG, "Navigating to lat="+lat+", lon="+lon);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + lat +","+ lon));
                this.cordova.getActivity().startActivity(i);
                result = true;
            } else {
            	Log.d(LOG_TAG, "Expected two non-empty string arguments for lat and lon." );
            	result = false;
            }
		}catch( JSONException e ) {
			Log.d(LOG_TAG, "Exception occurred: ".concat(e.getMessage()));
        	result = false;
		}
        return result;
    }

}