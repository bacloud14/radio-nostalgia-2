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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.util.Util;
import com.radiomeditation.exoplayer.R;


import androidx.appcompat.app.AppCompatActivity;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity implements ShareDialog.ShareDialogListener {

    private PlayerView playerView;
    private TextView textViewURL;
    private Button submit;


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
        shareDialog.show(getSupportFragmentManager(), "share dialog");
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
