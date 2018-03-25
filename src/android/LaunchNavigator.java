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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    private static final String LYFT = "lyft";
    private static final String MAPS_ME = "maps_me";
    private static final String CABIFY = "cabify";
    private static final String BAIDU = "baidu";
    private static final String TAXIS_99 = "taxis_99";
    private static final String GAODE = "gaode";


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
        _supportedAppPackages.put(LYFT, "me.lyft.android");
        _supportedAppPackages.put(MAPS_ME, "com.mapswithme.maps.pro");
        _supportedAppPackages.put(CABIFY, "com.cabify.rider");
        _supportedAppPackages.put(BAIDU, "com.baidu.BaiduMap");
        _supportedAppPackages.put(TAXIS_99, "com.taxis99");
        _supportedAppPackages.put(GAODE, "com.autonavi.minimap");
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
        _supportedAppNames.put(LYFT, "Lyft");
        _supportedAppNames.put(MAPS_ME, "MAPS.ME");
        _supportedAppNames.put(CABIFY, "Cabify");
        _supportedAppNames.put(BAIDU, "Baidu Maps");
        _supportedAppNames.put(TAXIS_99, "99 Taxi");
        _supportedAppNames.put(GAODE, "Gaode Maps (Amap)");
        supportedAppNames = Collections.unmodifiableMap(_supportedAppNames);
    }

    private static final String GEO_URI = "geo:";

    PackageManager packageManager;
    Context context;

    OkHttpClient httpClient = new OkHttpClient();

    boolean enableDebug = false;
    boolean enableGeocoding = true;

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
                 * args[11] - enableGeolocation
                 */
                if(args.get(11) != null){
                    enableGeocoding = args.getBoolean(11);
                }
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
                            + "; extras="+ args.getString(10)
                            + "; enableGeocoding="+ args.getString(11);
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
            handleException(e.getMessage(), callbackContext);
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
        }else if(appName.equals(LYFT)){
            launchLyft(args, callbackContext);
        }else if(appName.equals(MAPS_ME)){
            launchMapsMe(args, callbackContext);
        }else if(appName.equals(CABIFY)){
            launchCabify(args, callbackContext);
        }else if(appName.equals(BAIDU)){
            launchBaidu(args, callbackContext);
        }else if(appName.equals(GAODE)){
            launchGaode(args, callbackContext);
        }else if(appName.equals(TAXIS_99)){
            launch99Taxis(args, callbackContext);
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
            destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
            if(destLatLon == null){
                return;
            }
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
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
            }

            String url = "https://citymapper.com/directions?";
            String logMsg = "Using Citymapper to navigate to";
            if(!isNull(destAddress)){
                url += "&endaddress="+Uri.encode(destAddress);
                logMsg += " '"+destAddress+"'";
            }
            if(isNull(destLatLon)){
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }
            url += "&endcoord="+destLatLon;
            logMsg += " ["+destLatLon+"]";
            if(!isNull(destNickname)){
                url += "&endname="+Uri.encode(destNickname);
                logMsg += " ("+destNickname+")";
            }

            if(!sType.equals("none")){
                logMsg += " from";
                if(!isNull(startAddress)){
                    url += "&startaddress="+Uri.encode(startAddress);
                    logMsg += " '"+startAddress+"'";
                }
                if(isNull(startLatLon)){
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
                    }
                }
                url += "&startcoord="+startLatLon;
                logMsg += " ["+startLatLon+"]";
                if(!isNull(startNickname)){
                    url += "&startname="+Uri.encode(startNickname);
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
            handleException(msg, callbackContext);
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
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
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
            handleException(msg, callbackContext);
        }
    }

    private void launchWaze(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;

            String dType = args.getString(1);

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
            }else{
                destLatLon = getLocationFromPos(args, 2);
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
            handleException(msg, callbackContext);
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
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
                startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                if(isNull(startLatLon)){
                    return;
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
            handleException(msg, callbackContext);
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
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }

            logMsg += " ["+destLatLon+"]";

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
            handleException(msg, callbackContext);
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
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
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
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
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
            handleException(msg, callbackContext);
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
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
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
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
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
            handleException(msg, callbackContext);
        }
    }

    private void launchLyft(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;

            String dType = args.getString(1);
            String sType = args.getString(4);

            String url = "lyft://ridetype?";
            String logMsg = "Using Lyft to navigate";

            String extras = parseExtrasToUrl(args);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            if(isNull(extras) || !extras.contains("id=")){
                url += "id=lyft";
            }

            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";

            String[] destPos = splitLatLon(destLatLon);
            url += "&destination[latitude]=" + destPos[0] + "&destination[longitude]=" + destPos[1];

            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(args, 5);
                    logMsg += " '"+startAddress+"'";
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(args, 5);
                }

                String[] startPos = splitLatLon(startLatLon);
                url += "&pickup[latitude]=" + startPos[0] + "&pickup[longitude]=" + startPos[1];
                logMsg += " ["+startLatLon+"]";

            }

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Lyft app is not installed on this device";
            }
            handleException(msg, callbackContext);
        }
    }

    private void launchMapsMe(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;

            String dType = args.getString(1);
            String sType = args.getString(4);
            String transportMode = args.getString(7);

            Intent intent = new Intent(supportedAppPackages.get(MAPS_ME).concat(".action.BUILD_ROUTE"));
            intent.setPackage(supportedAppPackages.get(MAPS_ME));

            String logMsg = "Using MAPs.ME to navigate";

            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";

            String[] destPos = splitLatLon(destLatLon);
            intent.putExtra("lat_to", Double.parseDouble(destPos[0]));
            intent.putExtra("lon_to", Double.parseDouble(destPos[1]));

            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(args, 5);
                    logMsg += " '"+startAddress+"'";
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(args, 5);
                }

                String[] startPos = splitLatLon(startLatLon);
                intent.putExtra("lat_from", Double.parseDouble(startPos[0]));
                intent.putExtra("lon_from", Double.parseDouble(startPos[1]));
                logMsg += " ["+startLatLon+"]";
            }



            if(transportMode.equals("d")){
                transportMode = "vehicle";
            }else if(transportMode.equals("w")){
                transportMode = "pedestrian";
            }else if(transportMode.equals("b")){
                transportMode = "bicycle";
            }else if(transportMode.equals("t")){
                transportMode = "taxi";
            }

            if(!isNull(transportMode)){
                intent.putExtra("router", transportMode);
                logMsg += " by transportMode=" + transportMode;
            }

            logDebug(logMsg);

            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "MAPS.ME app is not installed on this device";
            }
            handleException(msg, callbackContext);
        }
    }

    private void launchCabify(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);

            String url = "cabify://cabify/journey?json=";
            String logMsg = "Using Cabify to navigate";
            JSONObject oJson = new JSONObject();

            // Parse dest
            JSONObject oDest = new JSONObject();
            logMsg += " to";

            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";
            String[] destPos = splitLatLon(destLatLon);

            JSONObject oDestLoc = new JSONObject();
            oDestLoc.put("latitude", destPos[0]);
            oDestLoc.put("longitude", destPos[1]);
            oDest.put("loc", oDestLoc);

            if(!isNull(destNickname)){
                oDest.put("name", destNickname);
                logMsg += " ("+destNickname+")";
            }

            // Parse start
            JSONObject oStart = new JSONObject();
            logMsg += " from";

            if(sType.equals("none")){
                logMsg += " Current Location";
                oStart.put("loc", "current");
            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(args, 5);
                    logMsg += " '"+startAddress+"'";
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    if(isNull(startLatLon)){
                        return;
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(args, 5);
                }
                logMsg += " ["+startLatLon+"]";
                String[] startPos = splitLatLon(startLatLon);

                JSONObject oStartLoc = new JSONObject();
                oStartLoc.put("latitude", startPos[0]);
                oStartLoc.put("longitude", startPos[1]);
                oStart.put("loc", oStartLoc);
            }

            if(!isNull(startNickname)){
                oStart.put("name", startNickname);
                logMsg += " ("+startNickname+")";
            }

            String extras = args.getString(10);
            if(!isNull(extras)){
                oJson =  new JSONObject(extras);
                logMsg += " - extras="+extras;
            }

            // Assemble JSON
            JSONArray aStops = new JSONArray();
            aStops.put(oStart);
            if(oJson.has("stops")){
                JSONArray stops = oJson.getJSONArray("stops");
                for (int i = 0; i < stops.length(); i++) {
                    aStops.put(stops.getJSONObject(i));
                }
            }
            aStops.put(oDest);
            oJson.put("stops", aStops);

            url += oJson.toString();

            logDebug(logMsg);
            logDebug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Cabify app is not installed on this device";
            }
            handleException(msg, callbackContext);
        }
    }

    private void launchBaidu(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String start;
            String dest;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);
            String transportMode = args.getString(7);


            String url = "baidumap://map/direction";
            String logMsg = "Using Baidu Maps to navigate";

            String extras = parseExtrasToUrl(args);
            if(isNull(extras)){
                extras = "";
            }

            if(!extras.contains("coord_type=")){
                extras += "&coord_type=wgs84";
            }

            // Destination
            logMsg += " to";
            if(dType.equals("name")){
                dest = getLocationFromName(args, 2);
                logMsg += dest;
            }else{
                dest = getLocationFromPos(args, 2);
                logMsg += " ["+dest+"]";
                if(!isNull(destNickname)){
                    dest = "latlng:" + dest + "|name:" + destNickname;
                    logMsg += " ("+destNickname+")";
                }
            }
            url += "?destination=" + dest;

            // Start
            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else{
                if(sType.equals("name")){
                    start = getLocationFromName(args, 5);
                    logMsg += start;
                }else{
                    start = getLocationFromPos(args, 5);
                    logMsg += " ["+start+"]";
                    if(!isNull(startNickname)){
                        start = "latlng:" + start + "|name:" + startNickname;
                        logMsg += " ("+startNickname+")";
                    }
                }
                url += "&origin=" + start;
            }

            // Transport mode
            if(transportMode.equals("d")){
                transportMode = "driving";
            }else if(transportMode.equals("w")){
                transportMode = "walking";
            }else if(transportMode.equals("b")){
                transportMode = "riding";
            }else if(transportMode.equals("t")){
                transportMode = "transit";
            }else{
                transportMode = "driving";
            }

            url += "&mode="+transportMode;
            logMsg += " by transportMode=" + transportMode;

            // Extras
            url += extras;
            logMsg += " - extras="+extras;


            logDebug(logMsg);
            logDebug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Baidu Maps app is not installed on this device";
            }
            handleException(msg, callbackContext);
        }
    }

    private void launchGaode(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);
            String transportMode = args.getString(7);


            String url = "amapuri://route/plan/?";
            String logMsg = "Using Gaode Maps to navigate";

            String extras = parseExtrasToUrl(args);
            if(isNull(extras)){
                extras = "";
            }

            if(!extras.contains("sourceApplication=")){
                extras += "&sourceApplication="+Uri.encode(getThisAppName());
            }

            // Destination
            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";
            String[] pos = splitLatLon(destLatLon);
            url += "dlat="+pos[0]+"&dlon="+pos[1];

            // Dest name
            if(!isNull(destNickname)){
                logMsg += " ("+destNickname+")";
                url += "&dname="+destNickname;
            }

            // Start
            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else {
                if (sType.equals("name")) {
                    startAddress = getLocationFromName(args, 5);
                    startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                    logMsg += " '" + startAddress + "'";
                } else{
                    startLatLon = getLocationFromPos(args, 5);
                }
                if (!isNull(startLatLon)) {
                    logMsg += " [" + startLatLon + "]";
                    pos = splitLatLon(startLatLon);
                    url += "&slat=" + pos[0] + "&slon=" + pos[1];

                    // Start name
                    if(!isNull(startNickname)){
                        logMsg += " ("+startNickname+")";
                        url += "&sname="+startNickname;
                    }
                }
            }


            // Transport mode
            String transportModeName;
            if(transportMode.equals("d")){
                transportModeName = "driving";
                transportMode = "0";
            }else if(transportMode.equals("w")){
                transportModeName = "walking";
                transportMode = "2";
            }else if(transportMode.equals("b")){
                transportModeName = "bicycle";
                transportMode = "3";
            }else if(transportMode.equals("t")){
                transportModeName = "transit";
                transportMode = "1";
            }else{
                transportModeName = "driving";
                transportMode = "0";
            }
            url += "&t="+transportMode;
            logMsg += " by transportMode=" + transportModeName;

            // Extras
            url += extras;
            logMsg += " - extras="+extras;


            logDebug(logMsg);
            logDebug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Gaode Maps app is not installed on this device";
            }
            handleException(msg, callbackContext);
        }
    }

    private void launch99Taxis(JSONArray args, CallbackContext callbackContext) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = args.getString(3);
            String startNickname = args.getString(6);

            String dType = args.getString(1);
            String sType = args.getString(4);


            String url = "taxis99://call?";
            String logMsg = "Using 99 Taxi to navigate";

            String extras = parseExtrasToUrl(args);
            if(isNull(extras)){
                extras = "";
            }

            if(!extras.contains("deep_link_product_id")){
                extras += "&deep_link_product_id=316";
            }

            if(!extras.contains("client_id")){
                extras += "&client_id=MAP_123";
            }

            // Destination
            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(args, 2);
                logMsg += " '"+destAddress+"'";
                destLatLon = geocodeAddressToLatLon(args.getString(2), callbackContext);
                if(isNull(destLatLon)){
                    return;
                }
            }else{
                destLatLon = getLocationFromPos(args, 2);
            }
            logMsg += " ["+destLatLon+"]";
            String[] pos = splitLatLon(destLatLon);
            url += "dropoff_latitude="+pos[0]+"&dropoff_longitude="+pos[1];

            // Dest name
            if(isNull(destNickname)){
                if(!isNull(destAddress)){
                    destNickname = destAddress;
                }else{
                    destNickname = "Dropoff";
                }
            }
            logMsg += " ("+destNickname+")";
            url += "&dropoff_title="+destNickname;

            // Start
            logMsg += " from";
            if(sType.equals("name")){
                startAddress = getLocationFromName(args, 5);
                startLatLon = geocodeAddressToLatLon(args.getString(5), callbackContext);
                logMsg += " '"+startAddress+"'";
                if(isNull(startLatLon)){
                    return;
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(args, 5);
            }else{
                handleError("start location is a required parameter for 99 Taxi and must be specified", callbackContext);
                return;
            }
            logMsg += " ["+startLatLon+"]";
            pos = splitLatLon(startLatLon);
            url += "&pickup_latitude="+pos[0]+"&pickup_longitude="+pos[1];

            // Start name
            if(isNull(startNickname)){
                if(!isNull(startAddress)){
                    startNickname = startAddress;
                }else{
                    startNickname = "Pickup";
                }
            }
            logMsg += " ("+startNickname+")";
            url += "&pickup_title="+startNickname;


            // Extras
            url += extras;
            logMsg += " - extras="+extras;

            logDebug(logMsg);
            logDebug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "99 Taxis app is not installed on this device";
            }
            handleException(msg, callbackContext);
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
            ApplicationInfo ai = packageManager.getApplicationInfo(packagename, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String geocodeAddressToLatLon(String address, CallbackContext callbackContext) throws Exception {
        String result = null;
        String errMsg = "Unable to geocode coords from address '"+address;
        try {

            if(!enableGeocoding){
                handleError("Geocoding disabled: "+errMsg, callbackContext);
                return result;
            }

            if(!isNetworkAvailable()){
                handleError("No internet connection: "+errMsg, callbackContext);
                return result;
            }

            address = address.replaceAll(" ", "%20");

            JSONObject oResponse = doGeocode("address=" + address);

            double longitude = oResponse
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double latitude = oResponse
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            result = latitude+","+longitude;
            logDebug("Geocoded '"+address+"' to '"+result+"'");
            return result;
        }catch(Exception e){
            handleException(errMsg+": "+e.getMessage(), callbackContext);
            return result;
        }
    }

    private String reverseGeocodeLatLonToAddress(String latLon, CallbackContext callbackContext) throws Exception {
        String result = null;
        String errMsg = "Unable to reverse geocode address from coords '"+latLon;
        try {
            if(!enableGeocoding){
                handleError("Geocoding disabled: "+errMsg, callbackContext);
                return result;
            }

            if(!isNetworkAvailable()){
                handleError("No internet connection: "+errMsg, callbackContext);
                return result;
            }

            JSONObject oResponse = doGeocode("latlng=" + latLon);
            result = oResponse.getString("formatted_address");
            logDebug("Reverse geocoded '"+latLon+"' to '"+result+"'");
            return result;
        }catch(Exception e){
            handleException(errMsg+": "+e.getMessage(), callbackContext);
            return result;
        }
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

    private void handleError(String msg, CallbackContext callbackContext){
        logError(msg);
        callbackContext.error(msg);
    }

    private void handleException(String msg, CallbackContext callbackContext){
        msg = "Exception occurred: ".concat(msg);
        handleError(msg, callbackContext);
    }

    private String escapeDoubleQuotes(String string){
        String escapedString = string.replace("\"", "\\\"");
        escapedString = escapedString.replace("%22", "\\%22");
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

    private String getThisAppName(){
        return this.context.getApplicationInfo().loadLabel(this.context.getPackageManager()).toString();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}