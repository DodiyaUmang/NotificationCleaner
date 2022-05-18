package com.heaven.notificationcleaner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationCleaner extends NotificationListenerService {

    private SharedPreferences sharedPreferences, configsPreferences;
    private SharedPreferences.Editor preferencesEditor;
    private NotificationManager notificationManager;
    private List<ResolveInfo> appList;

    public static ArrayList<NotiModel> notiList = new ArrayList<>();

    private static final long TIME_TO_WAIT_QUAEU = 100;
    private static long currentTime;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            PackageManager packageManager = getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            appList = packageManager.queryIntentActivities(mainIntent, 0);
            Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            sharedPreferences = getSharedPreferences(getString(R.string.APPS_NOTICES), MODE_PRIVATE);
            configsPreferences = getSharedPreferences("Configs", MODE_PRIVATE);
            preferencesEditor = sharedPreferences.edit();

            clearOtherNotifications();

            currentTime = System.currentTimeMillis();
        } catch (Exception e) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
//            sendUserNotification();
        } catch (Exception e) {}

        return START_STICKY;
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle extras = sbn.getNotification().extras;

                String title;
                String text = "";
                if (extras.getCharSequence("android.text") != null)
                    text = extras.getCharSequence("android.text").toString();
                String time = String.valueOf(sbn.getPostTime());
                String packageName = sbn.getPackageName();
                Bitmap icon = sbn.getNotification().largeIcon;
                PendingIntent pendingIntent = sbn.getNotification().contentIntent;

                if (System.currentTimeMillis() - currentTime >= TIME_TO_WAIT_QUAEU) {
                    for (ResolveInfo appInfo : appList) {
                        if (appInfo.activityInfo.packageName.equals(packageName)
                                && !packageName.equals(getApplicationContext().getPackageName())
                                && !isDeniedPackage(appInfo.activityInfo.packageName)) {
                            if (extras.getString("android.title") == null)
                                title = ((String) appInfo.loadLabel(getPackageManager()));
                            else
                                title = extras.getString("android.title");


                            Log.d("TAG", "onNotificationPosted: "+title);
                            Log.d("TAG", "onNotificationPosted: "+packageName);
                            Log.d("TAG", "onNotificationPosted: "+text);

                            NotiModel model = new NotiModel();
                            model.setPkg(packageName);
                            model.setText(text);
                            model.setTitle(title);
                            notiList.add(model);

                            Log.d("TAG", "onNotificationPosted: "+notiList);

//                            String notificationInit = (saveToInternalStorage(icon, sbn.getKey()) + "///" + title + "///" + text + "///" + time + "///" + packageName);
//
//                            if (pendingIntent != null)
//                                notificationInit += "///" + pendingIntent.getIntentSender().getCreatorPackage();
//
//                            preferencesEditor.putString(sbn.getKey(), notificationInit);
//                            preferencesEditor.commit();

                            if (configsPreferences.getBoolean("isRequireNotifsControl", true))
                                cancelNotification(sbn.getKey());

                            if (getActiveNotifications().length >= 1 && configsPreferences.getBoolean("isRequireNotifsControl", true))
                                clearOtherNotifications();

//                            if (configsPreferences.getBoolean("isRequireNotifsControl", true))
//                                sendUserNotification();
                        }
                    }

                    currentTime = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
//            Toast.makeText(this, e.toString() + getClass(), Toast.LENGTH_SHORT).show();
        }
    }

