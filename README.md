# GetPhoneNumberLog
Get Log of Phone number provide in input field

It requires two permission:
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.INTERNET" />
 
We also use volley to send data to server.
 
We put volly in:
GetPhoneNumberLog/app/build.gradle

To post data to Server:
  Check postUsingVolley function in MainAcitivity.java
  
  change https://e3db6ee9.ngrok.io/zahir/android/php/log.php to YOUR URL
