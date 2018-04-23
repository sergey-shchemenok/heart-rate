package ru.ninaland.heartrate;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static ru.ninaland.heartrate.AppData.mSWValue;
import static ru.ninaland.heartrate.AppData.prepValue;

public class SWActivity extends AppCompatActivity {

    //интервал секундомера
    public final static int SW_INTERVAL = 1000;


    //Здесь хранится макет демонстрации секундомера
    private TextView mSWScreen;

    //Переменные треда секундамера
    private Handler mSWHandler;
    private Runnable mSWUpdater;

    //Кнопка отмены
    private Button cancelButton;

    //let's check up the Timer
    private Timer mTimer;
    private MetronomeTimerTask metronomeTimerTask;

    //just for debugging
    private int quantity = 0;

    /*
    Методы и переменные медиаплеера
     */
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };

    MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sw);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mSWScreen = (TextView) findViewById(R.id.stop_watch_screen);
        mSWHandler = new Handler();
        cancelButton = (Button) findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaPlayer();
                if (mTimer != null) {
                    mTimer.cancel();
                }
                quantity = 0;//debugging field
                stopStopWatch();
                Intent intent = new Intent(SWActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
            startStopWatch();
            try{
            startMetronome();}catch (IllegalArgumentException e){
                startActivity(new Intent(SWActivity.this, InputActivity.class));
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        if (mTimer != null) {
            mTimer.cancel();
        }
        stopStopWatch();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        releaseMediaPlayer();
//        if (mTimer != null) {
//            mTimer.cancel();
//        }
//        stopStopWatch();
//        Intent intent = new Intent(SWActivity.this, InputActivity.class);
//        startActivity(intent);
//    }

    //The helper methods for stopwatch
    private void startStopWatch() {
        mSWUpdater = new Runnable() {
            @Override
            public void run() {
                try {
                    if (prepValue > 3) {
                        mSWScreen.setText("Внимание!");
                        prepValue--;
                        return;
                    }
                    if (prepValue > 0) {
                        mSWScreen.setText(String.valueOf(prepValue) + "!");
                        prepValue--;
                        return;
                    }

                    if (mSWValue > 9) {
                        mSWScreen.setText(String.valueOf(mSWValue--));
                    } else if (mSWValue >= 0) {
                        mSWScreen.setText("0" + String.valueOf(mSWValue--));
                    } else {
                        stopStopWatchAtTheEnd();
                    }

                } finally {
                    mSWHandler.postDelayed(mSWUpdater, SW_INTERVAL);
                }
            }
        };
        mSWUpdater.run();
    }

    private void stopStopWatch() {
        mSWHandler.removeCallbacks(mSWUpdater);
        mSWUpdater = null;
        //startButton.setEnabled(true);
    }

    private void stopStopWatchAtTheEnd() {
        mSWHandler.removeCallbacks(mSWUpdater);
        mSWUpdater = null;
        Intent intent = new Intent(SWActivity.this, ResultActivity.class);
        startActivity(intent);
    }

    private void startMetronome() throws IllegalArgumentException {
        if (mTimer != null) {
            mTimer.cancel();
        }
        // re-schedule timer here otherwise, IllegalStateException of "TimerTask is scheduled already" will be thrown
        mTimer = new Timer();
        metronomeTimerTask = new MetronomeTimerTask();
        mTimer.scheduleAtFixedRate(metronomeTimerTask, (prepValue + 1) * 1000, AppData.metronomeInterval);
    }

    class MetronomeTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mSWValue > 0) {
                        long now = new Date().getTime();
                        Log.i(MainActivity.class.getSimpleName(), String.valueOf(now));
                        releaseMediaPlayer();
                        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            mMediaPlayer = MediaPlayer.create(SWActivity.this, R.raw.metronome_click);
                            mMediaPlayer.start();
                            mMediaPlayer.setOnCompletionListener(mCompletionListener);
                            quantity++;
                        }
                        long then = new Date().getTime();
                        long delay = then - now;
                        Log.i(MainActivity.class.getSimpleName(), String.valueOf(then));
                        Log.i(MainActivity.class.getSimpleName(), String.valueOf(delay));
                    } else {
                        releaseMediaPlayer();
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        Log.i(MainActivity.class.getSimpleName(), "Количество ударов: " + String.valueOf(quantity));
                    }
                }
            });
        }
    }
}
