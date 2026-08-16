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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final InnerPrinterCallback printerCallback = new InnerPrinterCallback() {
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

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        /*
         * Website JavaScript:
         *
         * window.SunmiBridge.printReceipt("receipt text");
         *
         * We also expose "lee" for compatibility with your
         * earlier JavaScript:
         *
         * window.lee.funAndroid("receipt text");
         */
        SunmiBridge bridge = new SunmiBridge();
        webView.addJavascriptInterface(bridge, "SunmiBridge");
        webView.addJavascriptInterface(bridge, "lee");

        try {
            boolean bound = InnerPrinterManager.getInstance()
                    .bindService(this, printerCallback);

            if (!bound) {
                toast("SUNMI printer service not available");
            }
        } catch (InnerPrinterException e) {
            toast("Printer bind error: " + e.getMessage());
        }

        webView.loadUrl("https://mathbor.robotispsoft.xyz/");
    }

    private void toast(final String message) {
        mainHandler.post(() ->
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private void printReceipt(final String text) {
        if (printerService == null) {
            toast("SUNMI printer not connected");
            return;
        }

        mainHandler.post(() -> {
            try {
                printerService.printerInit(new InnerResultCallback() {
                    @Override public void onRunResult(boolean isSuccess) {}
                    @Override public void onReturnString(String result) {}
                    @Override public void onRaiseException(int code, String msg) {}
                    @Override public void onPrintResult(int code, String msg) {}
                });

                printerService.setAlignment(0, null);

                printerService.printText(
                        text + "\n",
                        new InnerResultCallback() {
                            @Override
                            public void onRunResult(boolean isSuccess) {
                                toast(isSuccess ? "Receipt sent to printer" : "Print failed");
                            }

                            @Override public void onReturnString(String result) {}

                            @Override
                            public void onRaiseException(int code, String msg) {
                                toast("Print error: " + msg);
                            }

                            @Override public void onPrintResult(int code, String msg) {}
                        }
                );

                printerService.lineWrap(3, null);

            } catch (Exception e) {
                toast("Print error: " + e.getMessage());
            }
        });
    }

    public class SunmiBridge {

        @JavascriptInterface
        public void printReceipt(String text) {
            printReceipt(text == null ? "" : text);
        }

        @JavascriptInterface
        public void printText(String text) {
            printReceipt(text == null ? "" : text);
        }

        /*
         * Compatibility with the code you already tested:
         * window.lee.funAndroid(receipt)
         */
        @JavascriptInterface
        public void funAndroid(String text) {
            printReceipt(text == null ? "" : text);
        }

        @JavascriptInterface
        public String getStatus() {
            return printerService == null ? "disconnected" : "connected";
        }

        @JavascriptInterface
        public void initPrinter() {
            if (printerService == null) {
                toast("SUNMI printer not connected");
                return;
            }

            mainHandler.post(() -> {
                try {
                    printerService.printerInit(new InnerResultCallback() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            toast(isSuccess ? "Printer ready" : "Printer init failed");
                        }

                        @Override public void onReturnString(String result) {}

                        @Override
                        public void onRaiseException(int code, String msg) {
                            toast("Printer error: " + msg);
                        }

                        @Override public void onPrintResult(int code, String msg) {}
                    });
                } catch (Exception e) {
                    toast("Printer init error: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            InnerPrinterManager.getInstance()
                    .unBindService(this, printerCallback);
        } catch (Exception ignored) {
        }

        if (webView != null) {
            webView.removeJavascriptInterface("SunmiBridge");
            webView.removeJavascriptInterface("lee");
            webView.destroy();
        }

        super.onDestroy();
    }
}
