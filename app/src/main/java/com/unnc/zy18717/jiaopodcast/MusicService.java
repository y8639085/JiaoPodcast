package com.unnc.zy18717.jiaopodcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private StreamingPlayer player;
    private ArrayList<String> podList;
    private int index = 0;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "com.unnc.zy18717.jiaopodcast";

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {

        public boolean isPlaying() {
            return player.isPlaying();
        }

        // play or pause
        public void play() {
            if (player.isPlaying())
                player.pause();
            else
                player.play();
        }

        // next track
        public void next() {
            player.pause();
            if (index == podList.size() - 1) {
                Toast.makeText(MusicService.this, "Back to the first", Toast.LENGTH_SHORT).show();
                index = 0;
                player.load(podList.get(index), MusicService.this);
            }
            else {
                player.load(podList.get(++index), MusicService.this);
                Toast.makeText(MusicService.this, "Next track", Toast.LENGTH_SHORT).show();
            }
        }

        // previous track
        public void previous() {
            player.pause();
            if (index == 0) {
                Toast.makeText(MusicService.this, "Back to the last", Toast.LENGTH_SHORT).show();
                index = podList.size() - 1;
                player.load(podList.get(index), MusicService.this);
            } else {
                player.load(podList.get(--index), MusicService.this);
                Toast.makeText(MusicService.this, "Previous track", Toast.LENGTH_SHORT).show();
            }
        }

        public void stop() {
            player.pause();
            player.load(podList.get(index), MusicService.this);
            player.pause();
        }

        // get duration of current song
        public int getDuration() {
            return player.getDuration();
        }

        // get current progress
        public int getCurrentPosition() {
            return player.getCurrentPosition();
        }

        // set progress
        public void seekTo(int mesc) {
            player.seekTo(mesc);
        }

        // add a song from website
        public void addWebSong(String url) {
            player.pause();
            podList.add(index + 1, url);
            player.load(podList.get(++index), MusicService.this);
        }

        public String getSongName() {
            return podList.get(index);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (player == null) {
            player = new StreamingPlayer();
            podList = new ArrayList<>();
            podList.add("https://upload.eeo.com.cn/2013/1016/1381893703264.mp3");
            podList.add("http://music.163.com/song/media/outer/url?id=17241116.mp3");

            player.load(podList.get(index), MusicService.this);
            player.pause();
        } else {
            if (player.isPlaying()) {
                player.pause();
            }else {
                player.play();
            }
        }

        createNotificationChannel();

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Jiao Podcast")
                .setContentText("Service on")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "sequence Name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp = player.getMediaPlayer();
        mp.stop();
        if (index == (podList.size() - 1)) {
            Toast.makeText(getApplicationContext(), "Back to the first", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(1000);
                index = 0;
                player.load(podList.get(index), MusicService.this);
            } catch (java.lang.InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            index++;
            player.load(podList.get(index), MusicService.this);
        }
    }
}