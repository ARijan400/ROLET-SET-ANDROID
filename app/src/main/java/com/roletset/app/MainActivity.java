
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

    public class PrintBridge {
        @JavascriptInterface
        public void printPage() {
            runOnUiThread(() -> {
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
                    webView.createPrintDocumentAdapter(
                        "ROLET SET"
                    ),
                    attributes
                );
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());

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
}
