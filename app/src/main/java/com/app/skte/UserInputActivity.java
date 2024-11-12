package com.app.skte;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class UserInputActivity extends AppCompatActivity {

    private EditText etName, etHeight, etWeight;
    private TextView tvDateOfBirth;
    private Spinner spHealthStatus;
    private Button btnSave;
    private int day, month, year;
    private Spinner spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        etName = findViewById(R.id.etName);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        spHealthStatus = findViewById(R.id.spHealthStatus);
        spGender = findViewById(R.id.spGender);
        btnSave = findViewById(R.id.btnSave);

        // Set up the health status spinner
        ArrayAdapter<CharSequence> healthStatusAdapter = ArrayAdapter.createFromResource(this,
                R.array.health_status_array, android.R.layout.simple_spinner_item);
        healthStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHealthStatus.setAdapter(healthStatusAdapter);

        // Set up the gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Pre-fill the input fields with existing data
        Intent intent = getIntent();
        etName.setText(intent.getStringExtra("name"));
        etHeight.setText(intent.getStringExtra("height"));
        etWeight.setText(intent.getStringExtra("weight"));
        tvDateOfBirth.setText(intent.getIntExtra("day", 0) + "/" + (intent.getIntExtra("month", 0) + 1) + "/" + intent.getIntExtra("year", 0));
        spHealthStatus.setSelection(healthStatusAdapter.getPosition(intent.getStringExtra("healthStatus")));
        spGender.setSelection(genderAdapter.getPosition(intent.getStringExtra("gender")));

        // Set up the date picker dialog
        tvDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }
    private void saveUserData() {
        String name = etName.getText().toString();
        String height = etHeight.getText().toString();
        String weight = etWeight.getText().toString();
        String healthStatus = spHealthStatus.getSelectedItem().toString();
        String gender = spGender.getSelectedItem().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putInt("day", day);
        editor.putInt("month", month);
        editor.putInt("year", year);
        editor.putString("height", height);
        editor.putString("weight", weight);
        editor.putString("healthStatus", healthStatus);
        editor.putString("gender", gender);
        editor.putLong("lastUpdateTime", System.currentTimeMillis());
        editor.apply();

        setResult(RESULT_OK);
        finish();
    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        year = selectedYear;
                        month = selectedMonth;
                        day = selectedDay;
                        tvDateOfBirth.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}