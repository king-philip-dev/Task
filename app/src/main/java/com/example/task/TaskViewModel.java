package com.example.task;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.task.arch.Task;
import com.example.task.arch.TaskRepository;

import java.util.List;

/**
 * The TaskViewModel provides the interface between the UI
 * and the data layer of the app, represented by the Repository.
 */
public class TaskViewModel extends AndroidViewModel {

    // Member variables
    private TaskRepository mRepository;
    private LiveData<List<Task>> mAllTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
        mAllTasks = mRepository.getAllTasks();
    }

    // Methods that will be used in the main activity.
    LiveData<List<Task>> getAllTasks() {
        return mAllTasks;
    }

    public void insert(Task task) {
        mRepository.insert(task);
    }

    public void update(Task task) {
        mRepository.update(task);
    }

    public void delete(Task task) {
        mRepository.delete(task);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }
}
