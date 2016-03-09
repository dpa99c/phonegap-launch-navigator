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

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LaunchNavigator extends CordovaPlugin {

	private static final String LOG_TAG = "LaunchNavigator";
	private static final String NO_GOOGLE_MAPS = "No Activity found to handle Intent";
	private static final String MAPS_PROTOCOL = "http://maps.google.com/maps?";
	private static final String TURN_BY_TURN_PROTOCOL = "google.navigation:";

    private static final String GOOGLE_MAPS = "google_maps";
    private static final String GOOGLE_MAPS_PACKAGE_ID = "com.google.android.apps.maps";
    private static final String UNSPECIFIED = "none"; // App is unspecified so use native chooser

	private static final String GEO_URI = "geo:";

	PackageManager packageManager;
	Context context;

    // Map of app name to package name
	Map<String, String> appPackages;


	@Override
	protected void pluginInitialize() {
		Log.i(LOG_TAG, "pluginInitialize()");
        discoverAvailableApps();
	}

	@Override
	public boolean execute(String action, JSONArray args,
		CallbackContext callbackContext) throws JSONException {
		boolean result = true;
    try {
        if ("navigate".equals(action)) {
            result = this.navigate(args, callbackContext);
        } else if ("discoverSupportedApps".equals(action)) {
            this.discoverSupportedApps(args, callbackContext);
        } else if ("availableApps".equals(action)) {
            this.availableApps(args, callbackContext);
        } else if ("isAppAvailable".equals(action)) {
            this.isAppAvailable(args, callbackContext);
        }else {
            String msg = "Invalid action";
            Log.e(LOG_TAG, msg);
            callbackContext.error(msg);

        }
    }catch(Exception e){
        String msg = "Exception occurred: "+e.getMessage();
        Log.e(LOG_TAG, msg);
        callbackContext.error(msg);
        result = false;
    }
		return result;
	}

    /*
     * Plugin API
     */

    private void discoverSupportedApps(JSONArray args, CallbackContext callbackContext) throws JSONException{
        JSONObject apps = new JSONObject();
        for (Map.Entry<String, String> entry : appPackages.entrySet()) {
            String appName = entry.getKey();
            String packageName = entry.getValue();
            apps.put(appName, packageName);
        }
        callbackContext.success(apps);
    }

	private void availableApps(JSONArray args, CallbackContext callbackContext){
        JSONArray apps = new JSONArray();
        for (String appName : appPackages.values()) {
            apps.put(appName);
        }
		callbackContext.success(apps);
	}

    private void isAppAvailable(JSONArray args, CallbackContext callbackContext) throws Exception{
        boolean isAvailable = appPackages.containsValue(args.getString(0));
        if(isAvailable){
            callbackContext.success(1);
        }else{
            callbackContext.success(0);
        }
    }

    private boolean navigate(JSONArray args, CallbackContext callbackContext) throws Exception{
        boolean result;
        String appName = args.getString(0);
        String launchMode = args.getString(8);

        if(appName.equals(GOOGLE_MAPS) && !launchMode.equals("geo")){
            result = launchGoogleMaps(args, callbackContext);
        }else{
            result = launchApp(args, callbackContext);
        }
        return result;
    }


    /*
     * Internal functions
     */
    private boolean launchApp(JSONArray args, CallbackContext callbackContext) throws Exception{
        String appName = args.getString(0);
        String dType = args.getString(1);
        String dNickName = args.getString(3);

        String destLatLon;
        if(dType.equals("name")){
            destLatLon = geocodeAddressToLatLon(args.getString(2));
        }else{
            destLatLon = getLocationFromPos(args, 2);
        }

        String uri = GEO_URI+destLatLon+"?q="+destLatLon;
        if(!isNull(dNickName)){
            uri += "("+dNickName+")";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(!appName.equals(UNSPECIFIED)){
            if(appName.equals(GOOGLE_MAPS)) {
                appName = GOOGLE_MAPS_PACKAGE_ID;
            }
            intent.setPackage(appName);
        }
        this.cordova.getActivity().startActivity(intent);
        callbackContext.success();
        return true;
    }

    private boolean launchGoogleMaps(JSONArray args, CallbackContext callbackContext) throws Exception{
        boolean result;
        try {
            String destination;
            String start = null;

            String dType = args.getString(1);
            if(dType.equals("pos")){
                destination = getLocationFromPos(args, 2);
            }else{
                destination = getLocationFromName(args, 2);
            }

            String sType = args.getString(4);
            if(sType.equals("pos")){
                start = getLocationFromPos(args, 5);
            }else if(sType.equals("name")){
                start = getLocationFromName(args, 5);
            }
            String transportMode = args.getString(7);
            String launchMode = args.getString(8);

            String logMsg = "Navigating to "+destination;
            String url;

            if(launchMode.equals("turn-by-turn")){
                url = TURN_BY_TURN_PROTOCOL + "q=" + destination;
                if(!isNull(transportMode)){
                    logMsg += " by transportMode=" + transportMode;
                    url += "&mode=" + transportMode;
                }
                logMsg += " in turn-by-turn mode";
            }else{
                url = MAPS_PROTOCOL + "daddr=" + destination;
                if(!isNull(start)){
                    logMsg += " from " + start;
                    url += "&saddr=" + start;
                }else{
                    logMsg += " from current location";
                }
                logMsg += " in maps mode";
            }
            Log.d(LOG_TAG, logMsg);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
            result = true;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_GOOGLE_MAPS)){
                msg = "Google Maps app is not installed on this device";
            }
            Log.e(LOG_TAG, "Exception occurred: ".concat(msg));
            callbackContext.error(msg);
            result = false;
        }
        return result;
    }

    private String getLocationFromPos(JSONArray args, int index) throws Exception{
        String location;
        JSONArray pos = args.getJSONArray(index);
        String lat = pos.getString(0);
        String lon = pos.getString(1);
        if (isNull(lat) || lat.length() == 0 || isNull(lon) || lon.length() == 0) {
            throw new Exception("Expected two non-empty string arguments for lat/lon.");
        }
        location = lat + "," + lon;
        return location;
    }

    private String getLocationFromName(JSONArray args, int index) throws Exception{
        String name = args.getString(index);
        if (isNull(name) || name.length() == 0) {
            throw new Exception("Expected non-empty string argument for place name.");
        }
        return name;
    }

    private String getAppName(String packageName){
        ApplicationInfo ai;
        try {
            ai = packageManager.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? packageManager.getApplicationLabel(ai) : null);
        return applicationName;
    }

    private void discoverAvailableApps(){
        context = this.cordova.getActivity().getApplicationContext();
        packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GEO_URI));
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        appPackages = new HashMap<String, String>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = getAppName(packageName);
            Log.i(LOG_TAG, "Available app: name="+appName +"; package=" + packageName);
            appPackages.put(appName, packageName);
        }
    }

    private String geocodeAddressToLatLon(String address) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        address = address.replaceAll(" ","%20");

        HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;

        response = client.execute(httppost);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        int b;
        while ((b = stream.read()) != -1) {
            stringBuilder.append((char) b);
        }
        JSONObject  oResponse = new JSONObject(stringBuilder.toString());

        double longitude = ((JSONArray)oResponse.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

        double latitude = ((JSONArray)oResponse.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");

        String result = latitude+","+longitude;
        return result;
    }

    private boolean isNull(String arg){
        return arg == null || arg.equals("null");
    }

}