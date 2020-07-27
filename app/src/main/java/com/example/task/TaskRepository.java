package com.example.task;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * This class holds the implementation code for the methods that interact with the database.
 * Using a repository allows to group the implementation methods together,
 * and allows the TaskViewModel to be a clean interface between the
 * rest of the app and the database.
 *
 * For insert, update and delete, and longer-running queries,
 * must run the database interaction methods in the background.
 *
 * To implement a database method is to call it on the data
 * access object (DAO), in the background if applicable.
 */
public class TaskRepository {

    private TaskDao mTaskDao;
    private LiveData<List<Task>> mAllTasks;

    public TaskRepository(Application application) {
        TaskRoomDatabase db = TaskRoomDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
        mAllTasks = mTaskDao.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return mAllTasks;
    }

    public void insert(Task task) {
        new insertTaskAsyncTask(mTaskDao).execute(task);
    }

    public void update(Task task) {
        new updateTaskAsyncTask(mTaskDao).execute(task);
    }

    // Must run off main thread
    public void delete(Task task) {
        new deleteTaskAsyncTask(mTaskDao).execute(task);
    }

    public void deleteAll() {
        new deleteAllTaskAsyncTask(mTaskDao).execute();
    }

    // Static inner classes below here to run database interactions in the background.
    /**
     * Inserts a task into the database.
     */
    private static class insertTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private TaskDao mAsyncTaskDao;

        public insertTaskAsyncTask(TaskDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... tasks) {
            mAsyncTaskDao.insert(tasks[0]);
            return null;
        }
    }

    /**
     * Updates a task in the database.
     */
    private static class updateTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private TaskDao mAsyncTaskDao;

        public updateTaskAsyncTask(TaskDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... tasks) {
            mAsyncTaskDao.update(tasks[0]);
            return null;
        }
    }

    /**
     * Deletes a single task from the database.
     */
    private static class deleteTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private TaskDao mAsyncTaskDao;

        public deleteTaskAsyncTask(TaskDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... tasks) {
            mAsyncTaskDao.delete(tasks[0]);
            return null;
        }
    }

    /**
     * Deletes all tasks from the database (does not delete the table).
     */
    private static class deleteAllTaskAsyncTask extends AsyncTask<Void, Void, Void> {

        private TaskDao mAsyncTaskDao;

        public deleteAllTaskAsyncTask(TaskDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}
