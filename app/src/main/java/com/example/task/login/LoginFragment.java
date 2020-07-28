package com.example.task.login;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.example.task.MainActivity;
import com.example.task.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private static final String TAG = "Register";
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@_#$%^&+=])(?=\\S+$).{6,}$");

    // Initialize member variables
    private Activity mActivity;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ConstraintLayout mParentLayout;
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputEditText mEditEmailView;
    private TextInputEditText mEditPasswordView;
    private ProgressBar mProgressBar;
    private Button mLoginButton;
    private CheckBox mAgreeCheckBox;

    // Declared variables
    private String email;
    private String password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = requireActivity();

        // Initialize the Firebase instances.
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Capture the view objects from the inflated layout.
        mParentLayout = view.findViewById(R.id.constraintLayout_login);
        mEmailLayout = view.findViewById(R.id.inputLayout_email_address);
        mPasswordLayout = view.findViewById(R.id.inputLayout_password);
        mEditEmailView = view.findViewById(R.id.editText_email_address);
        mEditPasswordView = view.findViewById(R.id.editText_password);
        mProgressBar = view.findViewById(R.id.progressBar);
        mLoginButton = view.findViewById(R.id.button_login);
        mAgreeCheckBox = view.findViewById(R.id.checkBox_agree);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = Objects.requireNonNull(mEditEmailView.getText()).toString().trim();
        password = Objects.requireNonNull(mEditPasswordView.getText()).toString().trim();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check is the user agrees to the privacy policy and agreement.
                if (!validateEmail() | !validatePassword()) {
                    return;
                }

                if (mAgreeCheckBox.isChecked()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    loginUser(); // Login user account.
                } else {
                    Snackbar.make(mParentLayout, R.string.tick_box, Snackbar.LENGTH_LONG).show();
                }
            }
        });
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
     * if there are changes in the inserted characters.
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

    /**
     * This will login the user if conditions are met.
     */
    private void loginUser() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            if (mUser != null) {
                                if (mUser.isEmailVerified()) {
                                    launchMainActivity();
                                } else {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    snackVerifyEmail();
                                }
                            }
                        } else {
                            try {
                                if (task.getException() != null) {
                                    throw task.getException();
                                }
                            } catch (FirebaseAuthInvalidUserException e) {
                                Log.d(TAG, "Error: " + task.getException());
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mEmailLayout.setError(getString(R.string.invalid_user));
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Log.d(TAG, "Error: " + task.getException());
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mPasswordLayout.setError(getString(R.string.incorrect_password));
                            } catch (Exception e) {
                                Log.d(TAG, "Error: " + e.getMessage());
                            }
                        }
                    }
                });
    }

    /**
     * If the user choose to verify its email, the app will open the
     * web page of the user's email.
     */
    private void openEmailDomain() {
        String domain = email.substring(email.indexOf("@") + 1);
        Uri webPage = Uri.parse("https://www." + domain);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
        PackageManager packageManager = mActivity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            startActivity(webIntent);
        }
    }

    /**
     * Show snack bar action to verify user.
     */
    private void snackVerifyEmail() {
        mProgressBar.setVisibility(View.INVISIBLE);
        Snackbar.make(mParentLayout, R.string.verify_to_proceed, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .setAction(R.string.verify, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEmailDomain();
                    }
                }).show();
    }

    /**
     * Launch the main activity.
     * This means that the user is logged in.
     */
    private void launchMainActivity() {
        Intent intent = new Intent(mActivity, MainActivity.class);
        startActivity(intent);
        mActivity.finish();
    }
}