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
package com.example.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    final String language = Locale.getDefault().getLanguage();
    private Uri subtitleUri, videoUriEN, videoUriFR, videoUriAR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.video_view);
    }

    private SimpleExoPlayer player;

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

        // Uri uri = Uri.parse(getString(R.string.media_url_dash));
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "exo-demo"));

        MediaSource mediaSourceEN = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUriEN);
        MediaSource mediaSourceFR = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUriFR);
        MediaSource mediaSourceAR = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUriAR);

        MediaSource[] mediaSourcesEN = new MediaSource[2]; //The Size must change depending on the Uris
        mediaSourcesEN[0] = mediaSourceEN; // uri
        SingleSampleMediaSource subtitleSourceEN = new SingleSampleMediaSource(subtitleUri, dataSourceFactory,
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                C.TIME_UNSET);
        mediaSourcesEN[1] = subtitleSourceEN;
        //mediaSourcesEN = new MergingMediaSource(mediaSourcesEN, subtitleSourceEN);
        //////////////////////////////////////////////////////////
        MediaSource[] mediaSourcesFR = new MediaSource[2]; //The Size must change depending on the Uris
        mediaSourcesFR[0] = mediaSourceFR; // uri
        SingleSampleMediaSource subtitleSourceFR = new SingleSampleMediaSource(subtitleUri, dataSourceFactory,
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                C.TIME_UNSET);
        mediaSourcesFR[1] = subtitleSourceFR;
        //mediaSourceFR = new MergingMediaSource(mediaSourcesFR);
        //////////////////////////////////////////////////////////
        MediaSource[] mediaSourcesAR = new MediaSource[2]; //The Size must change depending on the Uris
        mediaSourcesAR[0] = mediaSourceAR; // uri
        SingleSampleMediaSource subtitleSourceAR = new SingleSampleMediaSource(subtitleUri, dataSourceFactory,
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                C.TIME_UNSET);
        mediaSourcesAR[1] = subtitleSourceAR;
        //mediaSourcesAR = new MergingMediaSource(mediaSourcesAR);
        //////////////////////////////////////////////////////////
        ConcatenatingMediaSource concatenatedSource =
                new ConcatenatingMediaSource(mediaSourceEN, mediaSourceFR, mediaSourceAR);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(concatenatedSource, false, false);

    }

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

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(new Date());

        videoUriEN = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/EN/%s/mixed.mp3", date));
        videoUriFR = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/FR/%s/mixed.mp3", date));
        videoUriAR = Uri.parse(String.format("https://source-audio-mixer.s3.us-east-2.amazonaws.com/mixed/AR/%s/mixed.mp3", date));

        subtitleUri = Uri.parse("https://raw.githubusercontent.com/andreyvit/subtitle-tools/master/sample.srt");
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

}
