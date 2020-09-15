package com.example.music;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.music.Model.SongModel;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.music.Notification.CHANNEL_ID;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String UPDATE_SONG_DATA = "songControllerData";
    public static final String UPDATE_PROGRESS_DATA = "updateProgressData";

    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_STOP = "ACTION_STOP";

    private MediaPlayer mediaPlayer;

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private static final int NOTIFICATION_ID = 101;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private AudioManager audioManager;

    private ArrayList<SongModel> songs;
    private int audioIndex = -1;
    private SongModel activeSong;

    private int resumePosition;

    public static PlaybackStatus status = PlaybackStatus.PAUSED;
    public static boolean firstTime = true;
    private int SongDuration;
    private String SongDurationInMinutes;

    private final IBinder iBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        registerBecomingNoisyReceiver();
        registerPlayNewAudio();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentSeekBarPosition = mediaPlayer.getCurrentPosition() / 1000;
                    String currentPlayedDuration = currentDurationInMinutes(currentSeekBarPosition);
                    Intent broadcastCurrentDuration = new Intent(UPDATE_PROGRESS_DATA);
                    broadcastCurrentDuration.putExtra("currentPosition", currentSeekBarPosition);
                    broadcastCurrentDuration.putExtra("currentDuration", currentPlayedDuration);
                    sendBroadcast(broadcastCurrentDuration);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            loadPlayerSource();
            if (!requestAudioFocus()) {
                stopSelf();
            }
            preparePlayer();
        }
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadPlayerSource() {
        try {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            songs = storage.loadAudioList();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < songs.size()) {
                activeSong = songs.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void preparePlayer() {
        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!firstTime) {
            playMedia();
        }
        SongDuration = mediaPlayer.getDuration() / 1000;
        SongDurationInMinutes = currentDurationInMinutes(SongDuration);
        updateSongData();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(activeSong.getSongUrl());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void seekTo(int position) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(position);
        } else {
            resumePosition = position;
        }
    }

    private void skipToNext() {
        if (audioIndex == songs.size() - 1) {
            audioIndex = 0;
            activeSong = songs.get(audioIndex);
        } else {
            activeSong = songs.get(++audioIndex);
        }
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            audioIndex = songs.size() - 1;
            activeSong = songs.get(audioIndex);
        } else {
            activeSong = songs.get(--audioIndex);
        }
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return;
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
                updateSongData();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                updateSongData();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                updateSongData();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                updateSongData();
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                updateSongData();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                seekTo((int) position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_music_note_24);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeSong.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getTitle())
                .build());
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
            status = PlaybackStatus.PLAYING;
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
            status = PlaybackStatus.PAUSED;
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_music_note_24);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeSong.getArtist())
                .setContentTitle(activeSong.getTitle())
                .setContentInfo(activeSong.getAlbum())
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
            updateSongData();
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < songs.size()) {
                activeSong = songs.get(audioIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            mediaPlayer.reset();
            firstTime = false;
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            updateSongData();
        }
    };

    private void registerPlayNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    public void updatePlaylist() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        songs = storage.loadAudioList();
        audioIndex = storage.loadAudioIndex();
        if (audioIndex != -1 && audioIndex < songs.size()) {
            activeSong = songs.get(audioIndex);
        } else {
            stopSelf();
        }
        stopMedia();
        mediaPlayer.reset();
        firstTime = false;
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        updateSongData();
    }

    public void seekToSong(int position) {
        transportControls.seekTo((long) position * 1000);
    }

    public void previousSong() {
        transportControls.skipToPrevious();
    }

    public PlaybackStatus playPause() {
        if (status == PlaybackStatus.PLAYING) {
            transportControls.pause();
        } else if (status == PlaybackStatus.PAUSED) {
            transportControls.play();
            firstTime = false;
        }
        return status;
    }

    public void nextSong() {
        transportControls.skipToNext();
    }

    public void updateSongData() {
        Intent broadcastIntent = new Intent(UPDATE_SONG_DATA);
        broadcastIntent.putExtra("activeSong", activeSong);
        broadcastIntent.putExtra("duration", SongDuration);
        broadcastIntent.putExtra("totalTime", SongDurationInMinutes);
        broadcastIntent.putExtra("status", status);
        sendBroadcast(broadcastIntent);
    }

    private String currentDurationInMinutes(int currentSeekBarPosition) {
        String totalNew;
        String totalOut;
        String seconds = String.valueOf(currentSeekBarPosition % 60);
        String minutes = String.valueOf(currentSeekBarPosition / 60);
        totalNew = minutes + ":0" + seconds;
        totalOut = minutes + ":" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}
