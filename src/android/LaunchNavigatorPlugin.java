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

import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.workingedge.LaunchNavigator;


public class LaunchNavigatorPlugin extends CordovaPlugin {

    private static final String LOG_TAG = "LaunchNavigatorPlugin";
    private static final String MANIFEST_API_KEY = "launchnavigator.GOOGLE_API_KEY";

    private LaunchNavigator launchNavigator;
    private CordovaLogger logger;

    @Override
    protected void pluginInitialize() {
        try {
            logger = new CordovaLogger(cordova, webView, LOG_TAG);
            launchNavigator = new LaunchNavigator(cordova.getActivity().getApplicationContext(), new CordovaLogger(cordova, webView, LaunchNavigator.LOG_TAG));
            String googleApiKey = cordova.getActivity().getPackageManager().getApplicationInfo(cordova.getActivity().getPackageName(), PackageManager.GET_META_DATA).metaData.getString(MANIFEST_API_KEY);
            if(googleApiKey != null){
                launchNavigator.setGoogleApiKey(googleApiKey);
            }
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        try {
            logger.debug("Plugin action="+action);

            if ("enableDebug".equals(action)) {
                boolean debugEnabled = args.getBoolean(0);
                setDebug(debugEnabled);
                callbackContext.success();
            } else if ("setApiKey".equals(action)) {
                String apiKey = args.getString(0);
                setApiKey(apiKey);
                callbackContext.success();
            } else if ("navigate".equals(action)) {
                /**
                 * args[]
                 * args[0] - app
                 * args[1] - dType
                 * args[2] - dest
                 * args[3] - destNickname
                 * args[4] - sType
                 * args[5] - start
                 * args[6] - startNickname
                 * args[7] - transportMode
                 * args[8] - launchMode
                 * args[9] - extras
                 * args[10] - enableGeolocation
                 */

                if(args.get(10) != null){
                    launchNavigator.setGeocoding(args.getBoolean(10));
                }

                JSONObject params = new JSONObject();
                params.put("app", args.getString(0));
                params.put("dType", args.getString(1));
                params.put("dest", args.getString(2));
                params.put("destNickname", args.getString(3));
                params.put("sType", args.getString(4));
                params.put("start", args.getString(5));
                params.put("startNickname", args.getString(6));
                params.put("transportMode", args.getString(7));
                params.put("launchMode", args.getString(8));
                params.put("extras", args.getString(9));

                String error = launchNavigator.navigate(params);
                if(error == null){
                    callbackContext.success();
                }else{
                    handleError(error, callbackContext);
                }
            } else if ("discoverSupportedApps".equals(action)) {
                // This is called by plugin JS on initialisation
                JSONObject apps = launchNavigator.getGeoApps();
                callbackContext.success(apps);
            } else if ("availableApps".equals(action)) {
                JSONObject apps = launchNavigator.getAvailableApps();
                callbackContext.success(apps);
            } else if ("isAppAvailable".equals(action)) {
                boolean available = launchNavigator.isAppAvailable(args.getString(0));
                callbackContext.success(available ? 1 : 0);
            }else {
                String msg = "Invalid action";
                handleError(msg, callbackContext);
                return false;
            }
        }catch(Exception e){
            handleException(e.getMessage(), callbackContext);
        }
        return true;
    }


    /*
     * Utilities
     */

    private void setDebug(boolean enabled){
        this.logger.setEnabled(enabled);
        this.launchNavigator.getLogger().setEnabled(enabled);
    }

    private void setApiKey(String apiKey){
        this.launchNavigator.setGoogleApiKey(apiKey);
    }

    private void handleError(String msg, CallbackContext callbackContext){
        logger.error(msg);
        callbackContext.error(msg);
    }

    private void handleException(String msg, CallbackContext callbackContext){
        msg = "Exception occurred: ".concat(msg);
        handleError(msg, callbackContext);
    }

}
