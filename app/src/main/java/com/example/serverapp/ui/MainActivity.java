package com.example.serverapp.ui;

import static com.example.serverapp.utils.Constants.DATA;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.serverapp.databinding.ActivityMainBinding;
import com.example.serverapp.service.IPCServerService;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private ActivityMainBinding binding;

    public Messenger serverMessenger = null;   // Messenger on the localeServer
    private Messenger mMessenger = null;   // Messenger on this class
    public boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSend.setOnClickListener(v -> {
            String messageToSend = binding.etMessage.getText().toString();
            sendMessageToServer(messageToSend);
        });

        doBindService();
    }

    private void sendMessageToServer(String str) {
        if (!isBound || serverMessenger == null) {
            Toast.makeText(this, "Service not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        Message message = Message.obtain(handler);
        Bundle bundle = new Bundle();
        bundle.putString(DATA, str);
        message.setData(bundle);
        message.replyTo = mMessenger; // we offer our Messenger object for communication to be two-way
        try {
            serverMessenger.send(message);
            binding.etMessage.setText("");
        } catch (RemoteException e) {
            e.printStackTrace();
        }/* finally {
            message.recycle(); // not sure if this is correct
        }*/
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    // Handle messages from the locale service
    Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            String serverData = msg.getData().getString(DATA);

            binding.txtData.append("\n" + serverData);
//            binding.etMessage.setText("");
        }
    };

    private void doBindService() {
        mMessenger = new Messenger(handler);
        Intent intent = new Intent(this, IPCServerService.class);
        intent.setAction("localmessage");
        bindService(intent, this, BIND_AUTO_CREATE);
        isBound = true;
    }

    private void doUnbindService() {
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serverMessenger = new Messenger(service);
        isBound = true;
        sendMessageToServer("HELLO");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBound = false;
    }
}