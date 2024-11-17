package com.app.skte;

import static java.lang.Thread.sleep;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvHeight, tvGender, tvWeight, tvBMI, tvStatus, tvRecommendedDiet, tvExercise, tvRecommendEnergy, tvRecommendWater, tvTodaysEnergyConsumption, tvWarningHighCalorieDiet;
    private Button btnChangeState;
    private DatabaseHelper databaseHelper;
    private int todayEnergy = 0;
    private static final String PREFS_NAME = "EnergyPrefs";
    private static final String KEY_LAST_UPDATE = "lastUpdate";
    private static final String KEY_TODAY_ENERGY = "todayEnergy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastUpdate = prefs.getString(KEY_LAST_UPDATE, "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastUpdate)) {
            todayEnergy = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LAST_UPDATE, today);
            editor.putInt(KEY_TODAY_ENERGY, todayEnergy);
            editor.apply();
        } else {
            todayEnergy = prefs.getInt(KEY_TODAY_ENERGY, 0);
        }


        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);
        try {
            databaseHelper.createDatabase();
        } catch (IOException e) {
            throw new RuntimeException("Error creating database", e);
        }
        if (!isUserDataAvailable()) {
            Intent intent = new Intent(this, UserInputActivity.class);
//            startActivity(intent);
            startActivityForResult(intent, 1);
        } else if (isFirstOpenToday()) {
//        }else {
            Intent intent = new Intent(this, UserInputActivity.class);
            intent.putExtra("name", (String) getUserData("name"));
            intent.putExtra("day", (Integer) getUserData("day"));
            intent.putExtra("month", (Integer) getUserData("month"));
            intent.putExtra("year", (Integer) getUserData("year"));
            intent.putExtra("height", (String) getUserData("height"));
            intent.putExtra("weight", (String) getUserData("weight"));
            intent.putExtra("healthStatus", (String) getUserData("healthStatus"));
            intent.putExtra("gender", (String) getUserData("gender"));
            startActivityForResult(intent, 1);
            updateLastUpdateTime();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        tvBMI = findViewById(R.id.tvBMI);
        tvGender = findViewById(R.id.tvGender);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecommendedDiet = findViewById(R.id.tvRecommendedDiet);
        tvExercise = findViewById(R.id.tvExercise);
        tvRecommendEnergy = findViewById(R.id.tvRecommendEnergy);
        tvRecommendWater = findViewById(R.id.tvRecommendWater);
        tvTodaysEnergyConsumption = findViewById(R.id.tvTodaysEnergyConsumption);
        tvWarningHighCalorieDiet = findViewById(R.id.tvWarningHighCalorieDiet);
        btnChangeState = findViewById(R.id.btnChangeState);






        Button addSnackButton = findViewById(R.id.btnAddSnack);
        Button addMealButton = findViewById(R.id.btnAddMeal);

        addSnackButton.setOnClickListener(v -> showDialog(this, "snack"));
        addMealButton.setOnClickListener(v -> showDialog(this, "meal"));


        btnChangeState.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInputActivity.class);
            intent.putExtra("name", (String) getUserData("name"));
            intent.putExtra("day", (Integer) getUserData("day"));
            intent.putExtra("month", (Integer) getUserData("month"));
            intent.putExtra("year", (Integer) getUserData("year"));
            intent.putExtra("height", (String) getUserData("height"));
            intent.putExtra("weight", (String) getUserData("weight"));
            intent.putExtra("healthStatus", (String) getUserData("healthStatus"));
            intent.putExtra("gender", (String) getUserData("gender"));
            startActivityForResult(intent, 1);
        });

        displayUserData();





    }
    private void showDialog(Context context, String type) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_list);

        ListView listView = dialog.findViewById(R.id.listView);
        Cursor cursor = type.equals("snack") ? databaseHelper.get_snack() : databaseHelper.get_meal();

        String[] from = new String[]{"name", "energy"};
        int[] to = new int[]{R.id.itemName, R.id.itemEnergy};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, R.layout.list_item, cursor, from, to, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                Button addButton = view.findViewById(R.id.addButton);
                int energyIndex = cursor.getColumnIndex("energy");
                if (energyIndex != -1) {
                    int energy = cursor.getInt(energyIndex);
                    addButton.setOnClickListener(v -> {
                        todayEnergy += energy;
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_TODAY_ENERGY, todayEnergy);
                        editor.apply();
                        Toast.makeText(context, "Added " + energy + " energy. Total: " + todayEnergy, Toast.LENGTH_SHORT).show();
                        displayUserData();
                    });
                } else {
                    Log.e("MainActivity", "Column 'energy' not found in the cursor");
                }
            }
        };
        listView.setAdapter(adapter);

        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            displayUserData();
        }
    }

    private void showSnackList() {
        Cursor cursor = databaseHelper.get_snack();
        showList(cursor, "Snack");
    }

    private void showMealList() {
        Cursor cursor = databaseHelper.get_meal();
        showList(cursor, "Meal");
    }
    private void showList(Cursor cursor, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + type);
        SimpleCursorAdapter adapter;
        ListView listView = new ListView(this);
        if(type == "Snack"){
             adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    new String[]{"Món ăn vặt", "Năng lượng trung bình (kcal)", "Lượng muối trung bình (g)", "Lượng đường trung bình (g)"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            );
        }
        else {
             adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    new String[]{"Name", "avg_en", "Lượng muối trung bình (g)", "Lượng đường trung bình (g)"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            );
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                int avgEnIndex = cursor.getColumnIndex("Năng lượng trung bình (kcal)");
                int saltIndex = cursor.getColumnIndex("Lượng muối trung bình (g)");
                int sugarIndex = cursor.getColumnIndex("Lượng đường trung bình (g)");

                if (avgEnIndex != -1 && saltIndex != -1 && sugarIndex != -1) {
                    int avgEn = cursor.getInt(avgEnIndex);
                    int salt = cursor.getInt(saltIndex);
                    int sugar = cursor.getInt(sugarIndex);
                    addToTempData(avgEn, salt, sugar);
                } else {
                    Log.e("MainActivity", "Column not found in the cursor");
                }
            }
        });

        builder.setView(listView);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void addToTempData(int avgEn, int salt, int sugar) {
        SharedPreferences sharedPreferences = getSharedPreferences("TempData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("avg_en", sharedPreferences.getInt("avg_en", 0) + avgEn);
        editor.putInt("salt", sharedPreferences.getInt("salt", 0) + salt);
        editor.putInt("sugar", sharedPreferences.getInt("sugar", 0) + sugar);
        editor.apply();
        displayUserData();
    }

    private Object getUserData(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        return allEntries.get(key);
    }
    private void updateLastUpdateTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastUpdateTime", System.currentTimeMillis());
        editor.apply();
    }

    private boolean isFirstOpenToday() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        long lastUpdateTime = sharedPreferences.getLong("lastUpdateTime", 0);
        Calendar lastUpdateCalendar = Calendar.getInstance();
        lastUpdateCalendar.setTimeInMillis(lastUpdateTime);

        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) != lastUpdateCalendar.get(Calendar.YEAR) ||
                today.get(Calendar.DAY_OF_YEAR) != lastUpdateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isUserDataAvailable() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        return sharedPreferences.contains("name") && sharedPreferences.contains("height") && sharedPreferences.contains("weight") && sharedPreferences.contains("healthStatus");
    }

    private double calculateBMI(double height, double weight) {
    // Convert height from cm to meters
    height = height / 100;
    double bmi = weight / (height * height);
    return Double.parseDouble(String.format(Locale.US, "%.1f", bmi));
}

    private void displayUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        int day = sharedPreferences.getInt("day", 0);
        int month = sharedPreferences.getInt("month", 0);
        int year = sharedPreferences.getInt("year", 0);
        String height = sharedPreferences.getString("height", "");
        String weight = sharedPreferences.getString("weight", "");
        String healthStatus = sharedPreferences.getString("healthStatus", "");
        String gender = sharedPreferences.getString("gender", "");
        Cursor snack, meal, activity, diet, energy, energy_recommend, growth;
        int age = calculateAge(year, month, day);
        double bmi = 0;
        int bmi_status = 0;
        if (!height.isEmpty() && !weight.isEmpty()) {
            bmi = calculateBMI(Double.parseDouble(height), Double.parseDouble(weight));
            if (bmi < 18.5) {
                bmi_status = -1; // Thin
            } else if (bmi >= 18.5 && bmi < 24.9) {
                bmi_status = 0; // Normal
            } else {
                bmi_status = 1; // Fat
            }
        }
        String recommendedDiet, exercise, warningHighCalorieDiet, recommendEnergy;
        int recommendWater, todaysEnergyConsumption;
        snack = databaseHelper.get_snack();
        meal = databaseHelper.get_meal();
        if (gender.equals("Male")) {
            activity = databaseHelper.get_activity_boy();
            diet = databaseHelper.get_diet_boy();
            energy = databaseHelper.get_energy_boy();
            growth = databaseHelper.get_growth_data_boy();

        } else {
            activity = databaseHelper.get_activity_girl();
            diet = databaseHelper.get_diet_girl();
            energy = databaseHelper.get_energy_boy();
        }

        recommendedDiet = getRecommendedDiet(diet, age, bmi_status);
        exercise = getExerciseRecommendation(activity, age, bmi_status);
        recommendEnergy = getRecommendedEnergy(energy, age, bmi_status);
        recommendWater = getRecommendedWater(age);
        todaysEnergyConsumption = getTodaysEnergyConsumption();
        warningHighCalorieDiet = getWarningHighCalorieDiet(recommendEnergy, bmi);

        try {
            tvName.setText("Tên: " + name);
            tvAge.setText("Tuổi: " + age / 12);
            tvHeight.setText("Chiều cao: " + height + " cm");
            tvWeight.setText("Cân nặng: " + weight+ " kg");
            tvBMI.setText("BMI: " + bmi);
            tvStatus.setText("Trạng thái: " + healthStatus);
            tvGender.setText("Giới tính: " + gender);
            tvRecommendedDiet.setText("Bữa ăn khuyến nghị: " + recommendedDiet);
            tvExercise.setText("Hooạt động thể chất: " + exercise);
            tvRecommendEnergy.setText("Năng lượng khuyến nghị: " + recommendEnergy + " kcal/ngày");
            tvRecommendWater.setText("Lượn nước khuyến nghị: " + recommendWater+ " ml/ngày");
            tvTodaysEnergyConsumption.setText("Năng lượng tiêu thụ: " + todaysEnergyConsumption+ " kcal");
            tvWarningHighCalorieDiet.setText("Cảnh báo: " + warningHighCalorieDiet);
        } catch (Exception e) {
            Log.e("MainActivity", "Error displaying user data: " + e.getMessage());
        }
    }

    private int calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int monthDiff = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        int dayDiff = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);

        if (monthDiff < 0 || (monthDiff == 0 && dayDiff < 0)) {
            age--;
            monthDiff += 12;
        }

        int totalMonths = age * 12 + monthDiff;

        return totalMonths;
    }

    private String getRecommendedDiet(Cursor diet, int age, int bmi_status) {
        int tuoiIndex = diet.getColumnIndex("Tuoi");
        int valueIndex;

        if (bmi_status == -1) {
            valueIndex = diet.getColumnIndex("Che_do_thieu_can");
        } else if (bmi_status == 0) {
            valueIndex = diet.getColumnIndex("Che_do_binh_thuong");
        } else {
            valueIndex = diet.getColumnIndex("Che_do_thua_can");
        }

        if (tuoiIndex == -1 || valueIndex == -1) {
            throw new IllegalStateException("Column not found in the cursor");
        }

        if (diet.moveToFirst()) {
            do {
                int tuoi = diet.getInt(tuoiIndex);
                if (tuoi <= age) {
                    return diet.getString(valueIndex);
                }
            } while (diet.moveToNext());
        }
        return "";
    }

    private String getExerciseRecommendation(Cursor diet, int age, int bmi_status) {
        int tuoiIndex = diet.getColumnIndex("Tuoi");
        int valueIndex;
        if (bmi_status == -1) {
            valueIndex = diet.getColumnIndex("Hoat_dong_thieu_can");
        } else if (bmi_status == 0) {
            valueIndex = diet.getColumnIndex("Hoat_dong_khoe_manh");
        } else {
            valueIndex = diet.getColumnIndex("Hoat_dong_thua_can");
        }
        if (tuoiIndex == -1 || valueIndex == -1) {
            throw new IllegalStateException("Column not found in the cursor");
        }
        if (diet.moveToFirst()) {
            do {
                int tuoi = diet.getInt(tuoiIndex);
                if (tuoi <= age) {
                    return diet.getString(valueIndex);
                }
            } while (diet.moveToNext());
        }
        return "";
    }

    private String getRecommendedEnergy(Cursor energy, int age, int bmi_status) {
        int tuoiIndex = energy.getColumnIndex("Tuoi");
        int valueIndex;
        if (bmi_status == -1) {
            valueIndex = energy.getColumnIndex("Nang_luong_thieu_can (kcal/ngay)");
        } else if (bmi_status == 0) {
            valueIndex = energy.getColumnIndex("Nang_luong_khoe_manh (kcal/ngay)");
        } else {
            valueIndex = energy.getColumnIndex("Nang_luong_thua_can (kcal/ngay)");
        }
        if (tuoiIndex == -1 || valueIndex == -1) {
            throw new IllegalStateException("Column not found in the cursor");
        }
        if (energy.moveToFirst()) {
            do {
                int tuoi = energy.getInt(tuoiIndex);
                if (tuoi <= age) {
                    return energy.getString(valueIndex);
                }
            } while (energy.moveToNext());
        }
        return "";
    }

    private int getRecommendedWater(int age) {
        if (age < 12) {
            return 1000; // ml per day
        } else if (age < 18) {
            return 1500; // ml per day
        } else {
            return 2000; // ml per day
        }
    }

    private int getTodaysEnergyConsumption() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    return prefs.getInt(KEY_TODAY_ENERGY, 0);
}

    private String getWarningHighCalorieDiet(String todaysEnergyConsumption, double bmi) {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    int todayEnergy = prefs.getInt(KEY_TODAY_ENERGY, 0);
    String recommendEnergyText = tvRecommendEnergy.getText().toString();
    String warning = "";
    // Check if the string contains the expected delimiter
    Log.d("MainActivity", "Recommend energy text: " + recommendEnergyText);
    String[] parts = todaysEnergyConsumption.split("-");
    if (parts.length == 2) {
        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());

        if (todayEnergy > max) {
            warning += "Bạn đã tiêu thụ năng lượng nhiều hơn khuyến nghị!\n";
        } else if (todayEnergy < min) {
            warning += "Bạn đã tiêu thụ năng lượng ít hơn khuyến nghị!\n";
        }
    } else {
        Log.e("MainActivity", "Unexpected format for energy range: " + todaysEnergyConsumption);
    }

    if (bmi >= 25) {
        warning += "Cảnh báo: Bạn có chỉ số BMI cao!";
    } else if (bmi < 18.5) {
        warning += "Cảnh báo: Bạn có chỉ số BMI thấp!";
    }

    return warning;
}


}