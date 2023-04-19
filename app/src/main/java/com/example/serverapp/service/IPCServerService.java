package com.example.serverapp.service;

import static com.example.serverapp.utils.Constants.DATA;
import static com.example.serverapp.utils.Constants.GOT_IT;
import static com.example.serverapp.utils.Constants.PACKAGE_NAME;
import static com.example.serverapp.utils.Constants.PID;
import static com.example.serverapp.utils.Constants.USERNAME;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.serverapp.model.Client;
import com.example.serverapp.model.RecentClient;

public class IPCServerService extends Service {

    private final IncomingHandler incomingHandler = new IncomingHandler();
    private final LocaleMessageHandler localeHandler = new LocaleMessageHandler();

    // Messenger IPC - Messenger object contains binder to send to client
    private final Messenger mMessenger = new Messenger(incomingHandler);
    public Messenger mLocalMessenger = new Messenger(localeHandler);
    private Messenger incomingHandlerMessenger;
    private Messenger localeHandlerMessenger;

    // Messenger IPC - Message Handler
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            System.out.println("###   IncomingHandler.handleMessage called for: " + msg.getData().getString(DATA));

            incomingHandlerMessenger = msg.replyTo;

            String clientData = msg.getData().getString(DATA);

            // Get message from client. Save recent connected client info.
            Bundle receivedBundle = msg.getData();
            RecentClient.client = new Client(
                    receivedBundle.getString(PACKAGE_NAME),
                    String.valueOf(receivedBundle.getInt(PID)),
                    receivedBundle.getString(DATA),
                    "Messenger"
            );

            if (!clientData.equals(GOT_IT)){
                replyToRemoteClient(GOT_IT);
            }

            replyToLocaleClient(clientData);

        }
    }

    public class LocaleMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            System.out.println("###   LocaleMessageHandler.handleMessage called for: " + msg.getData().getString(DATA));


            localeHandlerMessenger = msg.replyTo;

            if (!"HELLO".equals(msg.getData().getString(DATA))) {
                replyToRemoteClient(msg.getData().getString(DATA));
            }
        }
    }

    private void replyToRemoteClient(String str) {

        System.out.println("###   IPCServerService.replyToRemoteClient called for: " + str);

        if (incomingHandlerMessenger == null) {
            Toast.makeText(IPCServerService.this, "Client not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send message to the client. The message contains server info
        Message message = Message.obtain(incomingHandler, 0);
        Bundle bundle = new Bundle();
        bundle.putInt(PID, Process.myPid());
        bundle.putString(USERNAME, "Test Username");
        bundle.putString(DATA, str);
        message.setData(bundle);

        try {
            incomingHandlerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void replyToLocaleClient(String str) {
        System.out.println("###   IPCServerService.replyToLocaleClient called for: " + str);

        if (localeHandlerMessenger == null) {
            Toast.makeText(IPCServerService.this, "Client not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send message to the client. The message contains server info
        Message message = Message.obtain(localeHandler, 0);
        Bundle bundle = new Bundle();
        bundle.putInt(PID, Process.myPid());
        bundle.putString(USERNAME, "Test Username");
        bundle.putString(DATA, str);
        message.setData(bundle);

        try {
            localeHandlerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Choose which binder we need to return based on the type of IPC the client makes
        if (intent.getAction().equals("messengerexample")) {
            return mMessenger.getBinder();
        } else if (intent.getAction().equals("localmessage")) {
            return mLocalMessenger.getBinder();
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        RecentClient.client = null;
        return super.onUnbind(intent);
    }
}
