package xyz.robotispsoft.mathborsunmi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class MainActivity extends Activity {

    private WebView webView;
    private SunmiPrinterService printerService;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final InnerPrinterCallback printerCallback =
            new InnerPrinterCallback() {

        @Override
        public void onConnected(SunmiPrinterService service) {
            printerService = service;
            toast("SUNMI Printer Connected");
        }

        @Override
        public void onDisconnected() {
            printerService = null;
            toast("SUNMI Printer Disconnected");
        }
    };

    @SuppressLint({
            "SetJavaScriptEnabled",
            "AddJavascriptInterface"
    })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                return false;
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                // Web page-কে জানিয়ে দিচ্ছি যে SUNMI WebView APK চলছে
                String js =
                        "javascript:(function(){" +
                        "window.IS_SUNMI_APP=true;" +
                        "console.log('SUNMI APP BRIDGE READY');" +
                        "})();";

                view.evaluateJavascript(js, null);
            }
        });

        // IMPORTANT:
        // Website থেকে এই নামেই native Android call হবে
        SunmiBridge bridge = new SunmiBridge();

        webView.addJavascriptInterface(
                bridge,
                "SunmiBridge"
        );

        // পুরনো code compatibility
        webView.addJavascriptInterface(
                bridge,
                "lee"
        );

        // SUNMI printer service bind
        try {

            boolean result =
                    InnerPrinterManager.getInstance()
                            .bindService(
                                    this,
                                    printerCallback
                            );

            if (!result) {
                toast("SUNMI Printer Service bind failed");
            }

        } catch (InnerPrinterException e) {

            toast(
                    "SUNMI Printer Service Error: "
                            + e.getMessage()
            );
        }

        // Website
        webView.loadUrl(
                "https://mathbor.robotispsoft.xyz/"
        );
    }

    // =====================================================
    // JAVASCRIPT -> ANDROID BRIDGE
    // =====================================================

    public class SunmiBridge {

        @JavascriptInterface
        public String getStatus() {

            if (printerService != null) {
                return "connected";
            }

            return "disconnected";
        }

        @JavascriptInterface
        public boolean isReady() {

            return printerService != null;
        }

        @JavascriptInterface
        public void initPrinter() {

            if (printerService == null) {

                toast(
                        "SUNMI Printer Service connected নেই"
                );

                return;
            }

            try {

                printerService.printerInit(
                        new InnerResultCallback() {

                            @Override
                            public void onRunResult(
                                    boolean success
                            ) {
                                toast(
                                        success
                                                ? "Printer Ready"
                                                : "Printer Init Failed"
                                );
                            }

                            @Override
                            public void onReturnString(
                                    String result
                            ) {
                            }

                            @Override
                            public void onRaiseException(
                                    int code,
                                    String msg
                            ) {
                                toast(
                                        "Printer Error: "
                                                + msg
                                );
                            }

                            @Override
                            public void onPrintResult(
                                    int code,
                                    String msg
                            ) {
                            }
                        }
                );

            } catch (Exception e) {

                toast(
                        "Printer Init Error: "
                                + e.getMessage()
                );
            }
        }

        @JavascriptInterface
        public void printReceipt(
                final String text
        ) {

            printTextInternal(
                    text == null ? "" : text
            );
        }

        @JavascriptInterface
        public void printText(
                final String text
        ) {

            printTextInternal(
                    text == null ? "" : text
            );
        }

        // পুরনো website code-এর জন্য
        @JavascriptInterface
        public void funAndroid(
                final String text
        ) {

            printTextInternal(
                    text == null ? "" : text
            );
        }
    }

    // =====================================================
    // ACTUAL PRINT
    // =====================================================

    private void printTextInternal(
            final String text
    ) {

        handler.post(() -> {

            if (printerService == null) {

                toast(
                        "SUNMI Printer connected নেই"
                );

                return;
            }

            try {

                printerService.printerInit(
                        new InnerResultCallback() {

                            @Override
                            public void onRunResult(
                                    boolean success
                            ) {
                            }

                            @Override
                            public void onReturnString(
                                    String result
                            ) {
                            }

                            @Override
                            public void onRaiseException(
                                    int code,
                                    String msg
                            ) {
                                toast(
                                        "Printer Error: "
                                                + msg
                                );
                            }

                            @Override
                            public void onPrintResult(
                                    int code,
                                    String msg
                            ) {
                            }
                        }
                );

                // Left align
                printerService.setAlignment(
                        0,
                        null
                );

                // Print
                printerService.printText(
                        text + "\n",
                        new InnerResultCallback() {

                            @Override
                            public void onRunResult(
                                    boolean success
                            ) {

                                if (success) {

                                    toast(
                                            "Receipt Printed"
                                    );

                                } else {

                                    toast(
                                            "Print Failed"
                                    );
                                }
                            }

                            @Override
                            public void onReturnString(
                                    String result
                            ) {
                            }

                            @Override
                            public void onRaiseException(
                                    int code,
                                    String msg
                            ) {

                                toast(
                                        "Print Error: "
                                                + msg
                                );
                            }

                            @Override
                            public void onPrintResult(
                                    int code,
                                    String msg
                            ) {
                            }
                        }
                );

                // Paper advance
                printerService.lineWrap(
                        4,
                        null
                );

            } catch (Exception e) {

                toast(
                        "Print Exception: "
                                + e.getMessage()
                );
            }
        });
    }

    private void toast(
            final String message
    ) {

        handler.post(() ->
                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    // =====================================================
    // BACK BUTTON
    // =====================================================

    @Override
    public void onBackPressed() {

        if (
                webView != null
                        && webView.canGoBack()
        ) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    // =====================================================
    // DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        try {

            InnerPrinterManager.getInstance()
                    .unBindService(
                            this,
                            printerCallback
                    );

        } catch (Exception ignored) {
        }

        if (webView != null) {

            webView.removeJavascriptInterface(
                    "SunmiBridge"
            );

            webView.removeJavascriptInterface(
                    "lee"
            );

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
