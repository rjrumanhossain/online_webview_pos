package xyz.robotispsoft.mathborsunmi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // =========================================================
    // SUNMI PRINTER CONNECTION
    // =========================================================

    private final InnerPrinterCallback printerCallback =
            new InnerPrinterCallback() {

                @Override
                public void onConnected(SunmiPrinterService service) {

                    printerService = service;

                    toast("SUNMI printer connected");
                }

                @Override
                public void onDisconnected() {

                    printerService = null;

                    toast("SUNMI printer disconnected");
                }
            };

    // =========================================================
    // ACTIVITY CREATE
    // =========================================================

    @SuppressLint({
            "SetJavaScriptEnabled",
            "AddJavascriptInterface"
    })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -----------------------------------------------------
        // WEBVIEW
        // -----------------------------------------------------

        webView = new WebView(this);

        setContentView(webView);

        // -----------------------------------------------------
        // WEBVIEW SETTINGS
        // -----------------------------------------------------

        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setDomStorageEnabled(true);

        webView.getSettings().setDatabaseEnabled(true);

        webView.getSettings().setAllowFileAccess(true);

        webView.getSettings().setAllowContentAccess(true);

        // -----------------------------------------------------
        // CHROME CLIENT
        // -----------------------------------------------------

        webView.setWebChromeClient(
                new WebChromeClient()
        );

        // -----------------------------------------------------
        // JAVASCRIPT BRIDGE
        // -----------------------------------------------------

        SunmiBridge bridge = new SunmiBridge();

        /*
         * Website JavaScript:
         *
         * window.SunmiBridge.printReceipt("TEXT");
         */

        webView.addJavascriptInterface(
                bridge,
                "SunmiBridge"
        );

        /*
         * Old compatibility:
         *
         * window.lee.funAndroid("TEXT");
         */

        webView.addJavascriptInterface(
                bridge,
                "lee"
        );

        // -----------------------------------------------------
        // WEBVIEW CLIENT
        // -----------------------------------------------------

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(view, url);

                        /*
                         * Check whether JavaScript can see
                         * SunmiBridge.
                         */

                        view.evaluateJavascript(
                                "(typeof window.SunmiBridge !== 'undefined')",
                                value -> {

                                    if ("true".equals(value)) {

                                        toast(
                                                "SUNMI WebView Bridge OK"
                                        );

                                    } else {

                                        toast(
                                                "SUNMI WebView Bridge NOT FOUND"
                                        );
                                    }
                                }
                        );
                    }
                }
        );

        // =====================================================
        // CONNECT SUNMI PRINTER
        // =====================================================

        connectPrinter();

        // =====================================================
        // LOAD WEBSITE
        // =====================================================

        webView.loadUrl(
                "https://mathbor.robotispsoft.xyz/"
        );
    }

    // =========================================================
    // CONNECT PRINTER
    // =========================================================

    private void connectPrinter() {

        try {

            boolean bound =
                    InnerPrinterManager
                            .getInstance()
                            .bindService(
                                    this,
                                    printerCallback
                            );

            if (!bound) {

                toast(
                        "SUNMI printer service not available"
                );
            }

        } catch (InnerPrinterException e) {

            toast(
                    "Printer bind error: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void toast(final String message) {

        mainHandler.post(() ->
                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    // =========================================================
    // PRINT RECEIPT
    // =========================================================

    private void printReceipt(final String text) {

        /*
         * Printer service not connected
         */

        if (printerService == null) {

            toast(
                    "SUNMI printer not connected"
            );

            // Try connecting again
            connectPrinter();

            return;
        }

        mainHandler.post(() -> {

            try {

                // -------------------------------------------------
                // PRINTER INITIALIZE
                // -------------------------------------------------

                printerService.printerInit(
                        new InnerResultCallback() {

                            @Override
                            public void onRunResult(
                                    boolean isSuccess
                            ) {
                                // No action
                            }

                            @Override
                            public void onReturnString(
                                    String result
                            ) {
                                // No action
                            }

                            @Override
                            public void onRaiseException(
                                    int code,
                                    String msg
                            ) {

                                toast(
                                        "Printer init error: "
                                                + msg
                                );
                            }

                            @Override
                            public void onPrintResult(
                                    int code,
                                    String msg
                            ) {
                                // No action
                            }
                        }
                );

                // -------------------------------------------------
                // ALIGN LEFT
                // -------------------------------------------------

                printerService.setAlignment(
                        0,
                        null
                );

                // -------------------------------------------------
                // PRINT TEXT
                // -------------------------------------------------

                printerService.printText(
                        (text == null ? "" : text)
                                + "\n",
                        new InnerResultCallback() {

                            @Override
                            public void onRunResult(
                                    boolean isSuccess
                            ) {

                                if (isSuccess) {

                                    toast(
                                            "Receipt sent to printer"
                                    );

                                } else {

                                    toast(
                                            "Print failed"
                                    );
                                }
                            }

                            @Override
                            public void onReturnString(
                                    String result
                            ) {
                                // No action
                            }

                            @Override
                            public void onRaiseException(
                                    int code,
                                    String msg
                            ) {

                                toast(
                                        "Print error: "
                                                + msg
                                );
                            }

                            @Override
                            public void onPrintResult(
                                    int code,
                                    String msg
                            ) {
                                // No action
                            }
                        }
                );

                // -------------------------------------------------
                // FEED PAPER
                // -------------------------------------------------

                printerService.lineWrap(
                        3,
                        null
                );

            } catch (Exception e) {

                toast(
                        "Print error: "
                                + e.getMessage()
                );
            }
        });
    }

    // =========================================================
    // JAVASCRIPT BRIDGE
    // =========================================================

    public class SunmiBridge {

        // -----------------------------------------------------
        // MAIN PRINT METHOD
        // -----------------------------------------------------

        @JavascriptInterface
        public void printReceipt(String text) {

            /*
             * IMPORTANT:
             *
             * MainActivity.this.printReceipt()
             *
             * ব্যবহার করতে হবে।
             *
             * না হলে bridge-এর নিজের method আবার নিজেকেই
             * call করতে পারে।
             */

            MainActivity.this.printReceipt(
                    text == null ? "" : text
            );
        }

        // -----------------------------------------------------
        // printText COMPATIBILITY
        // -----------------------------------------------------

        @JavascriptInterface
        public void printText(String text) {

            MainActivity.this.printReceipt(
                    text == null ? "" : text
            );
        }

        // -----------------------------------------------------
        // OLD lee.funAndroid COMPATIBILITY
        // -----------------------------------------------------

        @JavascriptInterface
        public void funAndroid(String text) {

            MainActivity.this.printReceipt(
                    text == null ? "" : text
            );
        }

        // -----------------------------------------------------
        // GET PRINTER STATUS
        // -----------------------------------------------------

        @JavascriptInterface
        public String getStatus() {

            if (printerService == null) {

                return "disconnected";

            } else {

                return "connected";
            }
        }

        // -----------------------------------------------------
        // CHECK CONNECTION
        // -----------------------------------------------------

        @JavascriptInterface
        public boolean isConnected() {

            return printerService != null;
        }

        // -----------------------------------------------------
        // INITIALIZE PRINTER
        // -----------------------------------------------------

        @JavascriptInterface
        public void initPrinter() {

            if (printerService == null) {

                toast(
                        "SUNMI printer not connected"
                );

                connectPrinter();

                return;
            }

            mainHandler.post(() -> {

                try {

                    printerService.printerInit(
                            new InnerResultCallback() {

                                @Override
                                public void onRunResult(
                                        boolean isSuccess
                                ) {

                                    if (isSuccess) {

                                        toast(
                                                "Printer ready"
                                        );

                                    } else {

                                        toast(
                                                "Printer init failed"
                                        );
                                    }
                                }

                                @Override
                                public void onReturnString(
                                        String result
                                ) {
                                    // No action
                                }

                                @Override
                                public void onRaiseException(
                                        int code,
                                        String msg
                                ) {

                                    toast(
                                            "Printer error: "
                                                    + msg
                                    );
                                }

                                @Override
                                public void onPrintResult(
                                        int code,
                                        String msg
                                ) {
                                    // No action
                                }
                            }
                    );

                } catch (Exception e) {

                    toast(
                            "Printer init error: "
                                    + e.getMessage()
                    );
                }
            });
        }
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

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

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        try {

            InnerPrinterManager
                    .getInstance()
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

            webView.stopLoading();

            webView.loadUrl("about:blank");

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
