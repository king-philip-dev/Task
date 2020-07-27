package com.example.task;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * Entity class that represents a word in the database
 */
@Entity(tableName = "task_table")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "task")
    private String mTask;

    @ColumnInfo(name = "details")
    private String mDetails;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "date")
    private Date mDate;

    public Task(String mTask, String mDetails, @Nullable Date mDate) {
        this.mTask = mTask;
        this.mDetails = mDetails;
        this.mDate = mDate;
    }

    /**
     * This constructor is annotated using @Ignore, because Room
     * expects only one constructor by default in an entity class.
     */
    @Ignore
    public Task(int id, String mTask, String mDetails, Date mDate) {
        this.id = id;
        this.mTask = mTask;
        this.mDetails = mDetails;
        this.mDate = mDate;
    }

    @Ignore
    public Task(String mTask, String mDetails) {
        this.mTask = mTask;
        this.mDetails = mDetails;
    }

    @Ignore
    public Task() {
        // Empty constructor.
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return mTask;
    }


    public String getDetails() {
        return mDetails;
    }


    public Date getDate() {
        return mDate;
    }
}
