package com.buggyarts.musicnotes;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    private Context mContext;
    private Button buttonPlay;
    private Button buttonStop;
    private TextView textShowNotes;
    private MediaPlayer mediaPlayer;
    private MultiAutoCompleteTextView editMusicNotes;

    private Handler uiHandler;

    private int i = -1;
    private boolean shouldPause = false;
    private boolean isTextChanged = false;
    private ArrayList<String> enteredNotes;
    private static final String DOT = ".";
//    private static final String TAG = "MainActivity";
//    private static final String[] MUSIC_NOTES =
//            {"a1", "a1_s", "a2", "a2_s", "a3", "a3_s", "a4", "a4_s", "a5", "a5_s", "a6", "a6_s", "a7", "a7_s",
//            "b1", "b2", "b3", "b4", "b5", "b6", "b7",
//            "c1", "c1_s", "c2", "c2_s", "c3", "c3_s", "c4", "c4_s", "c5", "c5_s", "c6", "c6_s", "c7", "c7_s",
//            "d1", "d1_s", "d2", "d2_s", "d3", "d3_s", "d4", "d4_s", "d5", "d5_s", "d6", "d6_s", "d7", "d7_s",
//            "e1", "e2", "e3", "e4", "e5", "e6", "e7",
//            "f1", "f1_s", "f2", "f2_s", "f3", "f3_s", "f4", "f4_s", "f5", "f5_s", "f6", "f6_s", "f7", "f7_s",
//            "g1", "g1_s", "g2", "g2_s", "g3", "g3_s", "g4", "g4_s", "g5", "g5_s", "g6", "g6_s", "g7", "g7_s",
//            DOT };

    private static final String[] MUSIC_NOTES =
            {"a1", "a1s", "b1", "c1", "c1s", "c2", "d1", "d1s", "e1", "f1", "f1s", "g1", "g1s", DOT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        uiHandler = new Handler();
        enteredNotes = new ArrayList<>();

        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonStop = (Button) findViewById(R.id.button_stop);
        textShowNotes = (TextView) findViewById(R.id.text_show_notes);
        editMusicNotes = (MultiAutoCompleteTextView) findViewById(R.id.edit_music_notes);
        ArrayAdapter<String> musicNotesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, MUSIC_NOTES);

        editMusicNotes.setAdapter(musicNotesAdapter);
        editMusicNotes.setThreshold(1);
        editMusicNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isTextChanged = true;
            }
        });
        editMusicNotes.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public int findTokenStart(CharSequence charSequence, int i) {
                int j = i;

                while (j > 0 && charSequence.charAt(j - 1) != ' ') {
                    j--;
                }
                while (j < i && charSequence.charAt(j) == ' ') {
                    j++;
                }

                return j;
            }

            @Override
            public int findTokenEnd(CharSequence charSequence, int i) {
                int j = i;
                int len = charSequence.length();

                while (j < len) {
                    if (charSequence.charAt(j) == ' ') {
                        return j;
                    } else {
                        j++;
                    }
                }
                return len;
            }

            @Override
            public CharSequence terminateToken(CharSequence charSequence) {
                int i = charSequence.length();

                while (i > 0 && charSequence.charAt(i - 1) == ' ') {
                    i--;
                }

                if (i > 0 && charSequence.charAt(i - 1) == ' ') {
                    return charSequence;
                } else {
                    if (charSequence instanceof Spanned) {
                        SpannableString sp = new SpannableString(charSequence + " ");
                        TextUtils.copySpansFrom((Spanned) charSequence, 0, charSequence.length(),
                                Object.class, sp, 0);
                        return sp;
                    } else {
                        return charSequence + " ";
                    }
                }
            }
        });
        initListeners();
    }

    private void initListeners(){
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shouldPause){
                    shouldPause = false;
                    if(isTextChanged){
                        isTextChanged = false;
                        resetPlayback();
                        return;
                    }
                    playNext();
                    return;
                }

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                }
                resetPlayback();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldPause = true;
            }
        });
    }

    private void resetPlayback(){
        if(editMusicNotes.getText().toString().equals("")) return;
        i = -1;
        enteredNotes.clear();
        textShowNotes.setText("");
        enteredNotes.addAll(Arrays.asList(editMusicNotes.getText().toString().split(" ")));
        playNext();
    }

    private void playNote(int pos){
        if(mediaPlayer != null) mediaPlayer.reset();

        if(enteredNotes.get(pos).equals(DOT)){
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playNext();
                }
            }, 100);
        }
        else {
            try {
                mediaPlayer = MediaPlayer.create(mContext,
                        getResources().getIdentifier(enteredNotes.get(pos), "raw", getPackageName()));
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(this);
            } catch (Resources.NotFoundException e){
                Toast.makeText(getApplicationContext(), "Invalid Note entered", Toast.LENGTH_SHORT).show();
                playNext();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext();
    }

    private void playNext(){
        if(shouldPause) return;
        i += 1;
        if(i < enteredNotes.size()){
            textShowNotes.setText(String.format("%s%s ", textShowNotes.getText(), enteredNotes.get(i)));
            playNote(i);
        }
    }
}
