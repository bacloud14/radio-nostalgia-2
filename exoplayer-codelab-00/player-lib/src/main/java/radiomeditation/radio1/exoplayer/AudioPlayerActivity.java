package radiomeditation.radio1.exoplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;

import android.net.http.HttpResponseCache;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.radiomeditation.exoplayer.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Response;

public class AudioPlayerActivity extends Service {
    public static SimpleExoPlayer player;
    public static final String TAG = "AudioPlayerActivity";
    private Uri subtitleUri, videoUriEN, videoUriFR, videoUriAR;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private PlayerNotificationManager playerNotificationManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = this;

        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());

        buildPlayerPlaylist(context, false);

        PlayerNotificationManager.MediaDescriptionAdapter notificationAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public String getCurrentContentTitle(Player player) {
                return samples.get(player.getCurrentWindowIndex()).title;
            }

            @Nullable
            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                Intent intent = new Intent(context, PlayerActivity.class);
                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            @Nullable
            @Override
            public String getCurrentContentText(Player player) {
                return samples.get(player.getCurrentWindowIndex()).description;
            }

            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                return null;
            }
        };

        PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopForeground(true);
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                if (ongoing)
                    startForeground(notificationId, notification);
                else
                    stopForeground(false);
            }
        };


        Intent dialogIntent = new Intent(this, PlayerActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(dialogIntent);
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context, "channelId", R.string.player_activity_name, R.string.player_activity_description, 1, notificationAdapter, notificationListener);


        playerNotificationManager.setPlayer(player);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();
        player.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);
        player.addListener(new ExoPlayer.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                switch(playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        break;
                    case ExoPlayer.STATE_ENDED:
                        player.seekTo(0);
                        break;
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_READY:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                switch (error.type) {
                    case ExoPlaybackException.TYPE_SOURCE:
                        Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
                        break;

                    case ExoPlaybackException.TYPE_RENDERER:
                        Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
                        break;

                    case ExoPlaybackException.TYPE_UNEXPECTED:
                        Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
                        break;
                }
                buildPlayerPlaylist(context, true);
            }
        });
        player.seekTo(0);
        player.setPlayWhenReady(true);//replay from start


    }

    private void buildPlayerPlaylist(Context context, boolean onError) {

        initURLs(onError);
        // Uri uri = Uri.parse(getString(R.string.media_url_dash));
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "radio-nostalgia"));
        ArrayList<MediaSource> mediaSources = new ArrayList<MediaSource>();
        ConcatenatingMediaSource playlist =
                new ConcatenatingMediaSource();

        for (Sample sample : samples) {
            MediaSource OneMediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(sample.url);
            MediaSource[] oneMediaSourceWithSub = new MediaSource[2];
            oneMediaSourceWithSub[0] = OneMediaSource;
            SingleSampleMediaSource oneSubtitleSource = new SingleSampleMediaSource(subtitleUri, dataSourceFactory,
                    Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                    C.TIME_UNSET);
            oneMediaSourceWithSub[1] = oneSubtitleSource;

            mediaSources.add(OneMediaSource);
            playlist = new ConcatenatingMediaSource(playlist, OneMediaSource);
        }

        // player.prepare(concatenatedSource, false, false);
        player.prepare(playlist);
        player.setPlayWhenReady(true);
    }


    private void initURLs(boolean onError) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(new Date());
        Date dv = new Date();
        dv.setDate(dv.getDate() + 1);
        if(onError)
            date = df.format(dv);
//        Date dv2 = new Date();
//        dv2.setDate(dv.getDate() + 2);
//        String date3 = df.format(dv2);
        int i = 0;


        for (String cat: Sample.categories){
            Uri videoUri;

//            boolean pingSuccess = true;
//            try {
//                pingSuccess = NetworkTest.pingTestWeb("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if(!pingSuccess){
//                try {
//                    pingSuccess = NetworkTest.pingTestWeb("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/");
//                    System.out.println("@@@@@@@@@@@@ failed"+String.format(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/%s/%s/mixed.mp3", cat, date2)));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            if(Sample.mixing[i]){
                videoUri = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/%s/%s/mixed.mp3", cat, date));
            }else{
                videoUri = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/playlist/%s/mixed.mp3", cat));
            }

            System.out.println(videoUri);
            samples.add(new Sample(Sample.titles[i], videoUri, Sample.descriptions[i], Sample.mixing[i]));
            i++;
        }

    }


    @Override
    public void onDestroy() {
        playerNotificationManager.setPlayer(null);
        player.release();
        player = null;
        super.onDestroy();
    }
}

