
package com.roletset.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.print.PrintManager;
import android.print.PrintAttributes;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebResourceRequestCompat;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;

public class MainActivity extends Activity {

    private FrameLayout root;
    private WebView webView;
    private WebView printWebView;

    private final WebViewAssetLoader assetLoader =
        new WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                new WebViewAssetLoader.AssetsPathHandler(this)
            )
            .build();

    public class PrintBridge {
        @JavascriptInterface
        public void printHtml(String html) {
            runOnUiThread(() -> {
                if (html == null || html.isEmpty()) return;

                if (printWebView != null) {
                    root.removeView(printWebView);
                    printWebView.destroy();
                }

                WebView documentView =
                    new WebView(MainActivity.this);

                printWebView = documentView;

                documentView.getSettings()
                    .setDefaultTextEncodingName("UTF-8");

                root.addView(
                    documentView,
                    0,
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                );

                documentView.setWebViewClient(
                    new WebViewClient() {
                        @Override
                        public void onPageFinished(
                            WebView view, String url
                        ) {
                            view.postDelayed(() -> {
                                if (printWebView != view) return;

                                PrintManager manager =
                                    (PrintManager) getSystemService(
                                        Context.PRINT_SERVICE
                                    );

                                PrintAttributes attributes =
                                    new PrintAttributes.Builder()
                                        .setMediaSize(
                                            PrintAttributes.MediaSize.ISO_A4
                                        )
                                        .build();

                                manager.print(
                                    "ROLET SET",
                                    view.createPrintDocumentAdapter(
                                        "ROLET SET"
                                    ),
                                    attributes
                                );
                            }, 500);
                        }
                    }
                );

                documentView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/assets/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                );
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        webView = new WebView(this);

        root.addView(
            webView,
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );

        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.addJavascriptInterface(
            new PrintBridge(), "AndroidPrint"
        );

        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                WebView view, WebResourceRequest request
            ) {
                return assetLoader.shouldInterceptRequest(
                    request.getUrl()
                );
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                WebView view, String url
            ) {
                return assetLoader.shouldInterceptRequest(
                    android.net.Uri.parse(url)
                );
            }

            @Override
            public void onPageFinished(
                WebView view, String url
            ) {
                installPrintSupport();
            }
        });

        webView.loadUrl(
            "https://appassets.androidplatform.net/assets/index.html"
        );
    }

    private void installPrintSupport() {
        String script =
            "(function() {" +
            "function setup(frame) {" +
            "try {" +
            "var w = frame.contentWindow;" +
            "if (!w || !w.document || !w.document.body) return;" +

            "w.print = function() {" +
            "var d = w.document;" +
            "var clone = d.documentElement.cloneNode(true);" +

            "var a = d.querySelectorAll('input,select,textarea');" +
            "var b = clone.querySelectorAll('input,select,textarea');" +

            "for (var i = 0; i < a.length; i++) {" +
            "if (!b[i]) continue;" +

            "if (a[i].tagName === 'INPUT') {" +
            "b[i].setAttribute('value',a[i].value);" +
            "if (a[i].checked) b[i].setAttribute('checked','');" +
            "else b[i].removeAttribute('checked');" +
            "}" +

            "if (a[i].tagName === 'TEXTAREA') {" +
            "b[i].textContent = a[i].value;" +
            "}" +

            "if (a[i].tagName === 'SELECT') {" +
            "for (var j=0;j<b[i].options.length;j++) {" +
            "if (a[i].options[j].selected) " +
            "b[i].options[j].setAttribute('selected','');" +
            "else b[i].options[j].removeAttribute('selected');" +
            "}" +
            "}" +
            "}" +

            "clone.querySelectorAll('script').forEach(" +
            "function(s){s.remove();});" +

            "var html='<!DOCTYPE html>'+clone.outerHTML;" +
            "w.AndroidPrint.printHtml(html);" +
            "};" +

            "} catch(e) { console.log(e); }" +
            "}" +

            "['prod','price','plise'].forEach(function(id) {" +
            "var f=document.getElementById(id);" +
            "if (!f) return;" +
            "f.addEventListener('load',function(){setup(f);});" +
            "setup(f);" +
            "});" +
            "})();";

        webView.evaluateJavascript(script, null);
    }

    @Override
    protected void onDestroy() {
        if (printWebView != null) {
            printWebView.destroy();
            printWebView = null;
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
