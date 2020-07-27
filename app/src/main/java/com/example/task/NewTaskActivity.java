package com.example.task;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.task.MainActivity.EXTRA_DATA_ID;
import static com.example.task.MainActivity.EXTRA_DATA_UPDATE_DATE;
import static com.example.task.MainActivity.EXTRA_DATA_UPDATE_DETAILS;
import static com.example.task.MainActivity.EXTRA_DATA_UPDATE_TASK;

public class NewTaskActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY_ID = "com.example.task.EXTRA_ID";
    public static final String EXTRA_REPLY_TASK = "com.example.task.EXTRA_TASK";
    public static final String EXTRA_REPLY_DETAILS = "com.example.task.EXTRA_DETAILS";
    public static final String EXTRA_REPLY_DATE = "com.example.task.EXTRA_DATE";

    private EditText mEditTaskView;
    private EditText mEditDetailsView;
    private TextView mDateView;
    private ImageButton mRemoveDateButton;
    private ConstraintLayout mLayoutDateView;
    private InputMethodManager imm;

    private long date;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        mEditTaskView = findViewById(R.id.editText_task);
        mEditDetailsView = findViewById(R.id.editText_details);
        mDateView = findViewById(R.id.textView_date);
        mRemoveDateButton = findViewById(R.id.imageButton_remove_date);
        mLayoutDateView = findViewById(R.id.constraintLayout_date);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_close_24);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_ID)) {
            String task = intent.getStringExtra(EXTRA_DATA_UPDATE_TASK);
            String details = intent.getStringExtra(EXTRA_DATA_UPDATE_DETAILS);
            mEditTaskView.setText(task);
            mEditDetailsView.setText(details);

            if (task != null) {
                if (!task.isEmpty()) {
                    mEditTaskView.setSelection(task.length());
                    mEditTaskView.requestFocus();
                    if (details != null && details.isEmpty()) {
                        mEditDetailsView.setVisibility(View.GONE);
                    }
                } else {
                    mEditDetailsView.setVisibility(View.VISIBLE);
                    mEditDetailsView.requestFocus();
                }
            }

            if (intent.hasExtra(EXTRA_DATA_UPDATE_DATE)) {
                date = intent.getLongExtra(EXTRA_DATA_UPDATE_DATE, 0);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
                        new SimpleDateFormat("EEE, MMM dd, hh:mm a");
                String dateUpdateString = dateFormat.format(date);
                mDateView.setText(dateUpdateString);
                mLayoutDateView.setVisibility(View.VISIBLE);
            } else {
                mLayoutDateView.setVisibility(View.GONE);
            }
        } else {
            mEditDetailsView.setVisibility(View.GONE);
            mLayoutDateView.setVisibility(View.GONE);
            // Otherwise, start with empty fields.
        }

        if (mLayoutDateView != null) {
            mRemoveDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLayoutDateView.setVisibility(View.GONE);
                }
            });
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (mEditTaskView.requestFocus()) {
            imm.showSoftInput(mEditTaskView, InputMethodManager.SHOW_FORCED);
        } else {
            imm.hideSoftInputFromWindow(mEditTaskView.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu: this will add items to the action if it is present.
        getMenuInflater().inflate(R.menu.new_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button.
        int id = item.getItemId();
        switch (id) {
            case R.id.save_task:
                saveTask();
                break;
            case R.id.add_details:
                addDetails();
                break;
            case R.id.set_date:
                setDate();
                break;
            case android.R.id.home:
                finish();
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDetails() {
        mEditDetailsView.setVisibility(View.VISIBLE);
        mEditDetailsView.requestFocus();
    }

    private void setDate() {
        imm.hideSoftInputFromWindow(mEditTaskView.getWindowToken(), 0);
        final Calendar calendar = Calendar.getInstance();
        final int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(NewTaskActivity.this,
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePicker = new TimePickerDialog(NewTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                date = calendar.getTimeInMillis();

                                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
                                        new SimpleDateFormat("EEE, MMM dd, hh:mm a");
                                String dateString = dateFormat.format(date);
                                mLayoutDateView.setVisibility(View.VISIBLE);
                                mDateView.setText(dateString);

                            }
                        }, hourOfDay, minute, false);
                timePicker.show();
            }
        }, year, month, dayOfMonth);
        datePicker.show();
    }

    private void saveTask() {
        // Create a new intent for the reply.
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(mEditTaskView.getText()) && TextUtils.isEmpty(mEditDetailsView.getText())) {
            // No task was entered, set the result accordingly.
            setResult(RESULT_CANCELED, replyIntent);
        } else {
            // Get the new task that the user entered.
            String task = mEditTaskView.getText().toString();
            String details = mEditDetailsView.getText().toString();

            // Put the new task in the extras for the Intent.
            replyIntent.putExtra(EXTRA_REPLY_TASK, task);
            replyIntent.putExtra(EXTRA_REPLY_DETAILS, details);

            if (mLayoutDateView.getVisibility() == View.GONE) {
                date = 0;
            }

            replyIntent.putExtra(EXTRA_REPLY_DATE, date);

            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(EXTRA_DATA_ID)) {
                int id = extras.getInt(EXTRA_DATA_ID, -1);
                if (id != -1) {
                    replyIntent.putExtra(EXTRA_REPLY_ID, id);
                }
            }
            // Set result status to indicate success.
            setResult(RESULT_OK, replyIntent);
        }
        finish();
    }
}