package com.example.task;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

/**
 * Adapter for the RecyclerView that displays a list of tasks.
 */
public class TaskListAdapter extends ListAdapter<Task, TaskListAdapter.TaskViewHolder> {

    private static OnItemClickListener itemClickListener;

    protected TaskListAdapter() {
        super(diffCallback);
    }

    /**
     * This constant is a callback for calculating the difference between
     * two non-null item in the list.
     */
    private static final DiffUtil.ItemCallback<Task> diffCallback = new DiffUtil.ItemCallback<Task>() {

        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTask().equals(newItem.getTask()) &&
                    oldItem.getDetails().equals(newItem.getDetails()) &&
                    oldItem.getDate() == newItem.getDate();
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Get the position of the task item in the date set.
        Task currentTask = getItem(position);
        holder.deleteItemView.setChecked(false);

        // Set the text of the task and details.
        holder.taskItemView.setText(currentTask.getTask());
        holder.detailsItemView.setText(currentTask.getDetails());

        if (currentTask.getDate() != null) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
                    new SimpleDateFormat("EEE, MMM dd, hh:mm a");
            String date = dateFormat.format(currentTask.getDate());
            holder.dateItemView.setText(date);
            holder.dateItemView.setVisibility(View.VISIBLE);
        } else {
            holder.dateItemView.setVisibility(View.GONE);
        }

        if (!holder.taskItemView.getText().toString().isEmpty()) {
            holder.taskItemView.setVisibility(View.VISIBLE);
        } else {
            holder.taskItemView.setVisibility(View.GONE);
        }

        if (!holder.detailsItemView.getText().toString().isEmpty()) {
            holder.detailsItemView.setVisibility(View.VISIBLE);
        } else {
            holder.detailsItemView.setVisibility(View.GONE);
        }
    }

    /**
     * Gets the word at a given position.
     * This method is useful for identifying which task
     * was clicked or swiped in methods that handle user events.
     *
     * @param position The position of the task in the RecyclerView
     * @return The task at the given position
     */
    public Task getTaskAtPosition(int position) {
        return getItem(position);
    }


    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView taskItemView;
        private TextView detailsItemView;
        private TextView dateItemView;
        private RadioButton deleteItemView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskItemView = itemView.findViewById(R.id.textView_task);
            detailsItemView = itemView.findViewById(R.id.textView_details);
            dateItemView = itemView.findViewById(R.id.textView_date);
            deleteItemView = itemView.findViewById(R.id.radioButton_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(getItem(position));
                    }
                }
            });

            deleteItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
                        itemClickListener.onDeleteClick(position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        TaskListAdapter.itemClickListener = itemClickListener;
    }
}
