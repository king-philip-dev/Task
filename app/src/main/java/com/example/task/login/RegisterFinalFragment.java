package com.example.task.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import android.widget.Toast;

import com.example.task.MainActivity;
import com.example.task.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class contains the final registration UI and logic.
 * It uses the Firebase service for user authentication and database.
 *
 */
public class RegisterFinalFragment extends Fragment {

    private static final String TAG = "Register";

    private static final String USER_COLLECTION = "user";
    private static final String USER_NAME = "name";
    private static final String USER_PHONE = "phone";
    private static final String USER_EMAIL = "email";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@_#$%^&+=])(?=\\S+$).{6,}$");

    // Initialize member variables
    private Activity mActivity;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mStore;
    private Bundle mBundle;
    private RegisterFinalFragmentArgs mArgs;
    private ConstraintLayout mParentLayout;
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputEditText mEditEmailView;
    private TextInputEditText mEditPasswordView;
    private ProgressBar mProgressBar;
    private Button mSendEmailButton;
    private Button mDoneButton;
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
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();

        // Retrieve the bundle args from the previous fragment.
        mBundle = getArguments();
        if (mBundle != null) {
            mArgs = RegisterFinalFragmentArgs.fromBundle(mBundle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_final, container, false);

        // Capture the view objects from the inflated layout.
        mParentLayout = view.findViewById(R.id.constraintLayout_final_register);
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

        email = Objects.requireNonNull(mEditEmailView.getText()).toString().trim();
        password = Objects.requireNonNull(mEditPasswordView.getText()).toString().trim();

        mSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail() | !validatePassword()) {
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                // Register user account.
                registerUser();
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check is the user agrees to the privacy policy and agreement.
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
     * Creates a user account with its email and password.
     */
    private void registerUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User created.");
                            sendEmail(); // Send verification email
                        } else {
                            try {
                                if (task.getException() != null) {
                                    throw task.getException();
                                }
                            } catch (FirebaseAuthUserCollisionException e) {
                                Log.d(TAG, "Email already in used.");
                                mEmailLayout.setError(getString(R.string.email_already_in_use));
                                mProgressBar.setVisibility(View.INVISIBLE);
                            } catch (Exception e) {
                                Log.d(TAG, "Error: " + e.getMessage());
                            }
                        }
                    }
                });
    }

    /**
     * This will send an email to the user for verification.
     */
    private void sendEmail() {
        if (mUser != null) {
            mUser.sendEmailVerification().addOnCompleteListener(mActivity,
                    new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent.");
                        updateUI(); // Update UI
                        snackEmailSent();
                        storeUserData(); // Store user data
                        confirmExit();
                    } else {
                        Log.d(TAG, "Error: " + task.getException());
                    }
                }
            });
        }
    }

    /**
     * This will store the user data in the firebase storage.
     * Passwords are not saved.
     */
    private void storeUserData() {
        // Create an object to map the keys string to values.
        Map<String, Object> user = new HashMap<>();
        user.put(USER_NAME, mArgs.getName());
        user.put(USER_PHONE, mArgs.getPhone());
        user.put(USER_EMAIL, email);

        // Store the user object to the firebase collection (database).
        mStore.collection(USER_COLLECTION).add(user).addOnSuccessListener(mActivity,
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Document added with ID: " + documentReference);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed adding document.", e);
                    }
                });
    }

    /**
     * This will login the user if conditions are met.
     */
    private void loginUser() {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mActivity,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (mUser.isEmailVerified()) {
                            Log.d(TAG, "Email verified.");
                            launchMainActivity(); // Navigate user to main activity.
                        } else {
                            Log.d(TAG, "Please verify your email to proceed.");
                            snackVerifyEmail();
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
     * Show snack back telling user that the email has been sent.
     * And show an action to bring the user to its email domain website.
     */
    private void snackEmailSent() {
        mProgressBar.setVisibility(View.INVISIBLE);
        Snackbar.make(mParentLayout, R.string.email_sent, Snackbar.LENGTH_LONG)
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
     * This means that the user registration is complete.
     */
    private void launchMainActivity() {
        Intent intent = new Intent(mActivity, MainActivity.class);
        startActivity(intent);
        mActivity.finish();
    }

    /**
     * This only show or hide particular ui in the display if
     * conditions are met.
     */
    private void updateUI() {
        mSendEmailButton.setEnabled(false);
        mDoneButton.setEnabled(true);
    }

    /**
     * This method will be triggered if the user already
     * tap the verification email button.
     */
    private void confirmExit() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.exit)
                        .setMessage(R.string.confirm_exit )
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mActivity.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);
    }
}