/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package radiomeditation.radio1.exoplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.radiomeditation.exoplayer.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.Service.*;
/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity implements ShareDialog.ShareDialogListener {

    private static final int PLAYBACK_CHANNEL_ID = 54321;
    private static final int NOTIFICATION_ID = 654321;
    final String language = Locale.getDefault().getLanguage();
    private PlayerView playerView;
    private TextView textViewURL;
    private Button submit;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private Uri subtitleUri, videoUriEN, videoUriFR, videoUriAR;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;

    private static HashMap<String, String> getQueryMap(String queryString, String charsetName) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<String, String>();

        String[] fields = queryString.split("&");

        for (String field : fields) {
            String[] pair = field.split("=");
            if (pair.length == 2) {
                String key = pair[0];
                String value = URLDecoder.decode(pair[1], charsetName).replace('+', ' ');
                map.put(key, value);
            }
        }

        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        WebView webView = findViewById(R.id.webView);
        // Configure the webview so that the game will load
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // Load in the game's HTML file
        webView.loadUrl("file:///android_asset/index.html");
        playerView = findViewById(R.id.video_view);
        textViewURL = findViewById(R.id.textView_URL);
        submit = findViewById(R.id.button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSubmitDialog();
            }
        });
        final Context context = this;
//        Context context,
//        String channelId,
//        @StringRes int channelName,
//        @StringRes int channelDescription,
//        int notificationId,
//        MediaDescriptionAdapter mediaDescriptionAdapter

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context, "channelId", android.R.string.cut, android.R.string.cut, new PlayerNotificationManager.MediaDescriptionAdapter() {
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
                }
                );

    }

    public void openSubmitDialog(){
        ShareDialog shareDialog = new ShareDialog();
        shareDialog.show(getSupportFragmentManager(), "share dialog");
    }



    private void initializePlayer() throws IOException {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /*
        URL url = new URL("https://youtube.com/get_video_info?video_id=q-3qus2pvJM");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept-Language", language);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null ) builder.append(line);


            HashMap<String, String> video = getQueryMap(builder.toString(), "UTF-8");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@"+video);

            //if(video.indexOf("dashmpd") == -1){
            //    System.out.println("@@@@@@@@@@@@@ fuck");
            //}
        } finally {
            urlConnection.disconnect();
        }
         */

        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        }

        // player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);

        initURLs();
        // Uri uri = Uri.parse(getString(R.string.media_url_dash));
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "radio-meditation"));
        ArrayList<MediaSource> mediaSources = new ArrayList<MediaSource>();
        ConcatenatingMediaSource playlist =
                new ConcatenatingMediaSource();

        for (Sample sample: samples){
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

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        // player.prepare(concatenatedSource, false, false);
        player.prepare(playlist, false, false);
    }

    private void initURLs() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(new Date());
        int i = 0;
        for (String cat: Sample.categories){
            Uri videoUri = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/%s/%s/mixed.mp3", cat, date));
            System.out.println(videoUri);
            samples.add(new Sample(Sample.titles[i], videoUri, Sample.descriptions[i]));
            i++;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "radio-meditation");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Util.SDK_INT >= 24) {
            try {
                initializePlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            try {
                initializePlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }


    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    @Override
    public void applyTexts(String shareURL) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bacloud14@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT   , shareURL);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            // Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }
}
