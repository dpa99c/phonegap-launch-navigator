/*
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
package uk.co.workingedge.phonegap.plugin;

import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import uk.co.workingedge.ILogger;

public class CordovaLogger implements ILogger {

    /**********************
     * Internal properties
     **********************/
    private boolean enabled = false;
    private CordovaInterface cordova;
    private CordovaWebView webView;
    private String logTag;

    /*******************
     * Constructors
     *******************/
    public CordovaLogger(CordovaInterface cordova, CordovaWebView webView, String logTag) {
        initialize(cordova, webView, logTag);
    }

    public CordovaLogger(CordovaInterface cordova, CordovaWebView webView, String logTag, boolean enabled) {
        initialize(cordova, webView, logTag);
        setEnabled(enabled);
    }

    /*******************
     * Public API
     *******************/
    @Override
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    @Override
    public boolean getEnabled(){
        return this.enabled;
    }

    @Override
    public void error(String msg) {
        Log.e(logTag, msg);
        logToCordova(msg, "error");
    }

    @Override
    public void warn(String msg) {
        Log.w(logTag, msg);
        logToCordova(msg, "warn");
    }

    @Override
    public void info(String msg) {
        Log.i(logTag, msg);
        logToCordova(msg, "info");
    }

    @Override
    public void debug(String msg) {
        Log.d(logTag, msg);
        logToCordova(msg, "log");
    }

    @Override
    public void verbose(String msg) {
        Log.v(logTag, msg);
        logToCordova(msg, "debug");
    }

    /*******************
     * Internal methods
     *******************/
    private void initialize(CordovaInterface cordova, CordovaWebView webView, String logTag){
        this.cordova = cordova;
        this.webView = webView;
        this.logTag = logTag;
    }

    private void logToCordova(String msg, String logLevel){
        if(enabled){
            executeGlobalJavascript("console."+logLevel+"(\""+logTag+": "+escapeDoubleQuotes(msg)+"\")");
        }
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
}