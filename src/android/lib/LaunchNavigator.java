/*
 * LaunchNavigator library for Android
 *
 * Copyright (c) 2018 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2018 Working Edge Ltd. (http://www.workingedge.co.uk)
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
package uk.co.workingedge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LaunchNavigator {
    /**********************
     * Public properties
     **********************/
    public static final String LOG_TAG = "LaunchNavigator";
    public final String NO_APP_FOUND = "No Activity found to handle Intent";
    public final String MAPS_PROTOCOL = "http://maps.google.com/maps?";
    public final String TURN_BY_TURN_PROTOCOL = "google.navigation:";

    // Explicitly supported apps
    public final String GEO = "geo"; // Use native app choose for geo: intent
    public final String GOOGLE_MAPS = "google_maps";
    public final String CITYMAPPER = "citymapper";
    public final String UBER = "uber";
    public final String WAZE = "waze";
    public final String YANDEX = "yandex";
    public final String SYGIC = "sygic";
    public final String HERE_MAPS = "here_maps";
    public final String MOOVIT = "moovit";
    public final String LYFT = "lyft";
    public final String MAPS_ME = "maps_me";
    public final String CABIFY = "cabify";
    public final String BAIDU = "baidu";
    public final String TAXIS_99 = "taxis_99";
    public final String GAODE = "gaode";


    public final Map<String, String> supportedAppPackages;
    {
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

    public final Map<String, String> supportedAppNames;
    {
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

    public final String GEO_URI = "geo:";

    /**********************
     * Internal properties
     **********************/
    boolean geocodingEnabled = true;

    PackageManager packageManager;
    Context context;
    OkHttpClient httpClient = new OkHttpClient();
    ILogger logger;


    // Map of app name to package name
    Map<String, String> availableApps;

    private final String[] navigateParams = {
            "app",
            "dType",
            "dest",
            "destNickname",
            "sType",
            "start",
            "startNickname",
            "transportMode",
            "launchMode",
            "extras"
    };

    String googleApiKey = null;


    /*******************
     * Constructors
     *******************/

    public LaunchNavigator(Context context, ILogger logger) throws Exception {
        setLogger(logger);
        initialize(context);
    }

    public LaunchNavigator(Context context, ILogger logger, boolean geocodingEnabled) throws Exception {
        this.geocodingEnabled = geocodingEnabled;
        setLogger(logger);
        initialize(context);
    }


    /*******************
     * Public API
     *******************/

    public void setGoogleApiKey(String googleApiKey){
        this.googleApiKey = googleApiKey;
    }

    public void setLogger(ILogger logger){
        this.logger = logger;
    }

    public ILogger getLogger(){
        return this.logger;
    }

    public void setGeocoding(boolean geocodingEnabled){
        this.geocodingEnabled = geocodingEnabled;
    }

    public JSONObject getGeoApps() throws JSONException{
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
        return apps;
    }

    public JSONObject getAvailableApps() throws Exception{
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
        return apps;
    }

    public boolean isAppAvailable(String appName){
        if(supportedAppPackages.containsKey(appName)){
            appName = supportedAppPackages.get(appName);
        }

        return availableApps.containsValue(appName);
    }

    public String navigate(JSONObject params) throws Exception{
        params = ensureNavigateKeys(params);

        String navigateArgs = "Called navigate() with params";
        for(String param : navigateParams){
            navigateArgs += "; "+param+"="+params.getString(param);
        }
        logger.debug(navigateArgs);

        String appName = params.getString("app");
        String launchMode = params.getString("launchMode");

        String error;

        if(appName.equals(GOOGLE_MAPS) && !launchMode.equals("geo")){
            error = launchGoogleMaps(params);
        }else if(appName.equals(CITYMAPPER)){
            error = launchCitymapper(params);
        }else if(appName.equals(UBER)){
            error = launchUber(params);
        }else if(appName.equals(WAZE)){
            error = launchWaze(params);
        }else if(appName.equals(YANDEX)){
            error = launchYandex(params);
        }else if(appName.equals(SYGIC)){
            error = launchSygic(params);
        }else if(appName.equals(HERE_MAPS)){
            error = launchHereMaps(params);
        }else if(appName.equals(MOOVIT)){
            error = launchMoovit(params);
        }else if(appName.equals(LYFT)){
            error = launchLyft(params);
        }else if(appName.equals(MAPS_ME)){
            error = launchMapsMe(params);
        }else if(appName.equals(CABIFY)){
            error = launchCabify(params);
        }else if(appName.equals(BAIDU)){
            error = launchBaidu(params);
        }else if(appName.equals(GAODE)){
            error = launchGaode(params);
        }else if(appName.equals(TAXIS_99)){
            error = launch99Taxis(params);
        }else{
            error = launchApp(params);
        }
        return error;
    }


    /*******************
     * Internal methods
     *******************/

    private void initialize(Context context) throws Exception {
        if(context == null){
            throw new Exception(LOG_TAG+": null context passed to initialize()");
        }
        this.context = context;

        this.packageManager = context.getPackageManager();
        discoverAvailableApps();
    }

    private void discoverAvailableApps(){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GEO_URI));
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        availableApps = new HashMap<String, String>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = getAppName(packageName);
            if(!supportedAppPackages.containsValue(packageName)) { // if it's not already an explicitly supported app
                logger.debug("Found available app supporting geo protocol: " + appName + " (" + packageName + ")");
                availableApps.put(appName, packageName);
            }
        }

        // Check if explicitly supported apps are installed
        for (Map.Entry<String, String> entry : supportedAppPackages.entrySet()) {
            String _appName = entry.getKey();
            String _packageName = entry.getValue();
            if(isPackageInstalled(_packageName, packageManager)){
                availableApps.put(supportedAppNames.get(_appName), _packageName);
                logger.debug(_appName+" is available");
            }else{
                logger.debug(_appName + " is not available");
            }
        }
    }

    private String launchApp(JSONObject params) throws Exception{
        String appName = params.getString("app");
        String dType = params.getString("dType");
        String dNickName = params.getString("destNickname");

        String logMsg = "Using " + getAppDisplayName(appName)+" to navigate to ";
        String destLatLon = null;
        String destName = null;
        String dest;

        if(dType.equals("name")){
            destName = getLocationFromName(params, "dest");
            try{
                destLatLon = geocodeAddressToLatLon(params.getString("dest"));
            }catch(Exception e){
                return "Unable to geocode destination address to coordinates: " + e.getMessage();
            }
            logMsg += destName;
            if(!isNull(destLatLon)){
                logMsg += "["+destLatLon+"]";
            }

        }else{
            destLatLon = getLocationFromPos(params, "dest");
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

        String extras = parseExtrasToUrl(params);
        if(!isNull(extras)){
            uri += extras;
            logMsg += " - extras="+extras;
        }

        logger.debug(logMsg);
        logger.debug("URI: " + uri);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(!appName.equals(GEO)){
            if(appName.equals(GOOGLE_MAPS)) {
                appName = supportedAppPackages.get(GOOGLE_MAPS);
            }
            intent.setPackage(appName);
        }
        invokeIntent(intent);
        return null;
    }

    private String launchGoogleMaps(JSONObject params) throws Exception{
        try {
            String destination;
            String start = null;

            String dType = params.getString("dType");
            if(dType.equals("pos")){
                destination = getLocationFromPos(params, "dest");
            }else{
                destination = getLocationFromName(params, "dest");
            }

            String sType = params.getString("sType");
            if(sType.equals("pos")){
                start = getLocationFromPos(params, "start");
            }else if(sType.equals("name")){
                start = getLocationFromName(params, "start");
            }
            String transportMode = params.getString("transportMode");
            String launchMode = params.getString("launchMode");

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

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setClassName(supportedAppPackages.get(GOOGLE_MAPS), "com.google.android.maps.MapsActivity");
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Google Maps app is not installed on this device";
            }
            logger.error("Exception occurred: ".concat(msg));
            return msg;
        }
    }

    private String launchCitymapper(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(params, "start");
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(params, "start");
            }

            String url = "https://citymapper.com/directions?";
            String logMsg = "Using Citymapper to navigate to";
            if(!isNull(destAddress)){
                url += "&endaddress="+Uri.encode(destAddress);
                logMsg += " '"+destAddress+"'";
            }
            if(isNull(destLatLon)){
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
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
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }
                url += "&startcoord="+startLatLon;
                logMsg += " ["+startLatLon+"]";
                if(!isNull(startNickname)){
                    url += "&startname="+Uri.encode(startNickname);
                    logMsg += " ("+startNickname+")";
                }
            }

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Citymapper app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchUber(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(params, "start");
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(params, "start");
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

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Uber app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchWaze(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;

            String dType = params.getString("dType");

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }

            String url = "waze://?";
            String logMsg = "Using Waze to navigate to";
            if(!isNull(destLatLon)){
                url += "ll="+destLatLon;
                logMsg += " ["+destLatLon+"]";
            }else{
                url += "q="+destAddress;
                logMsg += " '"+destAddress+"'";
            }
            url += "&navigate=yes";

            logMsg += " from current location";

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Waze app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchYandex(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }

            if(sType.equals("name")){
                startAddress = getLocationFromName(params, "start");
                try{
                    startLatLon = geocodeAddressToLatLon(params.getString("start"));
                }catch(Exception e){
                    return "Unable to geocode start address to coordinates: " + e.getMessage();
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(params, "start");
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

            String jsonStringExtras = params.getString("extras");
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
            logger.debug(logMsg);
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Yandex app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchSygic(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;

            String dType = params.getString("dType");
            String transportMode = params.getString("transportMode");
            String url = supportedAppPackages.get(SYGIC)+"://coordinate|";
            String logMsg = "Using Sygic to navigate to";

            if(transportMode.equals("w")){
                transportMode = "walk";
            }else{
                transportMode = "drive";
            }

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }

            logMsg += " ["+destLatLon+"]";

            String[] pos = splitLatLon(destLatLon);
            url += pos[1]+"|"+pos[0]+"|"+transportMode;

            logMsg += " by " + transportMode;

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Sygic app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchHereMaps(JSONObject params) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            String url = "https://share.here.com/r/";
            String logMsg = "Using HERE Maps to navigate";

            logMsg += " from";
            if(sType.equals("none")){
                url += "mylocation";
                logMsg += " Current Location";

            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '"+startAddress+"'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(params, "start");
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
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }
            logMsg += " ["+destLatLon+"]";
            url += destLatLon;

            if(!isNull(destNickname)){
                url += ","+destNickname;
                logMsg += " ("+destNickname+")";
            }

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += "?" + extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "HERE Maps app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchMoovit(JSONObject params) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            String url = "moovit://directions";
            String logMsg = "Using Moovit to navigate";


            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
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
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '"+startAddress+"'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(params, "start");
                }

                String[] startPos = splitLatLon(startLatLon);
                url += "&orig_lat=" + startPos[0] + "&orig_lon=" + startPos[1];
                logMsg += " ["+startLatLon+"]";

                if(!isNull(startNickname)){
                    url += "&orig_name="+startNickname;
                    logMsg += " ("+startNickname+")";
                }
            }

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Moovit app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchLyft(JSONObject params) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            String url = "lyft://ridetype?";
            String logMsg = "Using Lyft to navigate";

            String extras = parseExtrasToUrl(params);
            if(!isNull(extras)){
                url += extras;
                logMsg += " - extras="+extras;
            }

            if(isNull(extras) || !extras.contains("id=")){
                url += "id=lyft";
            }

            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
            }
            logMsg += " ["+destLatLon+"]";

            String[] destPos = splitLatLon(destLatLon);
            url += "&destination[latitude]=" + destPos[0] + "&destination[longitude]=" + destPos[1];

            logMsg += " from";
            if(sType.equals("none")){
                logMsg += " Current Location";
            }else{
                if(sType.equals("name")){
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '"+startAddress+"'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(params, "start");
                }

                String[] startPos = splitLatLon(startLatLon);
                url += "&pickup[latitude]=" + startPos[0] + "&pickup[longitude]=" + startPos[1];
                logMsg += " ["+startLatLon+"]";

            }

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Lyft app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchMapsMe(JSONObject params) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;

            String dType = params.getString("dType");
            String sType = params.getString("sType");
            String transportMode = params.getString("transportMode");

            Intent intent = new Intent(supportedAppPackages.get(MAPS_ME).concat(".action.BUILD_ROUTE"));
            intent.setPackage(supportedAppPackages.get(MAPS_ME));

            String logMsg = "Using MAPs.ME to navigate";

            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
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
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '"+startAddress+"'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(params, "start");
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

            logger.debug(logMsg);

            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "MAPS.ME app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchCabify(JSONObject params) throws Exception{
        try {
            String destAddress;
            String destLatLon = null;
            String startAddress;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");

            String url = "cabify://cabify/journey?json=";
            String logMsg = "Using Cabify to navigate";
            JSONObject oJson = new JSONObject();

            // Parse dest
            JSONObject oDest = new JSONObject();
            logMsg += " to";

            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
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
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '"+startAddress+"'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        return "Unable to geocode start address to coordinates: " + e.getMessage();
                    }
                }else if(sType.equals("pos")){
                    startLatLon = getLocationFromPos(params, "start");
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

            String extras = params.getString("extras");
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

            logger.debug(logMsg);
            logger.debug("URI: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Cabify app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchBaidu(JSONObject params) throws Exception{
        try {
            String start;
            String dest;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");
            String transportMode = params.getString("transportMode");


            String url = "baidumap://map/direction";
            String logMsg = "Using Baidu Maps to navigate";

            String extras = parseExtrasToUrl(params);
            if(isNull(extras)){
                extras = "";
            }

            if(!extras.contains("coord_type=")){
                extras += "&coord_type=wgs84";
            }

            // Destination
            logMsg += " to";
            if(dType.equals("name")){
                dest = getLocationFromName(params, "dest");
                logMsg += dest;
            }else{
                dest = getLocationFromPos(params, "dest");
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
                    start = getLocationFromName(params, "start");
                    logMsg += start;
                }else{
                    start = getLocationFromPos(params, "start");
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


            logger.debug(logMsg);
            logger.debug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Baidu Maps app is not installed on this device";
            }
            return msg;
        }
    }

    private String launchGaode(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");
            String transportMode = params.getString("transportMode");


            String url = "amapuri://route/plan/?";
            String logMsg = "Using Gaode Maps to navigate";

            String extras = parseExtrasToUrl(params);
            if(isNull(extras)){
                extras = "";
            }

            if(!extras.contains("sourceApplication=")){
                extras += "&sourceApplication="+Uri.encode(getThisAppName());
            }

            // Destination
            logMsg += " to";
            if(dType.equals("name")){
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
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
                    startAddress = getLocationFromName(params, "start");
                    logMsg += " '" + startAddress + "'";
                    try{
                        startLatLon = geocodeAddressToLatLon(params.getString("start"));
                    }catch(Exception e){
                        startLatLon = null;
                    }
                } else{
                    startLatLon = getLocationFromPos(params, "start");
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


            logger.debug(logMsg);
            logger.debug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "Gaode Maps app is not installed on this device";
            }
            return msg;
        }
    }

    private String launch99Taxis(JSONObject params) throws Exception{
        try {
            String destAddress = null;
            String destLatLon = null;
            String startAddress = null;
            String startLatLon = null;
            String destNickname = params.getString("destNickname");
            String startNickname = params.getString("startNickname");

            String dType = params.getString("dType");
            String sType = params.getString("sType");


            String url = "taxis99://call?";
            String logMsg = "Using 99 Taxi to navigate";

            String extras = parseExtrasToUrl(params);
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
                destAddress = getLocationFromName(params, "dest");
                logMsg += " '"+destAddress+"'";
                try{
                    destLatLon = geocodeAddressToLatLon(params.getString("dest"));
                }catch(Exception e){
                    return "Unable to geocode destination address to coordinates: " + e.getMessage();
                }
            }else{
                destLatLon = getLocationFromPos(params, "dest");
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
                startAddress = getLocationFromName(params, "start");
                logMsg += " '"+startAddress+"'";
                try{
                    startLatLon = geocodeAddressToLatLon(params.getString("start"));
                }catch(Exception e){
                    return "Unable to geocode start address to coordinates: " + e.getMessage();
                }
            }else if(sType.equals("pos")){
                startLatLon = getLocationFromPos(params, "start");
            }else{
                return "start location is a required parameter for 99 Taxi and must be specified";
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

            logger.debug(logMsg);
            logger.debug("URI: " + url);

            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            invokeIntent(intent);
            return null;
        }catch( JSONException e ) {
            String msg = e.getMessage();
            if(msg.contains(NO_APP_FOUND)){
                msg = "99 Taxis app is not installed on this device";
            }
            return msg;
        }
    }

    /*
     * Utilities
     */
    private void invokeIntent(Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private String parseExtrasToUrl(JSONObject params) throws JSONException{
        String extras = null;
        String jsonStringExtras = params.getString("extras");
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

    private String getLocationFromPos(JSONObject params, String key) throws Exception{
        String location;
        JSONArray pos = new JSONArray(params.getString(key));
        String lat = pos.getString(0);
        String lon = pos.getString(1);
        if (isNull(lat) || lat.length() == 0 || isNull(lon) || lon.length() == 0) {
            throw new Exception("Expected two non-empty string arguments for lat/lon.");
        }
        location = lat + "," + lon;
        return location;
    }

    private String getLocationFromName(JSONObject params, String key) throws Exception{
        String name = params.getString(key);
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



    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            ApplicationInfo ai = packageManager.getApplicationInfo(packagename, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String geocodeAddressToLatLon(String address) throws Exception {
        String result;
        String errMsg = "Unable to geocode coords from address '"+address;

        if(!geocodingEnabled){
            throw new Exception("Geocoding disabled: "+errMsg);
        }

        if(!isNetworkAvailable()){
            throw new Exception("No internet connection: "+errMsg);
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
        logger.debug("Geocoded '"+address+"' to '"+result+"'");
        return result;
    }

    private String reverseGeocodeLatLonToAddress(String latLon) throws Exception {
        String result;
        String errMsg = "Unable to reverse geocode address from coords '"+latLon;
        if(!geocodingEnabled){
            throw new Exception("Geocoding is disabled: "+errMsg);
        }

        if(!isNetworkAvailable()){
            throw new Exception("No internet connection: "+errMsg);
        }

        JSONObject oResponse = doGeocode("latlng=" + latLon);
        result = oResponse.getString("formatted_address");
        logger.debug("Reverse geocoded '"+latLon+"' to '"+result+"'");
        return result;
    }

    private JSONObject doGeocode(String query) throws Exception{
        if(this.googleApiKey == null){
            throw new Exception("Google API key has not been specified");
        }
        String url = "https://maps.google.com/maps/api/geocode/json?" + query + "&sensor=false&key="+this.googleApiKey;
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject oResponse = new JSONObject(responseBody);
        if(oResponse.has("error_message")){
            throw new Exception(oResponse.getString("error_message"));
        }
        return ((JSONArray)oResponse.get("results")).getJSONObject(0);
    }

    private boolean isNull(String arg){
        return arg == null || arg.equals("null");
    }

    private JSONObject ensureNavigateKeys(JSONObject params) throws Exception{
        for(String param : navigateParams){
            if(!params.has(param)){
                params.put(param, "null");
            }
        }
        return params;
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
        return context.getApplicationInfo().loadLabel(packageManager).toString();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}