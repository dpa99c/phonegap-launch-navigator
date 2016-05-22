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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LaunchNavigator extends CordovaPlugin {

    private static final String LOG_TAG = "LaunchNavigator";
    private static final String NO_APP_FOUND = "No Activity found to handle Intent";
    private static final String MAPS_PROTOCOL = "http://maps.google.com/maps?";
    private static final String TURN_BY_TURN_PROTOCOL = "google.navigation:";

    // Explicitly supported apps
    private static final String GEO = "geo"; // Use native app choose for geo: intent
    private static final String GOOGLE_MAPS = "google_maps";
    private static final String CITYMAPPER = "citymapper";
    private static final String UBER = "uber";
    private static final String WAZE = "waze";
    private static final String YANDEX = "yandex";
    private static final String SYGIC = "sygic";
    private static final String HERE_MAPS = "here_maps";
    private static final String MOOVIT = "moovit";


    private static final Map<String, String> supportedAppPackages;
    static {
        Map<String, String> _supportedAppPackages = new HashMap<String, String>();
        _supportedAppPackages.put(GOOGLE_MAPS, "com.google.android.apps.maps");
        _supportedAppPackages.put(CITYMAPPER, "com.citymapper.app.release");
        _supportedAppPackages.put(UBER, "com.ubercab");
        _supportedAppPackages.put(WAZE, "com.waze");
        _supportedAppPackages.put(YANDEX, "ru.yandex.yandexnavi");
        _supportedAppPackages.put(SYGIC, "com.sygic.aura");
        _supportedAppPackages.put(HERE_MAPS, "com.here.app.maps");
        _supportedAppPackages.put(MOOVIT, "com.tranzmate");
        supportedAppPackages = Collections.unmodifiableMap(_supportedAppPackages);
    }

    private static final Map<String, String> supportedAppNames;
    static {
        Map<String, String> _supportedAppNames = new HashMap<String, String>();
        _supportedAppNames.put(GOOGLE_MAPS, "Google Maps");
        _supportedAppNames.put(CITYMAPPER, "Citymapper");
        _supportedAppNames.put(UBER, "Uber");
        _supportedAppNames.put(WAZE, "Waze");
        _supportedAppNames.put(YANDEX, "Yandex Navigator");
        _supportedAppNames.put(SYGIC, "Sygic");
        _supportedAppNames.put(HERE_MAPS, "HERE Maps");
        _supportedAppNames.put(MOOVIT, "Moovit");
        supportedAppNames = Collections.unmodifiableMap(_supportedAppNames);
    }

    private static final String GEO_URI = "geo:";

    PackageManager packageManager;
    Context context;

    OkHttpClient httpClient = new OkHttpClient();

    boolean enableDebug = false;

    // Map of app name to package name
    Map<String, String> availableApps;


    @Override
    protected void pluginInitialize() {
        Log.i(LOG_TAG, "pluginInitialize()");
        discoverAvailableApps();
    }

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        try {
            logDebug("Plugin action="+action);
            if ("navigate".equals(action)) {
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
                 * args[9] - enableDebug
                 * args[10] - extras
                 */
                enableDebug = args.getBoolean(9);
                if(enableDebug){
                    String navigateArgs = "Called navigate() with args"
                            + ": app="+ args.getString(0)
                            + "; dType="+ args.getString(1)
                            + "; dest="+ args.getString(2)
                            + "; destNickname="+ args.getString(3)
                            + "; sType="+ args.getString(4)
                            + "; start="+ args.getString(5)
                            + "; startNickname="+ args.getString(6)
                            + "; transportMode="+ args.getString(7)
                            + "; launchMode="+ args.getString(8)
                            + "; extras="+ args.getString(10);
                    logDebug(navigateArgs);
                }
                this.navigate(args, callbackContext);
            } else if ("discoverSupportedApps".equals(action)) {
                // This is called by plugin JS on initialisation
                this.discoverSupportedApps(args, callbackContext);
            } else if ("availableApps".equals(action)) {
                this.availableApps(args, callbackContext);
            } else if ("isAppAvailable".equals(action)) {
                this.isAppAvailable(args, callbackContext);
            }else {
                String msg = "Invalid action";
                logError(msg);
                callbackContext.error(msg);
                return false;
            }
        }catch(Exception e){
            String msg = "Exception occurred: "+e.getMessage();
            logError(msg);
            callbackContext.error(msg);
        }
        return true;
    }

    /*
     * Plugin API
     */
    private void discoverSupportedApps(JSONArray args, CallbackContext callbackContext) throws JSONException{
        JSONObject apps = new JSONObject();

        // Dynamically populate from discovered available apps that support geo: protocol
        for (Map.Entry<String, String> entry : availableApps.entrySet()) {
            String appName = entry.getKey();
            String packageName = entry.getValue();
            // If it's not already an explicitly supported app
            if(!supportedAppPackages.containsValue(packageName)){
                apps.put(appName, packageName);
            }
        }

        callbackContext.success(apps);
    }

    private void availableApps(JSONArray args, CallbackContext callbackContext) throws Exception{
        JSONObject apps = new JSONObject();

        // Add explicitly supported apps first
        for (Map.Entry<String, String> entry : supportedAppPackages.entrySet()) {
            String _appName = entry.getKey();
            String _packageName = entry.getValue();
            apps.put(_appName, availableApps.containsValue(_packageName));
        }

        // Iterate over available apps and add any dynamically discovered ones
        for (Map.Entry<String, String> entry : availableApps.entrySet()) {
            String _packageName = entry.getValue();
            // If it's not already present
            if(!apps.has(_packageName) && !supportedAppPackages.containsValue(_packageName)){
                apps.put(_packageName, true);
            }
        }
        callbackContext.success(apps);
    }

    private void isAppAvailable(JSONArray args, CallbackContext callbackContext) throws Exception{
        String appName = args.getString(0);
        if(supportedAppPackages.containsKey(appName)){
            appName = supportedAppPackages.get(appName);
        }

        if(availableApps.containsValue(appName)){
            callbackContext.success(1);
        }else{
            callbackContext.success(0);
        }
    }

    private void navigate(JSONArray args, CallbackContext callbackContext) throws Exception{
        String appName = args.getString(0);
        String launchMode = args.getString(8);

        if(appName.equals(GOOGLE_MAPS) && !launchMode.equals("geo")){
            launchGoogleMaps(args, callbackContext);
        }else if(appName.equals(CITYMAPPER)){
            launchCitymapper(args, callbackContext);
        }else if(appName.equals(UBER)){
            launchUber(args, callbackContext);
        }else if(appName.equals(WAZE)){
            launchWaze(args, callbackContext);
        }else if(appName.equals(YANDEX)){
            launchYandex(args, callbackContext);
        }else if(appName.equals(SYGIC)){
            launchSygic(args, callbackContext);
        }else if(appName.equals(HERE_MAPS)){
            launchHereMaps(args, callbackContext);
        }else if(appName.equals(MOOVIT)){
             launchMoovit(args, callbackContext);
         }else{
            launchApp(args, callbackContext);
        }
    }


    /*
     * Launch apps
     */
    private void launchApp(JSONArray args, CallbackContext callbackContext) throws Exception{
        String appName = args.getString(0);
        String dType = args.getString(1);
        String dNickName = args.getString(3);

        String logMsg = "Using " + getAppDisplayName(appName)+" to navigate to ";
        String destLatLon = null;
        String destName = null;
        String dest;

        if(dType.equals("name")){
            destName = getLocationFromName(args, 2);
            destLatLon = geocodeAddressToLatLon(args.getString(2));
            logMsg += destName;
            if(!isNull(destLatLon)){
                logMsg += "["+destLatLon+"]";
            }

        }else{
            destLatLon = getLocationFromPos(args, 2);
            logMsg += "["+destLatLon+"]";
        }

        if(!isNull(destLatLon)){
            dest = destLatLon;
        }else{
            dest = destName;
        }

        String uri = GEO_URI+destLatLon+"?q="+dest;
        if(!isNull(dNickName)){
            uri += "("+dNickName+")";
            logMsg += "("+dNickName+")";
        }

        String extras = parseExtrasToUrl(args);
        if(!isNull(extras)){
            uri += extras;
            logMsg += " - extras="+extras;
        }

        logDebug(logMsg);
        logDebug("URI: " + uri);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(!appName.equals(GEO)){
            if(appName.equals(GOOGLE_MAPS)) {
                appName = supportedAppPackages.get(GOOGLE_MAPS);
            }
            intent.setPackage(appName);
        }
        this.cordova.getActivity().startActivity(intent);
        callbackContext.success();
    }

    private void launchGoogleMaps(JSONArray args, CallbackContext callbackContext) throws Exception{
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

            String logMsg = "Using Google Maps to navigate to "+destination;
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

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setClassName(supportedAppPackages.get(GOOGLE_MAPS), "com.google.android.maps.MapsActivity");
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Google Maps app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchCitymapper(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
                try {
                    destAddress = reverseGeocodeLatLonToAddress(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains address for coords '"+destLatLon+"': "+e.getMessage());
                }
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
                try {
                    startLatLon = geocodeAddressToLatLon(args.getString(5));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+startAddress+"': "+e.getMessage());
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
                try {
                    startAddress = reverseGeocodeLatLonToAddress(args.getString(5));
                }catch(Exception e){
                    logError("Unable to obtains address for coords '"+startLatLon+"': "+e.getMessage());
                }
            }

            String url = "citymapper://directions?";
            String logMsg = "Using Citymapper to navigate to";
            if(!isNull(destAddress)){
                url += "&endaddress="+destAddress;
                logMsg += " '"+destAddress+"'";
            }
            if(!isNull(destLatLon)){
                url += "&endcoord="+destLatLon;
                logMsg += " ["+destLatLon+"]";
            }
            if(!isNull(destNickname)){
                url += "&endname="+destNickname;
                logMsg += " ("+destNickname+")";
            }

            if(!sType.equals("none")){
                logMsg += " from";
                if(!isNull(startAddress)){
                    url += "&startaddress="+startAddress;
                    logMsg += " '"+startAddress+"'";
                }
                if(!isNull(startLatLon)){
                    url += "&startcoord="+startLatLon;
                    logMsg += " ["+startLatLon+"]";
                }
                if(!isNull(startNickname)){
                    url += "&startname="+startNickname;
                    logMsg += " ("+startNickname+")";
                }
            }

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Citymapper app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchUber(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
                try {
                    destAddress = reverseGeocodeLatLonToAddress(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains address for coords '"+destLatLon+"': "+e.getMessage());
                }
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
                try {
                    startLatLon = geocodeAddressToLatLon(args.getString(5));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+startAddress+"': "+e.getMessage());
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
                try {
                    startAddress = reverseGeocodeLatLonToAddress(args.getString(5));
                }catch(Exception e){
                    logError("Unable to obtains address for coords '"+startLatLon+"': "+e.getMessage());
                }
            }

            String url = "uber://?action=setPickup";
            String logMsg = "Using Uber to navigate to";
            if(!isNull(destAddress)){
                url += "&dropoff[formatted_address]="+destAddress;
                logMsg += " '"+destAddress+"'";
            }
            if(!isNull(destLatLon)){
                String[] parts = splitLatLon(destLatLon);
                url += "&dropoff[latitude]="+parts[0]+"&dropoff[longitude]="+parts[1];
                logMsg += " ["+destLatLon+"]";
            }
            if(!isNull(destNickname)){
                url += "&dropoff[nickname]="+destNickname;
                logMsg += " ("+destNickname+")";
            }

            logMsg += " from";
            if(!sType.equals("none")){
                if(!isNull(startAddress)){
                    url += "&pickup[formatted_address]=="+startAddress;
                    logMsg += " '"+startAddress+"'";
                }
                if(!isNull(startLatLon)){
                    String[] parts = splitLatLon(startLatLon);
                    url += "&pickup[latitude]="+parts[0]+"&pickup[longitude]="+parts[1];
                    logMsg += " ["+startLatLon+"]";
                }
                if(!isNull(startNickname)){
                    url += "&pickup[nickname]="+startNickname;
                    logMsg += " ("+startNickname+")";
                }
            }else{
                url += "&pickup=my_location";
                logMsg += " current location";
            }

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Uber app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchWaze(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;

            String dType = args.getString(1);

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
                try {
                    destAddress = reverseGeocodeLatLonToAddress(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains address for coords '"+destLatLon+"': "+e.getMessage());
                }
            }

            String url = "waze://?";
            String logMsg = "Using Waze to navigate to";
            if(!isNull(destLatLon)){
                url += "ll="+destLatLon+"&navigate=yes";
                logMsg += " ["+destLatLon+"]";
            }else{
                url += "q="+destAddress;
                logMsg += " '"+destAddress+"'";
            }

            logMsg += " from current location";

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Waze app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchYandex(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;

            String dType = args.getString(1);
            String sType = args.getString(4);

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
                try {
                    startLatLon = geocodeAddressToLatLon(args.getString(5));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+startAddress+"': "+e.getMessage());
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
            }

            Intent intent = new Intent(supportedAppPackages.get(YANDEX)+".action.BUILD_ROUTE_ON_MAP");
            intent.setPackage(supportedAppPackages.get(YANDEX));
            String logMsg = "Using Yandex to navigate to";

            String[] parts = splitLatLon(destLatLon);
            intent.putExtra("lat_to", parts[0]);
            intent.putExtra("lon_to", parts[1]);
            logMsg += " ["+destLatLon+"]";

            if(!isNull(destAddress)){
                logMsg += " ('"+destAddress+"')";
            }


            logMsg += " from";
            if(!sType.equals("none")){
                parts = splitLatLon(startLatLon);
                intent.putExtra("lat_from", parts[0]);
                intent.putExtra("lon_from", parts[1]);
                logMsg += " ["+startLatLon+"]";
                if(!isNull(startAddress)){
                    logMsg += " ('"+startAddress+"')";
                }
            }else{
                logMsg += " current location";
            }

            String jsonStringExtras = args.getString(10);
            JSONObject oExtras = null;
            if(!isNull(jsonStringExtras)){
                oExtras =  new JSONObject(jsonStringExtras);
            }

            if(oExtras != null){
                Iterator<?> keys = oExtras.keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    String value = oExtras.getString(key);
                    intent.putExtra(key, value);
                }
            }
            logDebug(logMsg);
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Yandex app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchSygic(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;

            String dType = args.getString(1);
            String transportMode = args.getString(7);
            String url = supportedAppPackages.get(SYGIC)+"://coordinate|";
            String logMsg = "Using Sygic to navigate to";

            if(transportMode.equals("w")){
                transportMode = "walk";
            }else{
                transportMode = "drive";
            }

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtain coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
                logMsg += " ["+destLatLon+"]";
            }

            String[] pos = splitLatLon(destLatLon);
            url += pos[1]+"|"+pos[0]+"|"+transportMode;

            logMsg += " by " + transportMode;

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Sygic app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchHereMaps(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);

            String url = "https://share.here.com/r/";
            String logMsg = "Using HERE Maps to navigate";

            logMsg += " from";
            if(sType.equals("none")){
                url += "mylocation";
                logMsg += " Current Location";

            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(args, 5);
                    logMsg += " '"+startAddress+"'";
                    try {
                        startLatLon = geocodeAddressToLatLon(args.getString(5));
                    }catch(Exception e){
                        logError("Unable to obtains coords for address '"+startAddress+"': "+e.getMessage());
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(args, 5);
                }

                url += startLatLon;
                logMsg += " ["+startLatLon+"]";

                if(!isNull(startNickname)){
                    url += ","+startNickname;
                    logMsg += " ("+startNickname+")";
                }
            }

            url += "/";
            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";
            url += destLatLon;

            if(!isNull(destNickname)){
                url += ","+destNickname;
                logMsg += " ("+destNickname+")";
            }

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += "?" + extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "HERE Maps app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    private void launchMoovit(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);

            String url = "moovit://directions";
            String logMsg = "Using Moovit to navigate";


            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                try {
                    destLatLon = geocodeAddressToLatLon(args.getString(2));
                }catch(Exception e){
                    logError("Unable to obtains coords for address '"+destAddress+"': "+e.getMessage());
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";

            String[] destPos = splitLatLon(destLatLon);
            url += "?dest_lat=" + destPos[0] + "&dest_lon=" + destPos[1];

            if(!isNull(destNickname)){
                url += "&dest_name="+destNickname;
                logMsg += " ("+destNickname+")";
            }

            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(args, 5);
                    logMsg += " '"+startAddress+"'";
                    try {
                        startLatLon = geocodeAddressToLatLon(args.getString(5));
                    }catch(Exception e){
                        logError("Unable to obtains coords for address '"+startAddress+"': "+e.getMessage());
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(args, 5);
                }

                String[] startPos = splitLatLon(startLatLon);
                url += "&orig_lat=" + startPos[0] + "&orig_lon=" + startPos[1];
                logMsg += " ["+startLatLon+"]";

                if(!isNull(startNickname)){
                    url += "&orig_name="+startNickname;
                    logMsg += " ("+startNickname+")";
                }
            }

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Moovit app is not installed on this device";
            }
            logError("Exception occurred: ".concat(msg));
            callbackContext.error(msg);
        }
    }

    /*
     * Utilities
     */

    private String parseExtrasToUrl(JSONArray args) throws JSONException{
        String extras = null;
        String jsonStringExtras = args.getString(10);
        JSONObject oExtras = null;
        if(!isNull(jsonStringExtras)){
            oExtras =  new JSONObject(jsonStringExtras);
        }

        if(oExtras != null){
            Iterator<?> keys = oExtras.keys();
            extras = "";
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                String value = oExtras.getString(key);
                extras += "&"+key+"="+value;
            }
        }
        return extras;
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

    private String[] splitLatLon(String latlon){
        return latlon.split(",");
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
        availableApps = new HashMap<String, String>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = getAppName(packageName);
            if(!supportedAppPackages.containsValue(packageName)) { // if it's not already an explicitly supported app
                logDebug("Found available app supporting geo protocol: " + appName + " (" + packageName + ")");
                availableApps.put(appName, packageName);
            }
        }

        // Check if explicitly supported apps are installed
        for (Map.Entry<String, String> entry : supportedAppPackages.entrySet()) {
            String _appName = entry.getKey();
            String _packageName = entry.getValue();
            if(isPackageInstalled(_packageName, packageManager)){
                availableApps.put(supportedAppNames.get(_appName), _packageName);
                logDebug(_appName+" is available");
            }else{
                logDebug(_appName + " is not available");
            }
        }
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String geocodeAddressToLatLon(String address) throws Exception {
        address = address.replaceAll(" ", "%20");

        JSONObject oResponse = doGeocode("address=" + address);

        double longitude = oResponse
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

        double latitude = oResponse
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");

        String result = latitude+","+longitude;
        logDebug("Geocoded '"+address+"' to '"+result+"'");
        return result;
    }

    private String reverseGeocodeLatLonToAddress(String latLon) throws Exception {
        JSONObject oResponse = doGeocode("latlng=" + latLon);
        String result = oResponse.getString("formatted_address");
        logDebug("Reverse geocoded '"+latLon+"' to '"+result+"'");
        return result;
    }

    private JSONObject doGeocode(String query) throws Exception{
        String url = "http://maps.google.com/maps/api/geocode/json?" + query + "&sensor=false";
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject oResponse = new JSONObject(responseBody);
        return ((JSONArray)oResponse.get("results")).getJSONObject(0);
    }

    private boolean isNull(String arg){
        return arg == null || arg.equals("null");
    }

    private void logDebug(String msg) {
        if(enableDebug){
            Log.d(LOG_TAG, msg);
            executeGlobalJavascript("console.log(\""+LOG_TAG+"[native]: "+escapeDoubleQuotes(msg)+"\")");
        }
    }

    private void logError(String msg){
        Log.e(LOG_TAG, msg);
        if(enableDebug){
            executeGlobalJavascript("console.error(\""+LOG_TAG+"[native]: "+escapeDoubleQuotes(msg)+"\")");
        }
    }

    private String escapeDoubleQuotes(String string){
        final String escapedString = string.replace("\"", "\\\"");
        return escapedString;
    }

    private void executeGlobalJavascript(final String jsString){
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + jsString);
            }
        });
    }

    private String getAppDisplayName(String packageName){
        String name = "[Not found]";
        if(packageName.equals(GEO)){
            return "[Native chooser]";
        }
        for (Map.Entry<String, String> entry : availableApps.entrySet()) {
            String _appName = entry.getKey();
            String _packageName = entry.getValue();
            if(packageName.equals(_packageName)){
                name = _appName;
                break;
            }
        }
        return name;
    }

}