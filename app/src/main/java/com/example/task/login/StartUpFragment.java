package com.example.task.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.task.R;

/**
 * This class provides a conditional process for the user to login or register.
 * The user must register first before logging in and use the function of the app.
 */
public class StartUpFragment extends Fragment {

    // Initialize member variables
    private Button mLoginButton;
    private TextView mRegisterHereView;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_start_up, container, false);

        // Capture the view objects from the inflated layout.
        mLoginButton = view.findViewById(R.id.button_login);
        mRegisterHereView = view.findViewById(R.id.textView_register);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = requireContext();
        final Activity activity = requireActivity();
        navController = Navigation.findNavController(activity, R.id.login_nav_host);

        // Navigate user to the login process.
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.login);
            }
        });

        // Open the dummy privacy policy and user agreement dialog.
        mRegisterHereView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.notice)
                        .setMessage(R.string.notice_message)
                        .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If the user agrees, start the registration process.
                                navController.navigate(R.id.action_start_up_to_initial_register);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }
}