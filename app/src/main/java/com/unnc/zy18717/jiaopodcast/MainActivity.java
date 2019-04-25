package com.unnc.zy18717.jiaopodcast;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MyConnection conn;
    private MusicService.MyBinder myBinder;
    private DatabaseAdapter dbAdapter;
    private ImageButton playBtn;
    private ImageButton nextBtn;
    private ImageButton previousBtn;
    private ImageButton stopBtn;
    private TextView currentText;
    private TextView endText;
    private SeekBar seekBar;
    private TextView tv;
    private EditText comment;
    private EditText urlText;
    private int currentPosition;
    private static final int UPDATE_PROGRESS = 0;

    // update seekbar
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    updateProgress();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = (ImageButton) findViewById(R.id.play);
        playBtn.setImageResource(android.R.drawable.ic_media_play);
        nextBtn = (ImageButton) findViewById(R.id.next);
        nextBtn.setImageResource(android.R.drawable.ic_media_ff);
        previousBtn = (ImageButton) findViewById(R.id.previous);
        previousBtn.setImageResource(android.R.drawable.ic_media_rew);
        stopBtn = (ImageButton) findViewById(R.id.stop);
        stopBtn.setImageResource(android.R.drawable.ic_media_next);
        currentText = (TextView) findViewById(R.id.currentTime);
        tv = (TextView)findViewById(R.id.commentView);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        comment = (EditText) findViewById(R.id.text);
        endText = (TextView) findViewById(R.id.endTime);
        urlText = (EditText) findViewById(R.id.url);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        Intent intent = new Intent(this, MusicService.class);
        conn = new MyConnection();
        // open service
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // change progress
                if (fromUser){
                    myBinder.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();

    }

    private class MyConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName cn, IBinder binder) {
            // get binder
            myBinder = (MusicService.MyBinder) binder;
            // update text
            updatePlayText();
            // update seekbar
            seekBar.setMax(myBinder.getDuration());
            seekBar.setProgress(myBinder.getCurrentPosition());
            queryDBTextView();
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myBinder != null){
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    private void updateProgress() {
        currentPosition = myBinder.getCurrentPosition();
        seekBar.setProgress(currentPosition);
        currentText.setText(timeParse(currentPosition));
        endText.setText(timeParse(myBinder.getDuration()));
        seekBar.setMax(myBinder.getDuration());
        // update seekbar every 1s
        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
    }

    // update text
    public void updatePlayText() {
        if (myBinder.isPlaying())
            playBtn.setImageResource(android.R.drawable.ic_media_pause);
        else
            playBtn.setImageResource(android.R.drawable.ic_media_play);

        handler.sendEmptyMessage(UPDATE_PROGRESS);
        endText.setText(timeParse(myBinder.getDuration()));
    }

    public void play(View view) {
        myBinder.play();
        updatePlayText();
    }

    public void next(View view) {
        myBinder.next();
        seekBar.setMax(myBinder.getDuration());
        updatePlayText();
        queryDBTextView();
    }

    public void previous (View view) {
        myBinder.previous();
        seekBar.setMax(myBinder.getDuration());
        updatePlayText();
        queryDBTextView();
    }

    public void stop (View view) {
        myBinder.stop();
        updatePlayText();
    }

    public void add(View view) {
        if (comment.getText().toString().length() == 0)
            Toast.makeText(MainActivity.this, "Input at least one character", Toast.LENGTH_SHORT).show();
        else {
            dbAdapter.addComment(myBinder.getSongName(), comment.getText().toString());
            Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            comment.setText("");
        }
        queryDBTextView();
    }

    public void addWebSong(View view) {
        if(urlText.getText().toString().length() == 0) {
            Toast.makeText(MainActivity.this, "Please enter a valid url", Toast.LENGTH_SHORT).show();
        } else {
            myBinder.addWebSong(urlText.getText().toString());
            seekBar.setMax(myBinder.getDuration());
            updatePlayText();
            queryDBTextView();
        }
    }

    public void queryDBTextView() {
        StringBuilder sb = new StringBuilder();
        int id = 1;

        Cursor c = dbAdapter.db.query("myList", new String[] {"comment"}, dbAdapter.SONG_NAME + "=?", new String[] {String.valueOf(myBinder.getSongName())}, null, null, null);

        if(c.moveToFirst()) {
            do {
                String comment = c.getString(0);

                sb.append(""+id+ ": " + comment);
                sb.append("\n");
                id++;
                Log.d("ae3mdp", id + " " + comment);
            } while(c.moveToNext());
        }
        tv.setText(sb);
    }

    public static String timeParse(long duration) {
        String time = "" ;
        long minute = duration / 60000 ;
        long seconds = duration % 60000 ;
        long second = Math.round((float)seconds/1000) ;
        if( minute < 10 ) {
            time += "0" ;
        }
        time += minute+":" ;
        if( second < 10 ) {
            time += "0" ;
        }
        time += second ;
        return time ;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }
}