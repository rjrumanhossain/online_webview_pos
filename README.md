# Mathbor SUNMI WebView Print App

This Android project opens:

https://mathbor.robotispsoft.xyz/

inside a WebView and exposes a native JavaScript bridge named `SunmiBridge`.

## What the website calls

```javascript
SunmiBridge.printReceipt("YOUR RECEIPT TEXT");
```

or:

```javascript
SunmiBridge.printText("YOUR RECEIPT TEXT");
```

Status:

```javascript
SunmiBridge.getStatus();
```

Printer initialization:

```javascript
SunmiBridge.initPrinter();
```

## PHP receipt page example

Do NOT generate a PDF for this route. Return a normal HTML page and call the bridge:

```php
<?php
$invoice = htmlspecialchars($_GET['token1'] ?? 'INV-1001', ENT_QUOTES, 'UTF-8');
?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Receipt</title>
<style>
body { font-family: monospace; width: 58mm; margin: 0 auto; }
pre { white-space: pre-wrap; font-size: 12px; }
</style>
</head>
<body>
<pre id="receipt">DEVNIOX
--------------------------------
Invoice: <?= $invoice ?>

--------------------------------
TOTAL                     600
--------------------------------
        Thank You!
</pre>

<script>
function printSunmiReceipt() {
    const text = document.getElementById('receipt').innerText;

    if (window.SunmiBridge && typeof window.SunmiBridge.printReceipt === 'function') {
        window.SunmiBridge.printReceipt(text);
    } else {
        alert('SUNMI Print App is not running');
    }
}

// Optional: auto print when this receipt page opens.
// Remove this line if you want the user to press a button.
window.addEventListener('load', function () {
    setTimeout(printSunmiReceipt, 300);
});
</script>
</body>
</html>
```

## Important

Chrome itself cannot directly call a native SUNMI Android JavaScript interface. The `SunmiBridge` object exists only inside this WebView app.

SUNMI V2s/V2s Plus require the `queries` declaration in the manifest for the printer service. This project already includes it.

## Build

Open this folder in Android Studio and build the debug APK.

The project uses:

`com.sunmi:printerlibrary:1.0.18`

Package/application id:

`xyz.robotispsoft.mathborprint`
