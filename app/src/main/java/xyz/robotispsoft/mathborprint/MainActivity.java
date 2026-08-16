package xyz.robotispsoft.mathborprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
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
            showToast("SUNMI printer connected");
        }

        @Override
        public void onDisconnected() {
            printerService = null;
            showToast("SUNMI printer disconnected");
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
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        // This is the bridge your PHP/website JavaScript will call.
        webView.addJavascriptInterface(new SunmiJsBridge(), "SunmiBridge");

        bindPrinter();

        webView.loadUrl("https://mathbor.robotispsoft.xyz/");
    }

    private void bindPrinter() {
        try {
            boolean ok = InnerPrinterManager.getInstance()
                    .bindService(this, printerCallback);

            if (!ok) {
                showToast("SUNMI printer service not available");
            }
        } catch (InnerPrinterException e) {
            showToast("SUNMI bind error: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        mainHandler.post(() -> Toast.makeText(
                MainActivity.this, message, Toast.LENGTH_SHORT
        ).show());
    }

    public class SunmiJsBridge {

        @JavascriptInterface
        public String getStatus() {
            return printerService != null ? "connected" : "disconnected";
        }

        @JavascriptInterface
        public void printText(final String text) {
            if (printerService == null) {
                showToast("SUNMI printer service not connected");
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

                    printerService.setAlignment(1, null); // center
                    printerService.printText(text + "\n", new InnerResultCallback() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            showToast(isSuccess ? "Print sent" : "Print failed");
                        }
                        @Override public void onReturnString(String result) {}
                        @Override public void onRaiseException(int code, String msg) {
                            showToast("Print error: " + msg);
                        }
                        @Override public void onPrintResult(int code, String msg) {}
                    });
                    printerService.lineWrap(3, null);
                } catch (Exception e) {
                    showToast("Print error: " + e.getMessage());
                }
            });
        }

        @JavascriptInterface
        public void printReceipt(final String receipt) {
            printText(receipt);
        }

        @JavascriptInterface
        public void initPrinter() {
            if (printerService == null) {
                showToast("SUNMI printer service not connected");
                return;
            }
            mainHandler.post(() -> {
                try {
                    printerService.printerInit(new InnerResultCallback() {
                        @Override public void onRunResult(boolean isSuccess) {
                            showToast(isSuccess ? "Printer ready" : "Printer init failed");
                        }
                        @Override public void onReturnString(String result) {}
                        @Override public void onRaiseException(int code, String msg) {
                            showToast("Printer error: " + msg);
                        }
                        @Override public void onPrintResult(int code, String msg) {}
                    });
                } catch (Exception e) {
                    showToast("Printer init error: " + e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("SunmiBridge");
            webView.destroy();
        }

        try {
            InnerPrinterManager.getInstance()
                    .unBindService(this, printerCallback);
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }
}
