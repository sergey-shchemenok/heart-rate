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

public class ResultActivity extends AppCompatActivity {

    //Здесь хранится макет результата
    private LinearLayout resultData;

    //Кнопка старта
    private Button resultButton;

    //Результирующие поля
    TextView mocField;
    TextView apField;
    TextView aphrField;
    EditText finalRateField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultData = (LinearLayout) findViewById(R.id.result_data);
        resultButton = (Button) findViewById(R.id.result_button);

        mocField = (TextView) findViewById(R.id.moc_field_text_view);
        apField = (TextView) findViewById(R.id.ap_field_text_view);
        aphrField = (TextView) findViewById(R.id.ap_hr_field_text_view);
        finalRateField = (EditText) findViewById(R.id.final_rate_field_edit_view);

        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getResultData();
                }catch (NumberFormatException e){
                    Toast.makeText(ResultActivity.this, getString(R.string.put_correct_data), Toast.LENGTH_LONG).show();
                }
            }
        });

        resultButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                goBack();
                return true;
            }
        });
    }

    private void getResultData() throws NumberFormatException {
        AppData.finalRateFieldInteger = Integer.parseInt(finalRateField.getText().toString());
        mocField.setText(Helpers.getMOCData(AppData.heightFieldInteger, AppData.weightFieldInteger,
                AppData.stepRate, AppData.finalRateFieldInteger, AppData.ageFieldInteger));
//        aphrField.setText(Helpers.getAPHRData(AppData.heightFieldInteger, AppData.weightFieldInteger,
//                AppData.stepRate, AppData.finalRateFieldInteger, AppData.ageFieldInteger));
    }

    private void goBack() {
        Intent intent = new Intent(ResultActivity.this, InputActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (AppData.weightFieldInteger == 0 || AppData.ageFieldInteger == 0)
            startActivity(new Intent(ResultActivity.this, InputActivity.class));
    }
}


