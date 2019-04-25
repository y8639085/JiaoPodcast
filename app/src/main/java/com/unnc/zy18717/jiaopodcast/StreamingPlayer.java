package com.unnc.zy18717.jiaopodcast;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;

public class StreamingPlayer{

    protected MediaPlayer mediaPlayer;
    protected StreamingPlayerState state;
    protected String filePath;

    public enum StreamingPlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public StreamingPlayer() {
        this.state = StreamingPlayerState.STOPPED;
    }

    public StreamingPlayerState getState() {
        return this.state;
    }

    /*path example: "https://upload.eeo.com.cn/2013/1016/1381893703264.mp3"*/
    public void load(String filePath, MediaPlayer.OnCompletionListener listener) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("StreamingPlayer", e.toString());
            e.printStackTrace();
            this.state = StreamingPlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("StreamingPlayer", e.toString());
            e.printStackTrace();
            this.state = StreamingPlayerState.ERROR;
            return;
        }

        this.state = StreamingPlayerState.PLAYING;
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(listener);
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getProgress() {
        if(mediaPlayer!=null) {
            if(this.state == StreamingPlayerState.PAUSED || this.state == StreamingPlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void play() {
        if(this.state == StreamingPlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = StreamingPlayerState.PLAYING;
        }
    }

    public void pause() {
        if (this.state == StreamingPlayerState.PLAYING) {
            mediaPlayer.pause();
            state = StreamingPlayerState.PAUSED;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            state = StreamingPlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}