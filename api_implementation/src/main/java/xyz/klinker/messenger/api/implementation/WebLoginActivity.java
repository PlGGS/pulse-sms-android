/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.api.implementation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Signs in through the real Pulse web login page, shown in a WebView.
 *
 * The API's login endpoint requires a reCaptcha token, which a native form cannot produce. The web
 * page can, so the user signs in there and this watches the page's own login request go by,
 * handing the request and response bodies back to {@link LoginActivity}. That response carries the
 * account id and salts, and the request carries the password, which together are everything the
 * encryption key is derived from on device.
 */
public class WebLoginActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_BODY = "request_body";
    public static final String EXTRA_RESPONSE_BODY = "response_body";

    private static final String LOGIN_URL = "https://pulsesms.app/login";
    private static final String BRIDGE = "PulseLoginBridge";

    /**
     * Wraps XMLHttpRequest and fetch so the login call reports itself as it completes. Installed on
     * every page event and guarded by a flag, since a single-page app will not reload between the
     * page appearing and the user submitting the form.
     */
    private static final String HOOK = ""
            + "(function(){"
            + "  if (window.__pulseHookInstalled) { return; }"
            + "  window.__pulseHookInstalled = true;"
            + "  var LOGIN = 'accounts/login';"
            + "  function report(req, res) {"
            + "    try { " + BRIDGE + ".onLoginCaptured(String(req == null ? '' : req), String(res == null ? '' : res)); } catch (e) {}"
            + "  }"
            + "  function readBody(xhr) {"
            + "    try { if (!xhr.responseType || xhr.responseType === 'text') { return xhr.responseText; } } catch (e) {}"
            + "    try { return JSON.stringify(xhr.response); } catch (e) {}"
            + "    return '';"
            + "  }"
            + "  var open = XMLHttpRequest.prototype.open;"
            + "  var send = XMLHttpRequest.prototype.send;"
            + "  XMLHttpRequest.prototype.open = function(method, url) {"
            + "    this.__pulseUrl = url;"
            + "    return open.apply(this, arguments);"
            + "  };"
            + "  XMLHttpRequest.prototype.send = function(body) {"
            + "    var xhr = this;"
            + "    if (xhr.__pulseUrl && String(xhr.__pulseUrl).indexOf(LOGIN) !== -1) {"
            + "      xhr.addEventListener('load', function() {"
            + "        if (xhr.status >= 200 && xhr.status < 300) { report(body, readBody(xhr)); }"
            + "      });"
            + "    }"
            + "    return send.apply(this, arguments);"
            + "  };"
            + "  if (window.fetch) {"
            + "    var originalFetch = window.fetch;"
            + "    window.fetch = function(input, init) {"
            + "      var url = (typeof input === 'string') ? input : ((input && input.url) || '');"
            + "      var body = init && init.body;"
            + "      return originalFetch.apply(this, arguments).then(function(response) {"
            + "        if (String(url).indexOf(LOGIN) !== -1 && response.ok) {"
            + "          try { response.clone().text().then(function(text) { report(body, text); }); } catch (e) {}"
            + "        }"
            + "        return response;"
            + "      });"
            + "    };"
            + "  }"
            + "})();";

    private WebView webView;
    private boolean delivered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_activity_web_login);

        webView = findViewById(R.id.web_login);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.addJavascriptInterface(new LoginBridge(), BRIDGE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                view.evaluateJavascript(HOOK, null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(HOOK, null);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    Toast.makeText(getApplicationContext(), R.string.api_web_login_load_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.loadUrl(LOGIN_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class LoginBridge {
        /**
         * Called from the page's thread, not the UI thread. Only the first successful login is
         * passed on, so a retry after a failed attempt cannot deliver twice.
         */
        @JavascriptInterface
        public void onLoginCaptured(final String requestBody, final String responseBody) {
            runOnUiThread(() -> {
                if (delivered) {
                    return;
                }
                delivered = true;

                Intent result = new Intent();
                result.putExtra(EXTRA_REQUEST_BODY, requestBody);
                result.putExtra(EXTRA_RESPONSE_BODY, responseBody);
                setResult(Activity.RESULT_OK, result);
                finish();
            });
        }
    }
}
