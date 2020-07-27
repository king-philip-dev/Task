package com.example.task;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;

import static com.example.task.ReminderBroadcast.CHANNEL_ID;


/**
 * This class displays a list of task in a RecyclerView. The task are saved in a Room database.
 * The layout for this activity also displays a FAB that allows users to start the
 * NewTaskActivity to add new tasks. Users can delete a task by swiping it away, or delete all
 * tasks through the Options menu. Whenever a new task is added, deleted, or updated,
 * the RecyclerView showing the list of tasks automatically updates.
 *
 */
public class MainActivity extends AppCompatActivity {

    // Request codes for intents.
    public static final int NEW_TASK_REQUEST_CODE = 1;
    public static final int UPDATE_TASK_REQUEST_CODE = 2;

    public static final String EXTRA_DATA_ID = "extra_data_id";
    public static final String EXTRA_DATA_UPDATE_TASK = "extra_task_to_be_updated";
    public static final String EXTRA_DATA_UPDATE_DETAILS = "extra_details_to_be_updated";
    public static final String EXTRA_DATA_UPDATE_DATE = "extra_date_to_be_updated";

    private FirebaseAuth mAuth;
    private TaskViewModel mViewModel;
    private TaskListAdapter mAdapter;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFab;
    private Toast mPressAgainToast;

    private long beforeExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the instance of the view objects and capture them from the layout.
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mFab = findViewById(R.id.fab);

        mAuth = FirebaseAuth.getInstance();

        // Set up the custom toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the recycler view.
        mAdapter = new TaskListAdapter();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        // Set up the view model.
        // Get all the tasks from the database and associate them to the adapter.
        mViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        mViewModel.getAllTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> tasks) {
                mAdapter.submitList(tasks); // Submits a new list to be diffed, and displayed.
            }
        });

        // Add the functionality to swipe items in the RecyclerView to delete the swiped item.
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // Will not implement the onMove() in this app.
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // When the user swipes a task, delete that task from the database.
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task myTask = mAdapter.getTaskAtPosition(position);
                mViewModel.delete(myTask); // Delete the task.
                taskCompleted();
            }
        });
        helper.attachToRecyclerView(recyclerView); // Attach the touch helper to recycler view.

        // The user can edit and update the task, when the item in the recycler view is clicked.
        mAdapter.setOnItemClickListener(new TaskListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
               launchEditActivity(task);
            }
            // The user can also remove/complete the task when the radio button is click.
            @Override
            public void onDeleteClick(int position) {
                Task myTask = mAdapter.getTaskAtPosition(position);
                mViewModel.delete(myTask);
                taskCompleted();
            }
        });

        // Floating action button setup.
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask();
            }
        });
    }

    /**
     * Check firebase if there is a user currently logged in.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // TODO: Edit this part.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Retrieve the extended data from the NewTaskActivity intent.
                String taskData = data.getStringExtra(NewTaskActivity.EXTRA_REPLY_TASK);
                String detailsData = data.getStringExtra(NewTaskActivity.EXTRA_REPLY_DETAILS);
                long date = data.getLongExtra(NewTaskActivity.EXTRA_REPLY_DATE, 0);
                Date dateData = new Date(date); // Convert the date long type to date type.

                if (date != 0) {
                    Task task = new Task(taskData, detailsData, dateData);
                    mViewModel.insert(task);
                } else {
                    Task task = new Task(taskData, detailsData, null);
                    mViewModel.insert(task);
                }
            }
        } else if (requestCode == UPDATE_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                int id = data.getIntExtra(NewTaskActivity.EXTRA_REPLY_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Task cannot be updated.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String taskData = data.getStringExtra(NewTaskActivity.EXTRA_REPLY_TASK);
                String detailsData = data.getStringExtra(NewTaskActivity.EXTRA_REPLY_DETAILS);
                long date = data.getLongExtra(NewTaskActivity.EXTRA_REPLY_DATE, 0);
                Date dateData = new Date(date);

                if (date != 0) {
                    Task task = new Task(taskData, detailsData, dateData);
                    task.setId(id);
                    mViewModel.update(task);
                } else {
                    Task task = new Task(taskData, detailsData, null);
                    task.setId(id);
                    mViewModel.update(task);
                }
                Toast.makeText(this, "Task updated.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Not saved.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_all_tasks) {
            confirmDeleteAllTasks();
        } else {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Tell user that the task had just completed.
     */
    private void taskCompleted() {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.task_completed,
                Snackbar.LENGTH_LONG);

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
                mFab.hide();
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                mFab.show();
            }
        });

        snackbar.show();
    }

    /**
     * Do not close the app immediately.
     */
    @Override
    public void onBackPressed() {
        if (beforeExitTime + 2000 > System.currentTimeMillis()) {
            mPressAgainToast.cancel();
            super.onBackPressed();
        } else {
            mPressAgainToast = Toast.makeText(MainActivity.this, R.string.press_back_again,
                    Toast.LENGTH_SHORT);
            mPressAgainToast.show();
        }
        beforeExitTime = System.currentTimeMillis();
    }

    /**
     * Ask user to confirm delete all tasks.
     */
    private void confirmDeleteAllTasks() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create();
        // If there are no tasks in the display, ask user if want to add a tasks.
        if (mAdapter.getItemCount() == 0) {
            builder.setTitle(R.string.no_task).setMessage(R.string.no_task_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createNewTask();
                        }
                    }).create().show();
        } else {
            builder.setTitle(R.string.delete_all_tasks).setMessage(R.string.delete_all_tasks_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mViewModel.deleteAll();
                        }
                    }).create().show();
        }
    }

    /**
     * Bring user to NewTaskActivity to create a task.
     */
    private void createNewTask() {
        Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
        startActivityForResult(intent, NEW_TASK_REQUEST_CODE);
    }

    /**
     * Bring user to an activity to edit the selected task.
     */
    private void launchEditActivity(Task task) {
        Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
        intent.putExtra(EXTRA_DATA_ID, task.getId());
        intent.putExtra(EXTRA_DATA_UPDATE_TASK, task.getTask());
        intent.putExtra(EXTRA_DATA_UPDATE_DETAILS, task.getDetails());
        if (task.getDate() != null) {
            long date = task.getDate().getTime();
            intent.putExtra(EXTRA_DATA_UPDATE_DATE, date);
        } // No date from the selected task.
        startActivityForResult(intent, UPDATE_TASK_REQUEST_CODE);
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "ChannelName";
            String description = "Channel for this notification.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Create a notification manager object.
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    private void createAlarm() {
        Toast.makeText(MainActivity.this, "Reminder set!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, intent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long timeAtButtonClick = System.currentTimeMillis();
        long tenSeconds = 1000*10;
        manager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSeconds, pendingIntent);
    }
}