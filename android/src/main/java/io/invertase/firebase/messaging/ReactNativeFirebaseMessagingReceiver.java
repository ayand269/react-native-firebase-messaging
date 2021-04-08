package io.invertase.firebase.messaging;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

import io.invertase.firebase.app.ReactNativeFirebaseApp;
import io.invertase.firebase.common.ReactNativeFirebaseEventEmitter;
import io.invertase.firebase.common.SharedUtils;
import android.app.NotificationManager;
import android.net.Uri;
import android.os.Build;

public class ReactNativeFirebaseMessagingReceiver extends BroadcastReceiver {
  private static final String TAG = "RNFirebaseMsgReceiver";
  static HashMap<String, RemoteMessage> notifications = new HashMap<>();


  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "broadcast received for message");
    if (ReactNativeFirebaseApp.getApplicationContext() == null) {
      ReactNativeFirebaseApp.setApplicationContext(context.getApplicationContext());
    }
    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();

    // Add a RemoteMessage if the message contains a notification payload


    if (remoteMessage.getNotification() != null) {
      Log.d("FirebaseDataNotify", String.valueOf(remoteMessage.getData()));
      notifications.put(remoteMessage.getMessageId(), remoteMessage);
      ReactNativeFirebaseMessagingStoreHelper.getInstance().getMessagingStore().storeFirebaseMessage(remoteMessage);
    }
    if (remoteMessage.getNotification() != null) {
      if (remoteMessage.getData().get("type").equals("call")) {
        Intent startIntent = new Intent(context, RingtonePlayingService.class);
        context.startService(startIntent);
      }
    }

    //  |-> ---------------------
    //      App in Foreground
    //   ------------------------
//    if (SharedUtils.isAppInForeground(context)) {
//      emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.remoteMessageToEvent(remoteMessage, false));
//      return;
//    }

    if (SharedUtils.isAppInForeground(context)) {
      if (!remoteMessage.getData().get("type").equals("reopen") && !remoteMessage.getData().get("type").equals("notifiction_ck")) {
        emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.remoteMessageToEvent(remoteMessage, false, context.getApplicationContext()));
      }
      return;
    }


    //  |-> ---------------------
    //    App in Background/Quit
    //   ------------------------

    try {
      Intent backgroundIntent = new Intent(context, ReactNativeFirebaseMessagingHeadlessService.class);
      backgroundIntent.putExtra("message", remoteMessage);
//      ComponentName name = context.startService(backgroundIntent);
//      if (name != null) {
//        HeadlessJsTaskService.acquireWakeLockNow(context);
//      }

      if (!remoteMessage.getData().get("type").equals("reopen") && !remoteMessage.getData().get("type").equals("notifiction_ck")){
        ComponentName name = context.startService(backgroundIntent);
        if (name != null) {
          HeadlessJsTaskService.acquireWakeLockNow(context);
        }
      }
    } catch (IllegalStateException ex) {
      // By default, data only messages are "default" priority and cannot trigger Headless tasks
      Log.e(
        TAG,
        "Background messages only work if the message priority is set to 'high'",
        ex
      );
    }
  }
  public void onStopData(Context context) {
    Intent stopIntent = new Intent(context, RingtonePlayingService.class);
    context.stopService(stopIntent);
  }
  public void onBol(Context context) {
//    Toast.makeText(context, "onBol", Toast.LENGTH_SHORT).show();

    NotificationManager notificationManager =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
      && !notificationManager.isNotificationPolicyAccessGranted()) {

      Intent intent = new Intent(
        android.provider.Settings
          .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
  }
}
