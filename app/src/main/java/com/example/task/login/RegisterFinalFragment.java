package com.example.task.login;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.task.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.regex.Pattern;


public class RegisterFinalFragment extends Fragment {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@_#$%^&+=])(?=\\S+$).{6,}$");

    // Initialize member variables
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputEditText mEditEmailView;
    private TextInputEditText mEditPasswordView;
    private ProgressBar mProgressBar;
    private Button mSendEmailButton;
    private Button mDoneButton;
    private CheckBox mAgreeCheckBox;

    // Declared varibles
    private String email;
    private String password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_final, container, false);

        // Capture the view objects from the inflated layout.
        mEmailLayout = view.findViewById(R.id.inputLayout_email_address);
        mPasswordLayout = view.findViewById(R.id.inputLayout_password);
        mEditEmailView = view.findViewById(R.id.editText_email_address);
        mEditPasswordView = view.findViewById(R.id.editText_password);
        mProgressBar = view.findViewById(R.id.progressBar);
        mSendEmailButton = view.findViewById(R.id.button_send_email);
        mDoneButton = view.findViewById(R.id.button_done);
        mAgreeCheckBox = view.findViewById(R.id.checkBox_agree);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail() | !validatePassword()) {
                    return;
                }
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Context context = requireContext();
        Bundle bundle = getArguments();
        if (bundle != null) {
            RegisterFinalFragmentArgs args = RegisterFinalFragmentArgs.fromBundle(bundle);
            Toast.makeText(context, args.getName() + " " + args.getPhone(),
                    Toast.LENGTH_LONG).show();

        }

    }

    /**
     * This method will validate the email address field.
     */
    private boolean validateEmail() {
        mEditEmailView.addTextChangedListener(textWatcher);

        email = Objects.requireNonNull(mEditEmailView.getText()).toString();
        if (email.isEmpty()) {
            mEmailLayout.setError(getString(R.string.cannot_be_empty));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailLayout.setError(getString(R.string.email_bad_format));
            return false;
        }
        else {
            mEmailLayout.setError(null);
            return true;
        }
    }

    /**
     * This method will validate the password field.
     */
    private boolean validatePassword() {
        mEditPasswordView.addTextChangedListener(textWatcher);

        password = Objects.requireNonNull(mEditPasswordView.getText()).toString();
        if (password.isEmpty()) {
            mPasswordLayout.setError(getString(R.string.cannot_be_empty));
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            mPasswordLayout.setError(getString(R.string.password_error));
            return false;
        }
        else {
            mPasswordLayout.setError(null);
            return true;
        }
    }

    /**
     * This object will notify the view object, in this case the EditText views
     * if there are changes in its character inputs.
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignored
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Remove error in the input fields.
            email = Objects.requireNonNull(mEditEmailView.getText()).toString();
            if (email.length() > 0) {
                mEmailLayout.setErrorEnabled(false);
            }

            password = Objects.requireNonNull(mEditPasswordView.getText()).toString();
            if (password.length() > 0) {
                mPasswordLayout.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Ignored
        }
    };

}