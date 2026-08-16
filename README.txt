MATHBOR SUNMI FRESH BUILD

Website:
https://mathbor.robotispsoft.xyz/

This version specifically fixes the previous:
checkDebugDuplicateClasses
kotlin-stdlib 1.8.22 vs kotlin-stdlib-jdk7/jdk8 1.6.21

Fix:
- No AppCompat
- No AndroidX
- Explicitly excludes kotlin-stdlib-jdk7/jdk8
- Uses SUNMI printerlibrary 1.0.24
- Java-only app

Website JS supported inside this APK:

window.SunmiBridge.printReceipt("receipt text");

window.SunmiBridge.printText("receipt text");

window.SunmiBridge.getStatus();

window.SunmiBridge.initPrinter();

Compatibility with your previous code:

window.lee.funAndroid("receipt text");

IMPORTANT:
Chrome itself cannot access the native bridge. The website must be opened inside this APK.

Build with GitHub:
1. Create a GitHub repository.
2. Upload all files/folders from this ZIP.
3. Open Actions.
4. Run "Build Mathbor SUNMI APK".
5. Download artifact:
   mathbor-sunmi-debug-apk
6. Install app-debug.apk on the SUNMI V2s.
