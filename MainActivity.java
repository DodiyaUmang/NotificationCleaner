package com.heaven.notificationcleaner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.heaven.notificationcleaner.NotificationCleaner.notiList;

public class MainActivity extends AppCompatActivity {
    RecyclerView rv_Noti;
    NotiAdapter notiAdapter;

    Handler permissionCheckingHandler;
    Runnable waitingRunnable;
    View view;
    private ImageView cancel, settings, active;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv_Noti = (RecyclerView) findViewById(R.id.rv_Noti);

        startService(new Intent(getApplicationContext(), NotificationCleaner.class));

        if (!checkNotificationEnabled()){
            usageAccessSettingsPage();
        }
        else{

            notiAdapter = new NotiAdapter(notiList,this);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rv_Noti.setLayoutManager(layoutManager);
            rv_Noti.setAdapter(notiAdapter);
        }

    }

    private void usageAccessSettingsPage() {
        Toast.makeText(getApplicationContext(),
                "You should access special permission before using this app!",
                Toast.LENGTH_LONG).show();

        waitingRunnable = new Runnable() {
            @Override
            public void run() {
                redirectToSecureSettings();

                while (!checkNotificationEnabled()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    if (checkNotificationEnabled()) {
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        break;
//                    }
                }
            }
        };

        permissionCheckingHandler = new Handler();
        permissionCheckingHandler.postDelayed(waitingRunnable, 2000);

    }

    private void redirectToSecureSettings() {
        Toast.makeText(getApplicationContext(), "You should allow this app to use notification control before using",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(new Intent (getApplicationContext(), MainActivity.class));

        startActivity(intent);
    }

    private boolean checkNotificationEnabled() {
        try{
            if(Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                return true;
            } else {
                return false;
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}