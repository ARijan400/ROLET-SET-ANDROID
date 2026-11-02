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

public class MainActivity extends Activity {

    private WebView webView;
    private WebView printWebView;

    public class PrintBridge {
        @JavascriptInterface
        public void printHtml(String html) {
            runOnUiThread(() -> {
                if (html == null || html.isEmpty()) return;

                WebView documentView =
                    new WebView(MainActivity.this);

                printWebView = documentView;

                documentView.getSettings()
                    .setDefaultTextEncodingName("UTF-8");

                documentView.setWebViewClient(
                    new WebViewClient() {
                        @Override
                        public void onPageFinished(
                            WebView view, String url
                        ) {
                            view.postDelayed(() -> {
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
                            }, 300);
                        }
                    }
                );

                documentView.loadDataWithBaseURL(
                    "file:///android_asset/",
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

        webView = new WebView(this);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(
                WebView view, String url
            ) {
                installPrintSupport();
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.addJavascriptInterface(
            new PrintBridge(), "AndroidPrint"
        );

        setContentView(webView);

        webView.loadUrl(
            "file:///android_asset/index.html"
        );
    }

    private void installPrintSupport() {

        String script =
            "(function() {" +
            "function setup(frame) {" +
            "try {" +
            "var w = frame.contentWindow;" +
            "if (!w || !w.document) return;" +

            "w.print = function() {" +
            "var d = w.document;" +
            "var clone = d.documentElement.cloneNode(true);" +

            "var originals = d.querySelectorAll('input,select,textarea');" +
            "var copies = clone.querySelectorAll('input,select,textarea');" +

            "for (var i = 0; i < originals.length; i++) {" +
            "var a = originals[i];" +
            "var b = copies[i];" +
            "if (!b) continue;" +

            "if (a.tagName === 'INPUT') {" +
            "b.setAttribute('value', a.value);" +
            "if (a.checked) b.setAttribute('checked','');" +
            "else b.removeAttribute('checked');" +
            "} else if (a.tagName === 'TEXTAREA') {" +
            "b.textContent = a.value;" +
            "} else if (a.tagName === 'SELECT') {" +
            "for (var j = 0; j < b.options.length; j++) {" +
            "if (a.options[j].selected) " +
            "b.options[j].setAttribute('selected','');" +
            "else b.options[j].removeAttribute('selected');" +
            "}" +
            "}" +
            "}" +

            "clone.querySelectorAll('script').forEach(" +
            "function(s){s.remove();});" +

            "var html = '<!DOCTYPE html>' + clone.outerHTML;" +
            "AndroidPrint.printHtml(html);" +
            "};" +

            "} catch(e) { console.log(e); }" +
            "}" +

            "['prod','price','plise'].forEach(function(id) {" +
            "var frame = document.getElementById(id);" +
            "if (!frame) return;" +
            "frame.addEventListener('load',function(){setup(frame);});" +
            "setup(frame);" +
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
