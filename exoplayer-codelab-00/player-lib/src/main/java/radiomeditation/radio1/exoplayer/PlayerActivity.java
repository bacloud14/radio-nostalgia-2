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
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.util.Util;
import com.radiomeditation.exoplayer.R;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service.*;
/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends Activity implements ShareDialog.ShareDialogListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = new Intent(this, AudioPlayerActivity.class);
        Util.startForegroundService(this, intent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.video_view);

        WebView webView = findViewById(R.id.webView);
        // Configure the webview so that the game will load
        WebSettings settings = webView.getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // Load in the game's HTML file
        webView.loadUrl("file:///android_asset/index.html");

        textViewURL = findViewById(R.id.textView_URL);
        submit = findViewById(R.id.button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSubmitDialog();
            }
        });

        playerView.setPlayer(AudioPlayerActivity.player);
    }

    public void openSubmitDialog(){
        ShareDialog shareDialog = new ShareDialog();
        // shareDialog.show(getSupportFragmentManager(), "share dialog");
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
