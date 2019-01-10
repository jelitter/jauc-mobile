package cit.jauc.adapter;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import cit.jauc.lib.HttpHandler;

public class JAUCFirebaseMessageService extends FirebaseMessagingService {

    public JAUCFirebaseMessageService() {}

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("INFO","From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload, else *nothing*
        if (remoteMessage.getNotification() != null) {
            Log.d("INFO","Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    public void sendTokenToFirebase(String token){

    }
}
