package ru.ninaland.heartrate;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {

    //Переменные для спинера
    public final static int SEX_MALE = 0;
    public final static int SEX_FEMALE = 1;

    //интервал секундомера
    public final static int SW_INTERVAL = 1000;

    //интервал метронома
    public static int metronomeInterval;

    private Spinner mSexSpinner;

    //Здесь хранится макета ввода данных
    private LinearLayout inputData;
    private TextView mSWScreen;
    private LinearLayout resultData;

    //Переменные треда секундамера
    private Handler mSWHandler;
    private Runnable mSWUpdater;

    //Кнопка старта
    private Button startButton;

    //Поля ввода данных
    EditText ageField;
    EditText heightField;
    EditText weightField;
    EditText initialRateField;

    //Результирующие поля
    TextView mocField;
    TextView apField;
    TextView aphrField;
    EditText finalRateField;

    //let's check up the Timer
    private Timer mTimer;
    private MetronomeTimerTask metronomeTimerTask;

    //just for debugging
    private int quantity = 0;
    private Toast numberFormatErrorToast;

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
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        inputData = (LinearLayout) findViewById(R.id.input_data);
        resultData = (LinearLayout) findViewById(R.id.result_data);

        mSexSpinner = (Spinner) findViewById(R.id.spinner_sex);
        setupSpinner();

        mSWScreen = (TextView) findViewById(R.id.stop_watch_screen);
        mSWHandler = new Handler();

        startButton = (Button) findViewById(R.id.start_button);

        ageField = (EditText) findViewById(R.id.age_field_edit_view);
        heightField = (EditText) findViewById(R.id.height_field_edit_view);
        weightField = (EditText) findViewById(R.id.weight_field_edit_view);
        initialRateField = (EditText) findViewById(R.id.initial_rate_field_edit_view);
        mocField = (TextView) findViewById(R.id.moc_field_text_view);
        apField = (TextView) findViewById(R.id.ap_field_text_view);
        aphrField = (TextView) findViewById(R.id.ap_hr_field_text_view);
        finalRateField = (EditText) findViewById(R.id.final_rate_field_edit_view);

        AppData.swPaused = false;
        AppData.swStarted = false;
        AppData.resultMode = false;
        AppData.goBackMode = false;

        numberFormatErrorToast = Toast.makeText(this, getString(R.string.put_correct_data), Toast.LENGTH_LONG);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppData.swStarted && !AppData.resultMode && !AppData.goBackMode) {
                    mSWValue = 3;
                    prepValue = 5;
                    if (!AppData.swPaused) {
                        try {
                            metronomeInterval = getMetronomeInterval();
                        } catch (NumberFormatException e) {
                            numberFormatErrorToast.show();
                            return;
                        }
                        quantity = 0;//debugging field
                    }
                    inputData.setVisibility(View.GONE);
                    mSWScreen.setVisibility(View.VISIBLE);
                    //startButton.setEnabled(false);
                    startStopWatch();
                    startMetronome();
                    startButton.setText("отмена");
                    AppData.swStarted = true;
                } else if (AppData.swStarted) {
                    releaseMediaPlayer();
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    quantity = 0;//debugging field
                    stopStopWatch();
                } else if (AppData.resultMode) {
                    getResultData();
                    AppData.resultMode = false;
                    AppData.goBackMode = true;
                    startButton.setText("обратно");
                } else if (AppData.goBackMode) {
                    goBack();
                }
            }
        });
    }

    //метод хелпер для обработки данных
    private int getMetronomeInterval() throws NumberFormatException {
        AppData.ageFieldInteger = Integer.parseInt(ageField.getText().toString());
        AppData.heightFieldInteger = Integer.parseInt(heightField.getText().toString());
        AppData.weightFieldInteger = Integer.parseInt(weightField.getText().toString());
        AppData.initialRateFieldInteger = Integer.parseInt(initialRateField.getText().toString());
        return Helpers.getMetronomeTimeInterval(AppData.ageFieldInteger, AppData.weightFieldInteger, AppData.mSex,
                AppData.heightFieldInteger, AppData.initialRateFieldInteger);
    }

    private void getResultData() throws NumberFormatException {
        AppData.finalRateFieldInteger = Integer.parseInt(finalRateField.getText().toString());
        mocField.setText(Helpers.getMOCData(AppData.heightFieldInteger, AppData.weightFieldInteger,
                Helpers.getStepRate(AppData.ageFieldInteger, AppData.weightFieldInteger, AppData.mSex,
                        AppData.heightFieldInteger, AppData.initialRateFieldInteger),
                AppData.finalRateFieldInteger, AppData.ageFieldInteger));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppData.swPaused) {
            startStopWatch();
            startMetronome();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppData.swStarted) {
            releaseMediaPlayer();
            if (mTimer != null) {
                mTimer.cancel();
            }
            pauseStopWatch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        if (mTimer != null) {
            mTimer.cancel();
        }
        stopStopWatch();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the sex.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_sex_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSexSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.sex_male))) {
                        AppData.mSex = SEX_MALE; // Male
                    } else {
                        AppData.mSex = SEX_FEMALE; // Female
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AppData.mSex = SEX_MALE; // Unknown
            }
        });
    }


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
        mSWScreen.setVisibility(View.GONE);
        inputData.setVisibility(View.VISIBLE);
        //startButton.setEnabled(true);
        startButton.setText("старт");
        AppData.swStarted = false;
        AppData.swPaused = false;
    }

    private void stopStopWatchAtTheEnd() {
        mSWHandler.removeCallbacks(mSWUpdater);
        mSWUpdater = null;
        mSWScreen.setVisibility(View.GONE);
        resultData.setVisibility(View.VISIBLE);
        startButton.setText("итог");
        AppData.swStarted = false;
        AppData.swPaused = false;
        AppData.resultMode = true;
    }

    private void goBack() {
        inputData.setVisibility(View.VISIBLE);
        resultData.setVisibility(View.GONE);
        startButton.setText("старт");
        AppData.swStarted = false;
        AppData.swPaused = false;
        AppData.resultMode = false;
        AppData.goBackMode = false;
    }

    private void pauseStopWatch() {
        mSWHandler.removeCallbacks(mSWUpdater);
        mSWUpdater = null;
        AppData.swPaused = true;
    }


    private void startMetronome() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        // re-schedule timer here otherwise, IllegalStateException of "TimerTask is scheduled already" will be thrown
        mTimer = new Timer();
        metronomeTimerTask = new MetronomeTimerTask();
        mTimer.scheduleAtFixedRate(metronomeTimerTask, (prepValue + 1) * 1000, metronomeInterval);
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
                            mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.metronome_click);
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


//    private class MetronomePlayer extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            //запускаем диалог показывающий что ты работаешь во всю
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
////            while (mSWValue >= 0) {
//            long now = new Date().getTime();
//            Log.i(MainActivity.class.getSimpleName(), String.valueOf(now));
//            releaseMediaPlayer();
//            int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
//                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//
//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.metronome_click);
//                mMediaPlayer.start();
//                mMediaPlayer.setOnCompletionListener(mCompletionListener);
//            }
//            long then = new Date().getTime();
//            long corr = then - now;
//            Log.i(MainActivity.class.getSimpleName(), String.valueOf(then));
//            Log.i(MainActivity.class.getSimpleName(), String.valueOf(corr));
//
////                try {
////                    Thread.sleep(500-corr);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////                long after = new Date().getTime();
////                Log.i(MainActivity.class.getSimpleName(), String.valueOf(after-now));
////
////            }
//            return null;
//        }

//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            //а здесь мы прячем диалог и заканчиваем работу всех функций которые были запущены в doInBackground()
//        }
//    }
