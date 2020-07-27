package com.example.task;

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

import java.util.Date;
import java.util.List;


/**
 * This class displays a list of task in a RecyclerView.
 * The task are saved in a Room database.
 * The layout for this activity also displays a FAB that
 * allows users to start the NewTaskActivity to add new tasks.
 * Users can delete a task by swiping it away, or delete all words
 * through the Options menu. Whenever a new word is added, deleted,
 * or updated, the RecyclerView showing the list of words
 * automatically updates.
 */
public class MainActivity extends AppCompatActivity {
    // Request codes
    public static final int NEW_TASK_REQUEST_CODE = 1;
    public static final int UPDATE_TASK_REQUEST_CODE = 2;

    public static final String EXTRA_DATA_ID = "extra_data_id";
    public static final String EXTRA_DATA_UPDATE_TASK = "extra_task_to_be_updated";
    public static final String EXTRA_DATA_UPDATE_DETAILS = "extra_details_to_be_updated";
    public static final String EXTRA_DATA_UPDATE_DATE = "extra_date_to_be_updated";

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

        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mFab = findViewById(R.id.fab);

        // Set up the custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Floating action button setup
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask();
            }
        });

        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mAdapter = new TaskListAdapter();
        recyclerView.setAdapter(mAdapter);

        // Set up the WordViewModel.
        mViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        // Get all the words from the database
        // and associate them to the mAdapter.
        mViewModel.getAllTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> tasks) {
                mAdapter.submitList(tasks); // Submits a new list to be diffed, and displayed.
            }
        });

        // Add the functionality to swipe items in the
        // RecyclerView to delete the swiped item.
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            // Will not implement the onMove() in this app.
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // When the use swipes a word,
            // delete that word from the database.
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task myTask = mAdapter.getTaskAtPosition(position);
                // Delete the task.
                mViewModel.delete(myTask);
                taskCompleted();
            }
        });
        // Attach the ItemTouchHelper to the RecyclerView.
        helper.attachToRecyclerView(recyclerView);

        mAdapter.setOnItemClickListener(new TaskListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
               launchEditActivity(task);
            }

            @Override
            public void onDeleteClick(int position) {
                Task myTask = mAdapter.getTaskAtPosition(position);
                mViewModel.delete(myTask);
                taskCompleted();
            }
        });
    }

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


    private void taskCompleted() {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.task_completed, Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();

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
}