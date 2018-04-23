package ru.ninaland.heartrate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static ru.ninaland.heartrate.AppData.mSWValue;
import static ru.ninaland.heartrate.AppData.prepValue;

public class InputActivity extends AppCompatActivity {

    //Переменные для спинера
    public final static int SEX_MALE = 0;
    public final static int SEX_FEMALE = 1;

    private Spinner mSexSpinner;

    //Здесь хранится макет ввода данных
    private LinearLayout inputData;

    //Кнопка старта
    private Button startButton;

    //Поля ввода данных
    EditText ageField;
    EditText heightField;
    EditText weightField;
    EditText initialRateField;

    //just for debugging
    private Toast numberFormatErrorToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        inputData = (LinearLayout) findViewById(R.id.input_data);

        mSexSpinner = (Spinner) findViewById(R.id.spinner_sex);
        setupSpinner();


        startButton = (Button) findViewById(R.id.start_button);

        ageField = (EditText) findViewById(R.id.age_field_edit_view);
        heightField = (EditText) findViewById(R.id.height_field_edit_view);
        weightField = (EditText) findViewById(R.id.weight_field_edit_view);
        initialRateField = (EditText) findViewById(R.id.initial_rate_field_edit_view);

        numberFormatErrorToast = Toast.makeText(this, getString(R.string.put_correct_data), Toast.LENGTH_LONG);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSWValue = 300;
                prepValue = 5;
                try {
                    AppData.metronomeInterval = getMetronomeInterval();
                } catch (NumberFormatException e) {
                    numberFormatErrorToast.show();
                    return;
                }
                Intent intent = new Intent(InputActivity.this, SWActivity.class);
                startActivity(intent);
            }
        });
    }

    //метод хелпер для обработки данных
    private int getMetronomeInterval() throws NumberFormatException {
        AppData.ageFieldInteger = Integer.parseInt(ageField.getText().toString());
        AppData.heightFieldInteger = Integer.parseInt(heightField.getText().toString());
        AppData.weightFieldInteger = Integer.parseInt(weightField.getText().toString());
        AppData.initialRateFieldInteger = Integer.parseInt(initialRateField.getText().toString());
        AppData.stepRate = Helpers.getStepRate(AppData.ageFieldInteger, AppData.weightFieldInteger, AppData.mSex,
                AppData.heightFieldInteger, AppData.initialRateFieldInteger);
        return 60000 / AppData.stepRate;
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
}


