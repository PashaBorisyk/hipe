//package com.bori.hipe.Controllers.Services;
//
//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Binder;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationManagerCompat;
//import android.support.v4.app.TaskStackBuilder;
//import android.util.Log;
//import android.view.View;
//
//import com.bori.hipe.controllers.WebSocket.MessageCallbackAdapter;
//import com.bori.hipe.controllers.WebSocket.WebSocketService;
//import com.bori.hipe.Models.ChatMessage;
//import com.bori.hipe.Models.Event;
//import com.bori.hipe.Models.User;
//import com.bori.hipe.HipeApplication;
//import com.bori.hipe.R;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.Timer;
//import java.util.TimerTask;
//
//
//public class AlkosService extends Service {
//
//    private static final String TAG = "ALKOS_SERVICE";
//    private static final String SHARED_IDS_KEY = "SHARED_IDS_KEY";
//
//    public ServiceGenerator serviceGenerator;
//
//    private boolean isRepaired = false;
//    private static int id = 0;
//    private static int resChooser = 0;
//    private HashMap<Integer,WebSocketService> webSocketServiceHashMap;
//    private Timer updateTimer;
//    private SharedPreferences alkosSharedPreferences;
//    private Set<Integer> idSet;
//    private Set<String> idStringSet;
//    private MyBinder binder = new MyBinder();
//
//    protected NotificationManagerCompat notificationManagerCompat;
//    protected Notification notification;
//    protected String contentTitles[];
//    protected String personContentTitle;
//    protected String notificationTicker1;
//    protected String notificationTicker2;
//    protected AlarmManager alarmManager;
//
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.e(TAG, "onCreate: ");
//
//        resourceInit();
//
//        webSocketServiceHashMap = new HashMap<>();
//        alkosSharedPreferences = getSharedPreferences(HipeApplication.ALKOS_SHARED, MODE_PRIVATE);
//        idStringSet = alkosSharedPreferences.getStringSet(SHARED_IDS_KEY, null);
//        idSet = new HashSet<>();
//
//        notificationManagerCompat = NotificationManagerCompat.from(this);
//        serviceGenerator = new ServiceGenerator(onResponseListener);
//
//        IntentFilter i = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(broadcastReceiver,i);
//
//        if (BootReciever.isConnected)
//            repairConnections();
//
//    }
//
//
//    void repairConnections(){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                if (idStringSet != null){
//                    for (String id : idStringSet)
//                        idSet.add(Integer.valueOf(id));
//                }
//                else
//                    idStringSet = new HashSet<>();
//
//                if (idSet.size()!= 0)
//                    for (Integer id : idSet) {
//
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {e.printStackTrace();}
//
//                        registerNewChatId(id);
//
//
//                    }
//
//            }
//        }).start();
//
//        isRepaired = true;
//    }
//
//    void resourceInit(){
//
//        contentTitles = getResources().getStringArray(R.array.notification_content_titles);
//        personContentTitle = getResources().getString(R.string.notification_person_content_title);
//        notificationTicker1 = getResources().getString(R.string.notification_ticker1);
//        notificationTicker2 = getResources().getString(R.string.notification_ticker2);
//
//    }
//
//
//    OnResponseListener onResponseListener = new OnResponseListener() {
//
//        @Override
//        protected void onPreRequest() {
//            Log.e(TAG, "onPreRequest: ");
//        }
//
//        @Override
//        protected void onSimpleResponse(Response response, int id) {
//            super.onSimpleResponse(response, id);
//            Log.e(TAG, "onSimpleResponse: ");
//        }
//
//        /**
//         * Только приватные события мы шарим через промежуток впемени. Остальные только по запросу пользователя
//         * @param response
//         * @param events
//         */
//        @Override
//        protected void onEventListResponse(Response response, List<Event> events) {
//            super.onEventListResponse(response, events);
//            Log.e(TAG, "onEventListResponse: ");
//
//            if (events.size() == 1){
//                showNotification(events.get(0));
//            }
//
//            else {
//                showNotification(events.size());
//            }
//
//        }
//
//        @Override
//        protected void onUserResponse(Response response, List<User> users) {
//            super.onUserResponse(response, users);
//
//            showNotification(2);
//        }
//
//    };
//
//
//    public void showNotification(int eventsCount){
//
//        Log.e(TAG, "showNotification: ");
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        Intent intent = new Intent(this,Act.class);
//
//        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
//        taskStackBuilder.addParentStack(Act.class);
//        taskStackBuilder.addNextIntent(intent);
//        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            notification = builder
//                    .setAutoCancel(true)
//                    .setTicker(notificationTicker1 + eventsCount + notificationTicker2)
//                    .setContentInfo("Content info")
//                    .setContentTitle(contentTitles[resChooser%contentTitles.length])
//                    .setContentText(notificationTicker1 + eventsCount + notificationTicker2)
//                    .setVibrate(new long[]{100, 100, 100, 500})
//                    .setVisibility(View.VISIBLE)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setPriority(Notification.PRIORITY_DEFAULT)
//                    .setCategory(Notification.CATEGORY_MESSAGE)
//                    .setSubText("Hello!! you have been invited to 1000 flats alkoholic")
//                    .setContentIntent(pendingIntent)
//                    .build();
//
//        notificationManagerCompat.notify("my notification",id,notification);
//
//        resChooser++;
//
//    }
//
//
//    public void showNotification(Event event){
//        Log.e(TAG, "showNotification: ");
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        Intent intent = new Intent(this,Act.class);
//
//        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
//        taskStackBuilder.addParentStack(Act.class);
//        taskStackBuilder.addNextIntent(intent);
//        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        notification = builder
//                .setAutoCancel(true)
//                .setTicker(event.creatorNick + " " + personContentTitle)
//                .setContentInfo("Content info")
//                .setContentTitle(event.creatorNick + " " + personContentTitle)
//                .setContentText(event.description)
//                .setVibrate(new long[]{100, 100, 100, 500})
//                .setVisibility(View.VISIBLE)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setPriority(Notification.PRIORITY_DEFAULT)
//                .setCategory(Notification.CATEGORY_MESSAGE)
//                .setContentIntent(pendingIntent)
//                .build();
//
//        notificationManagerCompat.notify("my notification", id, notification);
//
//    }
//
//
//    @Override
//    public MyBinder onBind(Intent intent) {
//        Log.e(TAG, "onBind: ");
//        return binder;
//    }
//
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(TAG, "onStartCommand: flags" + flags + " start id " + startId);
//        if (!isRepaired)
//            repairConnections();
//        return START_STICKY;
//    }
//
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.e(TAG, "onDestroy: ");
//        webSocketServiceHashMap.clear();
//
//        alkosSharedPreferences.edit().putStringSet(SHARED_IDS_KEY, idStringSet).apply();
//        unregisterReceiver(broadcastReceiver);
//
//    }
//
//
//    /**
//     * used to create new web socket connection with server
//     * @param id
//     */
//    public void registerNewChatId(int id){
//
//        if (webSocketServiceHashMap.containsKey(id)){
//            if (!webSocketServiceHashMap.get(id).isConnected())
//                webSocketServiceHashMap.get(id).connect();
//
//            Log.e(TAG, "registerNewChatId: Contained such connection ");
//        }
//
//        else {
//            Log.e(TAG, "registerNewChatId: ");
//
//            WebSocketService webSocketService = new WebSocketService(new MessageCallbackAdapter() {
//                @Override
//                public void onMessage(int groupId, ChatMessage chatMessage) {
//                    Log.e(TAG, "onMessage: id is" + groupId);
//
//                    chatMessage.save();
//
//                }
//            }, id);
//
//            webSocketService.connect();
//            webSocketServiceHashMap.put(id, webSocketService);
//        }
//
//        idStringSet.add(String.valueOf(id));
//
//    }
//
//
//
//    public void unregisterChatId(int id){
//
//        if (webSocketServiceHashMap.containsKey(id)) {
//
//            webSocketServiceHashMap.get(id).disconnect();
//            webSocketServiceHashMap.remove(id);
//
//
//        }
//
//        idStringSet.remove(String.valueOf(id));
//    }
//
//
//    public void checkService(){
//        Log.e(TAG, "checkService: ");
//    }
//
//
//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
//
//                Log.e(TAG, "onReceive: NETWORK STATE CHANGED");
//
//                ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//                if (networkInfo != null && networkInfo.isConnected())
//                    if (!isRepaired)
//                        repairConnections();
//
//            }
//
//        }
//    };
//
//    public class MyBinder extends Binder{
//
//        public AlkosService getService(){
//            return AlkosService.this;
//        }
//
//    }
//
//    private class RequsertsTimer extends TimerTask{
//
//        int eventId;
//
//        public RequsertsTimer(int eventId){
//            this.eventId = eventId;
//        }
//
//        @Override
//        public void run() {}
//
//    }
//
//
//}