//    public void sendUserNotification () {
//        PackageManager packageManager = getApplicationContext().getPackageManager();
//
//        sharedPreferences = getSharedPreferences(getString(R.string.APPS_NOTICES), MODE_PRIVATE);
//        HashMap<String, String> notifications = (HashMap<String, String>) sharedPreferences.getAll();
//
//        int NOTIFY_ID = 1;
//
//        if (notifications.size() > 0) {
//            Intent notificationIntent = null;
//
//            if (checkNotificationEnabled())
//                if (notifications.size() <= 0) {
//                    notificationIntent = new Intent(this, MainMenuScreen.class);
//
//                    notificationIntent.putExtra("MODE", "MODE_NOTIFICATION_CLEANER");
//                } else
//                    notificationIntent = new Intent(this, NotificationRemoteControlActivity.class);
//            else {
//                notificationIntent = new Intent(this, MainMenuScreen.class);
//
//                notificationIntent.putExtra("MODE", "MODE_NOTIFICATION_CLEANER");
//            }
//
//            PendingIntent contentIntent = PendingIntent.getActivity(this,
//                    0, notificationIntent,
//                    PendingIntent.FLAG_CANCEL_CURRENT);
//
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
//
//            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_cleaner_notif);
//            remoteViews.setTextViewText(R.id.wanring_title_num, String.valueOf (notifications.size()));
//            remoteViews.setOnClickPendingIntent(R.id.clear_notifs, contentIntent);
//
//            try {
//                switch (notifications.size()) {
//                    case 1:
//                        ArrayList<Bitmap> imagesFor1 = getIconForApp(notifications, appList, packageManager);
//
//                        if (imagesFor1.get(0) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_1, imagesFor1.get(0));
//
//                        remoteViews.setViewVisibility(R.id.img_app_slot_1, View.VISIBLE);
//                        break;
//                    case 2:
//                        ArrayList<Bitmap> imagesFor2 = getIconForApp(notifications, appList, packageManager);
//
//                        if (imagesFor2.get(0) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_1, imagesFor2.get(0));
//                        if (imagesFor2.get(1) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_2, imagesFor2.get(1));
//
//                        remoteViews.setViewVisibility(R.id.img_app_slot_1, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_2, View.VISIBLE);
//                        break;
//                    case 3:
//                        ArrayList<Bitmap> imagesFor3 = getIconForApp(notifications, appList, packageManager);
//
//                        if (imagesFor3.get(0) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_1, imagesFor3.get(0));
//                        if (imagesFor3.get(1) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_2, imagesFor3.get(1));
//                        if (imagesFor3.get(2) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_3, imagesFor3.get(2));
//
//                        remoteViews.setViewVisibility(R.id.img_app_slot_1, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_2, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_3, View.VISIBLE);
//                        break;
//                    default:
//                        ArrayList<Bitmap> imagesForDefault = getIconForApp(notifications, appList, packageManager);
//
//                        if (imagesForDefault.get(0) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_1, imagesForDefault.get(0));
//                        if (imagesForDefault.get(1) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_2, imagesForDefault.get(1));
//                        if (imagesForDefault.get(2) != null)
//                            remoteViews.setImageViewBitmap(R.id.img_app_slot_3, imagesForDefault.get(2));
//
//                        remoteViews.setViewVisibility(R.id.img_app_slot_1, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_2, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_3, View.VISIBLE);
//                        remoteViews.setViewVisibility(R.id.img_app_slot_4, View.VISIBLE);
//                }
//            } catch (Exception e) {}
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                String CHANNEL_ID = "my_channel_01";// The id of the channel.
//                CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.
//                int importance = NotificationManager.IMPORTANCE_HIGH;
//                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//
//                notificationManager.createNotificationChannel(mChannel);
//
//                notificationBuilder.setChannelId(CHANNEL_ID);
//            }
//
//            notificationBuilder.setContentIntent(contentIntent)
//                    .setDefaults(NotificationCompat.DEFAULT_ALL)
//                    // обязательные настройки
//                    .setSmallIcon(R.drawable.warning)
////                    .setContentText("Просмотрите конфиденциальность") // Текст уведомления
//                    .setShowWhen(false)
//                    .setContent(remoteViews)
////                .setColor (Color.WHITE)
//                    // необязательные настройки
////                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.warning))
//                    .setSubText("")
//                    .setTicker("Ненужные уведомления: " + notifications.size())
//                    .setWhen(System.currentTimeMillis())
////                    .setContentTitle("Ненужные уведомления: " + notifications.size())
//                    .setLights(Color.argb(255, 252, 148, 0), 1500, 1000)
//                    .setDefaults(Notification.FLAG_SHOW_LIGHTS
//                            | Notification.DEFAULT_VIBRATE
//                            | Notification.FLAG_NO_CLEAR
//                            | Notification.FLAG_FOREGROUND_SERVICE)
//                    .setOngoing(true)
//                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                    .setOnlyAlertOnce(true);
//
//            if (notifications.size() < 1) {
//                notificationBuilder.setContentTitle("У вас нет новых уведомлений!");
//                notificationBuilder.setContentText("Конфиденциальность под контролем...");
//                notificationBuilder.setTicker("Ненужные уведомления отсутствуют!");
//            }
//
//            if (configsPreferences.getBoolean("isRequireNotifsControl", true))
//                notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
//            else
//                notificationManager.cancel(NOTIFY_ID);
//
//            if (configsPreferences.getBoolean("isRequireNotifsControl", true))
//                clearOtherNotifications();
//        } else {
//            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
//            notificationManager.cancel(NOTIFY_ID);
//        }
//    }

    private ArrayList<Bitmap> getIconForApp (HashMap<String, String> notifications, List<ResolveInfo> appList, PackageManager packageManager) {
        ArrayList<Bitmap> images = new ArrayList<>();

        for (Map.Entry<String, String> notification : notifications.entrySet()) {

            String[] compressNotif = notification.getValue().split("///");

            String packageName = compressNotif[4];

            for (ResolveInfo appInfo : appList) {
                if (appInfo.activityInfo.packageName.equals(packageName)) {
                    Bitmap icon = loadImageFromStorage(compressNotif[0], notification.getKey());

                    if (icon == null) {
                        icon = drawableToBitmap(appInfo.loadIcon(packageManager));

                        images.add(icon);
                    }
                }
            }
        }

        return images;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification Removed");
    }

    private boolean isDeniedPackage (String packageName) {
        String[] deniedPackages = {"org.telegram.messenger",
                "com.whatsapp",
                "com.facebook.katana",
                "com.viber.voip",
                "com.google.android.gm",
                "com.tencent.mm"};

        for (int i = 0; i < deniedPackages.length; i++)
            if (deniedPackages[i].equals(packageName))
                return true;

        return false;
    }

    private void clearOtherNotifications () {
        try {
            for (int i = 0; i < getActiveNotifications().length; i++) {
                StatusBarNotification notice = getActiveNotifications()[i];

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && !notice.getPackageName().equals(getPackageName())
                        && !isDeniedPackage(notice.getPackageName()))
                    cancelNotification(notice.getKey());
            }
        } catch (Exception e) {}
    }

    private String saveToInternalStorage (Bitmap bitmapImage, String key) throws IOException {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // путь /data/data/yourapp/app_data/APPS_NOTICES
                File directory = cw.getDir(getString(R.string.APPS_NOTICES), Context.MODE_PRIVATE);
                // Создаем APPS_NOTICES
                File myPath = new File(directory, key);

                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(myPath);
                    // Используем метод сжатия BitMap объекта для записи в OutputStream
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fos.close();
                }

        return directory.getAbsolutePath();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
            Bitmap bitmap = null;

        try {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {}
        return bitmap;
    }

    private Bitmap loadImageFromStorage(String path, String key)
    {
        try {
            File f=new File(path, key);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    //check notification access setting is enabled or not
    public boolean checkNotificationEnabled() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();

//        sendUserNotification();
    }
}